package org.pm4j.jsf.preprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.jsf.preprocess.PagePreprocessLogic.RequestKind;
import org.pm4j.jsf.util.JsfUtil;
import org.pm4j.jsf.util.NaviJsfUtil;
import org.pm4j.navi.NaviHistoryCfg;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.NaviManager;
import org.pm4j.navi.impl.NaviLinkImpl;
import org.pm4j.navi.impl.NaviRuntimeException;
import org.pm4j.web.RequestUrlReader;
import org.pm4j.web.UrlInfo;

/**
 * Supports application specific preprocessing logic.<br>
 * The logic will be executed within the restore-values JSF phase.
 * <p>
 * TODO: docu!
 *
 * @author olaf boede
 */
public class PreprocessPhaseListener implements PhaseListener {

  private static final long   serialVersionUID = 1L;
  private static final String ATTR_SESSION_START_HANDLED = "pm4j.session_start_handled";
  private static final Logger    log = LoggerFactory.getLogger(PreprocessPhaseListener.class);

  /** Marker instance for pages without preprocess logic. */
  protected static final PagePreprocessLogic NO_PREPROCESS_LOGIC = new PagePreprocessLogic() {
    @Override public RedirectInfo preprocess(UrlInfo requestUrlInfo, RequestKind requestKind) {
      return null;
    }
  };

  /**
   * The url patterns are sorted according to the sequence of
   * {@link #addPreprocessLogic(String, PagePreprocessLogic)} calls.<br>
   * In case of multiple preprocessors per page, the preprocessors will be
   * applied according to the preprocessor add-sequence.
   */
  private Map<Pattern, PagePreprocessLogic> urlPatternToPreprocessLogicMap = new LinkedHashMap<Pattern, PagePreprocessLogic>();
  private Map<String, List<PagePreprocessLogic>> cachedUrlToPreprocessLogicMap = Collections.synchronizedMap(new LinkedHashMap<String, List<PagePreprocessLogic>>());
  /**
   * Only URL page pathes that are not longer than {@link #maxCachedUrlLen} will be cached.<br>
   * This should help to prevent memory problem in case of generated URLs.
   */
  private int maxCachedUrlLen = 40;
  private NaviLink defaultTimeOutRedirectLink = new NaviLinkImpl("/index.jsf");
  private String naviVersionParamName = null;

  public void beforePhase(PhaseEvent event) {
    FacesContext facesCtx = event.getFacesContext();
    ExternalContext extCtx = facesCtx.getExternalContext();
    HttpSession session = (HttpSession)extCtx.getSession(false);
    HttpServletRequest request = (HttpServletRequest) extCtx.getRequest();

    if (session == null || session.isNew()) {
      synchronized (this) {
        if (session.getAttribute(ATTR_SESSION_START_HANDLED) == null) {
          try {
            handleSessionStart(facesCtx, request);
          }
          finally {
            session.setAttribute(ATTR_SESSION_START_HANDLED, "true");
          }
        }
      }
    }
    else {
      // Don't store the synchronization information for the whole session live time.
      session.removeAttribute(ATTR_SESSION_START_HANDLED);

      preprocess(facesCtx, request);
    }
  }

  public void afterPhase(PhaseEvent event) {
    // Do nothing
  }

  public PhaseId getPhaseId() {
    return PhaseId.RESTORE_VIEW;
  }

  /**
   * Will be called on each non-initial request to the JSF application.
   *
   * @param facesCtx
   *          The faces context.
   * @param request
   *          The http request.
   */
  protected void preprocess(FacesContext facesCtx, HttpServletRequest request) {
    UrlInfo requestUrlInfo = RequestUrlReader.INSTANCE.readUrlInfoPutAllParamsToMap(request);

    // ignore the navigation version
    requestUrlInfo.removeParam(getNaviVersionParamName());

    RedirectInfo redirectInfo = doPreprocess(requestUrlInfo, RequestKind.NORMAL);

    if (redirectInfo != null && redirectInfo.toPage != null) {
      log.info(" Redirecting from '" + requestUrlInfo + "' to '" + redirectInfo.toPage + "'");

      // return to the requested page after login:
      if (redirectInfo.shouldRedirectBack) {
        storeNaviBackFromRedirectUrl(requestUrlInfo);
      }

      // redirect immediately:
      NaviJsfUtil.redirect(redirectInfo.toPage);
    }
  }


  /**
   * Will be called on each initial JSF request.
   *
   * @param facesCtx
   *          The faces context.
   * @param request
   *          The http request.
   */
  protected void handleSessionStart(FacesContext facesCtx, HttpServletRequest request) {
    RequestKind requestKind =
      facesCtx.getExternalContext().getRequestParameterMap().containsKey("javax.faces.ViewState")
        ? RequestKind.SESSION_TIMEOUT
        : RequestKind.SESSION_START;

    if (log.isDebugEnabled()) {
      log.debug("Session start request (" + requestKind + "): \n" +
                JsfUtil.getRequestDataLogString());
    }

    // we need a new session for the request
    facesCtx.getExternalContext().getSession(true);

    UrlInfo requestUrlInfo = RequestUrlReader.INSTANCE.readUrlInfoPutAllParamsToMap(request);
    requestUrlInfo.removeParam(getNaviVersionParamName());

    RedirectInfo redirectInfo = doPreprocess(requestUrlInfo, requestKind);

    if (redirectInfo == null) {
      redirectInfo = new RedirectInfo(getDefaultTimeOutRedirectLink(), false);
    };

    if (redirectInfo != null && redirectInfo.toPage != null) {
      log.info(" Redirecting to time out page: " + redirectInfo.toPage);

      // return to the requested page after login:
      if (redirectInfo.shouldRedirectBack) {
        storeNaviBackFromRedirectUrl(requestUrlInfo);
      }

// Did not work in all cases...
//        NaviJsfUtil.redirect(redirectInfo.toPage);

      // alternatively: redirect immediately
      String url = NaviJsfUtil.relUrlForNaviLink(redirectInfo.toPage, false);
      Application app = facesCtx.getApplication();
      ViewHandler viewHandler = app.getViewHandler();
      UIViewRoot view = viewHandler.createView(facesCtx, url);
      facesCtx.setViewRoot(view);
      facesCtx.renderResponse();
      try {
        viewHandler.renderView(facesCtx, view);
        facesCtx.responseComplete();
      } catch (Throwable t) {
        throw new FacesException("Can't redirect to '" + redirectInfo
            + "' on session time out", t);
      }
    } else {
      log.info("No redirect required (according the the result of getRedirectPage() ).");
    }
  }

  /**
   * A subclass may store the original URL for back-navigation after user authentication...
   *
   * @param requestPageInfo The url, the request came from.
   */
  protected void storeNaviBackFromRedirectUrl(UrlInfo requestPageInfo) {
  }

  /**
   * Provides the timeout redirect page for a request to a timed out session.
   *
   * @param requestUrlInfo
   *          The request URL.
   * @param requestKind
   *          Indicates the current session state.
   * @return The link to navigate to or <code>null</code> when there is no redirect to perform.
   */
  protected RedirectInfo doPreprocess(UrlInfo requestUrlInfo, RequestKind requestKind) {
    RedirectInfo redirectInfo = null;
    String urlPath = requestUrlInfo.getPath();
	  List<PagePreprocessLogic> pageLogicList = getPagePreprocessLogic(urlPath);

	  try {
	    for (PagePreprocessLogic pageLogic : pageLogicList) {
	      redirectInfo = pageLogic.preprocess(requestUrlInfo, requestKind);
	      if (redirectInfo != null) {
	        break;
	      }
	    }
	  } catch (Exception e) {
		  // Logger the case and navigate to the default time out page.
		  log.error("Unable to apply preprocessing logic that was registered for page '" +
				  requestUrlInfo + "'\n http request kind: '" + requestKind + "'", e);

		  if (defaultTimeOutRedirectLink != null) {
		    return new RedirectInfo(defaultTimeOutRedirectLink, false);
		  }
    }

	  return redirectInfo;
  }

  /**
   * @param urlPath
   *          The page path to get the preprocess logics for.
   * @return The found set. An empty list if there is no preprocessing defined
   *         for the given page.
   */
  @SuppressWarnings("unchecked")
  protected List<PagePreprocessLogic> getPagePreprocessLogic(String urlPath) {
    List<PagePreprocessLogic> list = null;
    boolean cacheCandidate = isCacheableUrlPath(urlPath);

    // check the cache first:
    if (cacheCandidate) {
      list = cachedUrlToPreprocessLogicMap.get(urlPath);
      if (list != null) {
        return list;
      }
    }

    // no cache hit:
    list = new ArrayList<PagePreprocessLogic>();
    for (Map.Entry<Pattern, PagePreprocessLogic> e : urlPatternToPreprocessLogicMap.entrySet()) {
      Matcher matcher = e.getKey().matcher(urlPath);
      if (matcher.matches()) {
        list.add(e.getValue());
      }
    }

    // maintain the cache:
    if (cacheCandidate) {
      cachedUrlToPreprocessLogicMap.put(urlPath, list.size() > 0
                                      ? list
                                      : Collections.EMPTY_LIST);
    }

    return list;
  }

  /**
   * Defines if a given URL path-to-preprocessor assignment may be cached.
   * <p>
   * The default implementation only checks of the path is not longer than
   * {@link #maxCachedUrlLen}.
   *
   * @param urlPath
   *          The path to check.
   * @return <code>true</code> if the preprocessor assignment for the given path
   *         may be cached.
   */
  protected boolean isCacheableUrlPath(String urlPath) {
    return urlPath.length() <= maxCachedUrlLen;
  }

  /**
   * Adds a redirect logic handler for the given URL pattern.<br>
   * If a page matches more than one pattern, the preprocessors will be applied
   * according to their {@link #addPreprocessLogic(String, PagePreprocessLogic)}
   * call sequence.<br>
   * The first preprocessor that returns a redirect hint will stop the
   * processing of the preprocessor chain.
   * <p>
   * See {@link Pattern} for the regular expression syntax used here.
   * <p>
   * Examples:
   * <ul>
   * <li>'/admin/users.jsf' - adds a preprocessor exactly for this page path.</li>
   * <li>'/admin/.*' - adds a proprocessor to all pages within the admin area.</li>
   * <ul>
   * <p>
   * RESTRICTION: Currently the same pattern may be added only once.
   *
   * @param urlPattern
   *          The URL pattern to provide a redirect logic for.
   * @param logic
   *          The URL specific redirect logic.
   */
  public void addPreprocessLogic(String urlPattern, PagePreprocessLogic logic) {
    if (urlPatternToPreprocessLogicMap.put(Pattern.compile(urlPattern), logic) != null) {
      throw new NaviRuntimeException("Page URL pattern is already added to the preprocesser set: " + urlPattern);
    }
  }

  private String getNaviVersionParamName() {
    if (naviVersionParamName == null) {
      NaviManager naviManager = NaviJsfUtil.findNaviManager();
      if (naviManager != null) {
        naviVersionParamName = naviManager.getNaviCfg().getVersionParamName();
      }
      else {
        // fallback
        return NaviHistoryCfg.DEFAULT_NAVI_HISTORY_VERSION_PARAM_NAME;
      }
    }

    return naviVersionParamName;
  }


  public NaviLink getDefaultTimeOutRedirectLink() {
    return defaultTimeOutRedirectLink;
  }

  public void setDefaultTimeOutRedirectLink(NaviLink defaultTimeOutRedirectLink) {
    this.defaultTimeOutRedirectLink = defaultTimeOutRedirectLink;
  }

  /**
   * Only URL page pathes that are not longer than {@link #maxCachedUrlLen} will be cached.<br>
   * This should help to prevent memory problem in case of generated URLs.
   *
   * @param maxCachedUrlLen The defined limit.
   */
  public void setMaxCachedUrlLen(int maxCachedUrlLen) {
    this.maxCachedUrlLen = maxCachedUrlLen;
  }

}

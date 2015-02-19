package org.pm4j.jsf;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.jsf.util.JsfUtil;
import org.pm4j.jsf.util.NaviJsfUtil;
import org.pm4j.jsf.util.PmJsfUtil;
import org.pm4j.navi.NaviHistory;
import org.pm4j.navi.NaviHistoryCfg;
import org.pm4j.navi.NaviManager;
import org.pm4j.navi.NaviManager.NaviMode;
import org.pm4j.navi.impl.NaviLinkImpl;
import org.pm4j.navi.impl.NaviRuntimeException;
import org.pm4j.navi.impl.NaviUtil;
import org.pm4j.web.UrlInfo;

/**
 * Maintains the navigation history.
 * The history itself is stored in the user session.
 *
 * @author olaf boede
 */
// TODO olaf: move to navi package
public class NaviHistoryPhaseListener implements PhaseListener {

  private static final long serialVersionUID = -7489565064238366392L;
  private static final Logger LOG = LoggerFactory.getLogger(NaviHistoryPhaseListener.class);

  private static final String REQ_ATTR_REDIRECT_TO = "pm4j.navi.phaseListnerRedirectUrl";
  private static final String REQ_ATTR_OLD_HISTORY = "pm4j.navi.oldHistory";

  private NaviHistoryCfg naviCfg = new NaviHistoryCfg();

  public void beforePhase(PhaseEvent event) {
    HttpServletRequest httpRequest = (HttpServletRequest) event.getFacesContext().getExternalContext().getRequest();

    if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
      ParamReader reader = new ParamReader(httpRequest);

      NaviManager naviManager = NaviJsfUtil.getNaviManager(naviCfg);
      NaviHistory oldHistory = naviManager.findHistory(reader.sessionId, reader.versionId);
      // save it for error handling in the render-response phase.
      if (oldHistory != null) {
        httpRequest.setAttribute(REQ_ATTR_OLD_HISTORY, oldHistory);
      }

      if (naviCfg.getHistoryPathMatcher().isNaviHistoryPath(reader.requestUrl)) {
        NaviHistory newHistory = naviManager.onNavigateTo(reader.naviLinkWithoutFragment, reader.versionString, reader.naviMode);

        // Provide the request navigation history, used for links and forms.
        NaviJsfUtil.setNaviHistoryRequestAttr(newHistory);

        // Redirect to new version URL when the version number is changed.
        // Postpone the redirect to the render-response-phase to not disturb
        // redirects caused by the application logic.
        if (! newHistory.getVersionString().equals(reader.versionString)) {

          // Some debug output in case of a new version number triggered redirect:
          if (LOG.isTraceEnabled()) {
            LOG.trace("Version changed from " + reader.versionString + " to " + newHistory.getVersionString() +
                "\n  Request details: " + JsfUtil.getRequestDataLogString());
          }

          // But: Don't redirect when the user is still on the same page.
          //      That would cause an unexpected page reload for the user.
          if ((oldHistory != null) &&
              oldHistory.getCurrentLink().isLinkToSamePage(newHistory.getCurrentLink())) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("Version change on active page detected. No redirect will be done to preserve the current browser position." +
                  "\n\tHistory=" + newHistory);
            }
          }
          else
          // In addition: Don't redirect if the faces response is already completed
          // by another phase listener (e.g. a PreprocessPhaseListener).
          if (! FacesContext.getCurrentInstance().getResponseComplete()) {
            LOG.debug("Version change on active page detected. \n" +
                "The faces response is already complete. \n" +
                "So we assume that the code that has completed the response already redirected the application to a new valid page position." +
                "\n\tHistory=" + newHistory);
          }
          else {
            httpRequest.setAttribute(REQ_ATTR_REDIRECT_TO, reader.requestUrl);

            if (LOG.isDebugEnabled()) {
              LOG.debug("Version change detected. Redirecting request to URL with current version parameter." +
                  "\n\tHistory=" + newHistory);
            }
          }
        }
      }
      // else for: naviManager.isRelevantPath(reader.requestUrl)
      else {
        if (oldHistory != null) {
          // XXX alternatively: Ping on NaviMgr
          oldHistory.ping();

          // Provide the identified old navigation history, even if the target page
          // is not navigation relevant. This way even the code for those pages may
          // use that information.
          NaviJsfUtil.setNaviHistoryRequestAttr(oldHistory);
        }

        if (LOG.isTraceEnabled()) {
          LOG.trace("Received call for page '" + reader.requestUrl
              + "'. Is not relevant for navigation history management."
              + "\n  httpRequest.getHeader('REFERER')   =" + httpRequest.getHeader("REFERER")
              );
        }
      }


    }
    // Postponed version-change triggered redirects. Will only be applied when
    // the application did not redirect in other phases.
    else if (event.getPhaseId() == PhaseId.RENDER_RESPONSE)
    {
      errorCheck(httpRequest);

      if (! event.getFacesContext().getResponseComplete()) {
        String redirectUrl = (String)httpRequest.getAttribute(REQ_ATTR_REDIRECT_TO);
        if (redirectUrl != null) {
          NaviJsfUtil.redirect(redirectUrl);
        }
      }
    }
  }

  public void afterPhase(PhaseEvent event) {
  }

  public PhaseId getPhaseId() {
    return PhaseId.ANY_PHASE;
  }

  /**
   * Checks if there is any error information in the given request.
   * If there is any, it will be reported to the log.
   *
   * @param request The request to check.
   * @return <code>true</code> when there is no reason to prevent redirects because of errors.
   */
  protected boolean errorCheck(HttpServletRequest request) {
    Object error_code = request.getAttribute("javax.servlet.error.status_code");

    if (error_code != null) {
      StringBuilder sb = new StringBuilder(100);
      Object uri = request.getAttribute("javax.servlet.error.request_uri");
      Object msg = request.getAttribute("javax.servlet.error.message");
      String referrer = request.getHeader("REFERER");
      NaviHistory naviHistory = (NaviHistory) request.getAttribute(REQ_ATTR_OLD_HISTORY);

      sb.append("Received request with error state: " +
                "\n\tjavax.servlet.error.status_code: ").append(error_code);
      sb.append("\n\tjavax.servlet.error.request_uri: ").append(uri);
      if (! ObjectUtils.equals(uri, msg)) {
        sb.append("\n\tjavax.servlet.error.message    : ").append(msg);
      }
      if (referrer != null) {
        sb.append("\n\tReferrer                       : ").append(referrer);
      }
      if (naviHistory != null) {
        sb.append("\n\tNavigation history             : ").append(naviHistory.toString());
      }

      LOG.error(sb.toString(), (Throwable) request.getAttribute("javax.servlet.error.exception"));

      return false;
    }
    else {
      return true;
    }
  }

  public NaviHistoryCfg getNaviCfg() {
    return naviCfg;
  }

  public void setNaviCfg(NaviHistoryCfg naviCfg) {
    assert naviCfg != null;

    this.naviCfg = naviCfg;
  }


  // FIXME: use the RequestUrlReader. Most of the code here is now redundant!
  /**
   * Reads the parameter information from the httpRequest.
   * <p>
   * In case of a missing version parameter within the request,
   * the referrer will be used as an additional information
   * source.
   */
  class ParamReader {
    private String fragment;
    private String versionString;
    private String sessionId;
    private String versionId;
    private NaviMode naviMode = NaviMode.NORMAL;
    private String requestUrl;
    private NaviLinkImpl naviLinkWithoutFragment;

    public ParamReader(HttpServletRequest httpRequest) {
      String versParName = naviCfg.getVersionParamName();
      boolean pm4jParamInUrl = false;
      UrlInfo requestUrlInfo = new UrlInfo(httpRequest);

      // 1. Try to get the version first from the 'original' - The request URL.
      //    This is the only valid source in case of parameterized links.
      //    - Remove the version from the URL. The current version parameter will
      //      be added by the navigation code...
      requestUrlInfo.getParams().remove(versParName);

      // 2. Try to get it from the post parameter set.
      //    Usual source in case of HTTP-post requests.
      if (StringUtils.isEmpty(versionString)) {
        versionString = httpRequest.getParameter(versParName);
      }

      // 3. Fall back solution:
      //    Try to get the parameter set from the referrer if not provided in the request.
      //    That may happen in case of simple 'hand made' links without version parameter.
      //
      String referrer = httpRequest.getHeader("REFERER");
      boolean referrerUrlUsed = false;
      if (referrer != null) {
        UrlInfo referrerUrlInfo = new UrlInfo(referrer);

        // Fall back solution for history version: Take the version from the
        // referrer. That usually happens in case of simple HTTP links.
        if (StringUtils.isEmpty(versionString)) {
          versionString = referrerUrlInfo.getParams().get(versParName);
        }

        // When the request navigates to the same page:
        // Preserve all other referrer parameters (except the navigation version).
        // That prevents that keep-alive pings will destroy the existing page
        // parameter set.
        if (referrerUrlInfo.getPath().endsWith(requestUrlInfo.getPath())) {
          // The navigation history is not part of the path string part that
          // identifies the page.
          referrerUrlInfo.getParams().remove(versParName);

          // change from absolute to relative path:
          referrerUrlInfo.setPath(requestUrlInfo.getPath());

          // All parameters that will be part of the URL need to be updated
          // to their actual values from the request.
          for (Map.Entry<String, String> e : referrerUrlInfo.getParams().entrySet()) {
            String reqValue = requestUrlInfo.getParams().get(e.getKey());
            if (reqValue != null) {
              e.setValue(reqValue);
              // Prevent duplication in request.
              // Would be especially bad for the pm4j-Parameter that would be added
              // to the URL twice.
              requestUrlInfo.getParams().remove(e.getKey());
            }
          }

          requestUrlInfo.setPath(referrerUrlInfo.buildUrl());
          pm4jParamInUrl = referrerUrlInfo.getParams().containsKey(PmJsfUtil.PM4J_REQUEST_PARAM);
          referrerUrlUsed = true;
        }

        fragment = referrerUrlInfo.getFragment();
      }

      // Analyze the navigation version string
      if (!StringUtils.isEmpty(versionString)) {
        // Extract navigation mode
        if (versionString.startsWith(NaviMode.POPUP.getPrefix())) {
          naviMode = NaviMode.POPUP;
          versionString = StringUtils.substringAfter(versionString, NaviMode.POPUP.getPrefix());
        }

        // FIXME olaf: find a graceful error handling method here for invalid strings.
        String[] versArray = NaviUtil.splitVersionString(versionString);

        sessionId = versArray[0];
        versionId = versArray[1];
      }

      // Preserve the pm4j parameter in case it was send via HTTP-post only.
      if ((! pm4jParamInUrl) &&
          ! requestUrlInfo.getParams().containsKey(PmJsfUtil.PM4J_REQUEST_PARAM)) {
        String paramValueString = httpRequest.getParameter(PmJsfUtil.PM4J_REQUEST_PARAM);
        if (StringUtils.isNotBlank(paramValueString)) {
          try {
            paramValueString = URLEncoder.encode(paramValueString, "UTF-8");
          } catch (UnsupportedEncodingException e) {
            throw new NaviRuntimeException("Unable to encode '" + paramValueString + "'.", e);
          }
          requestUrlInfo.getParams().put(PmJsfUtil.PM4J_REQUEST_PARAM, paramValueString);
        }
      }

      // The request URL (incl. preserved URL parameter set from referrer).
      requestUrl = requestUrlInfo.buildUrl();

      naviLinkWithoutFragment = NaviJsfUtil.makeNaviLink(requestUrl, null);
    }

    public String getFragment() {
      return fragment;
    }

    public String getVersionString() {
      return versionString;
    }

    public String getSessionId() {
      return sessionId;
    }

    public String getVersionId() {
      return versionId;
    }

  }

}

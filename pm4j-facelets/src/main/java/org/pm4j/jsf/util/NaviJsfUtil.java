package org.pm4j.jsf.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.jsf.NaviHistoryPhaseListener;
import org.pm4j.navi.NaviHistory;
import org.pm4j.navi.NaviHistoryCfg;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.NaviManager;
import org.pm4j.navi.impl.NaviLinkImpl;
import org.pm4j.navi.impl.NaviManagerImpl;
import org.pm4j.navi.impl.NaviRuntimeException;
import org.pm4j.web.UrlInfo;

public class NaviJsfUtil {

  private static final Log LOG = LogFactory.getLog(NaviJsfUtil.class);

  /** Name of the session attribute that stores the navigation manager. */
  private static final String NAVI_MGR_ATTR = "pm4j.navi.mgr";
  /** Name of the request attribute that stores the current navigation history. */
  private static final String NAVI_HISTORY_ATTR = "pm4j.navi.history";


  /**
   * Redirects to the provided navigation link.
   * <p>
   * In case of a {@link NaviManager} within the current session it adds
   * navigation version information to the URL to redirect to.
   *
   * @param url
   *          The URL to redirect to.
   */
  public static void redirect(NaviLink naviLink) {
    redirect(relUrlForNaviLink(naviLink, true));
  }

  /**
   * Redirects to the provided navigation path.
   * <p>
   * In case of a {@link NaviManager} within the current session it adds
   * navigation version information to the URL to redirect to.
   *
   * @param url
   *          The URL to redirect to.
   */
  public static void redirect(String url) {
    NaviManager m = findNaviManager();
    if (m != null) {
      redirect(url, m, getNaviHistory());
    }
    else {
      JsfUtil.redirect(url);
    }
  }

  /**
   * Redirects to the provided navigation path.
   * <p>
   * Adds version information if required.
   *
   * @param url
   *          An application relative URL to redirect to. Should not contain the servlet path.
   * @param naviManager
   *          The navigation manager of the current user session.
   * @param currentHistory
   *          The history for the current navigation state.
   */
  public static void redirect(String url, NaviManager naviManager, NaviHistory currentHistory) {
    if ((currentHistory == null) ||
        !naviManager.getNaviCfg().getHistoryPathMatcher().isNaviHistoryPath(url)) {
      // a) No history present.
      //    A link from a non-history page should not manage navigation histories...
      // b) Navigation history management not relevant for the target URL.
      JsfUtil.redirect(url);
    }
    else {
      // Prevent duplication of versions if already provided in the URL.
      String versParamName = naviManager.getNaviCfg().getVersionParamName();
      UrlInfo urlInfo = new UrlInfo(url);
      urlInfo.getParams().remove(versParamName);

      NaviLink link = makeNaviLink(urlInfo.buildUrlWithoutFragment(), urlInfo.getFragment());

      // Management of navigation version BEFORE redirect prevents
      // a second redirect within the navigation phase listener.
      NaviHistory newHistory = naviManager.onNavigateTo(link, currentHistory.getVersionString());
      urlInfo.getParams().put(versParamName, newHistory.getVersionString());

      JsfUtil.redirect(urlInfo.buildUrl());
    }
  }

  /**
   * Creates a navigation link for a given encoded relative URL.
   *
   * @param url The URL to generate a link for.
   * @param pagePos The fragment to add.
   * @return The generated navigation link.
   */
  public static NaviLinkImpl makeNaviLink(String url, String pagePos) {
    UrlInfo requestUrlInfo = new UrlInfo(url);
    String pm4jParamValueString = requestUrlInfo.getParams().remove(PmJsfUtil.PM4J_REQUEST_PARAM);
    NaviLinkImpl naviLink = new NaviLinkImpl(requestUrlInfo.buildUrlWithoutFragment(), pagePos);

    // Decode and transfer the pm4j parameter to a map.
    if (pm4jParamValueString != null) {
      try {
        pm4jParamValueString = URLDecoder.decode(pm4jParamValueString, "UTF-8");
      } catch (UnsupportedEncodingException e1) {
        throw new NaviRuntimeException("unable to decode pm4j parameter '" + pm4jParamValueString +
            "' found in request URL '" + url + "'", e1);
      }
      Map<String, Object> pm4jParamMap = PmJsfUtil.URL_PARAM_CODER.paramValueToMap(pm4jParamValueString);
      for (Map.Entry<String, Object> e : pm4jParamMap.entrySet()) {
        naviLink.addAttrValueParam(e.getKey(), e.getValue());
      }
    }

    return naviLink;
  }

  /**
   * Generates a relative URL for a given navigation link.
   * It does not contain the mapped servlet name.
   *
   * @param naviLink
   *          The link.
   * @param withVersion
   *          Defines if the navigation version should be part of the URI. Links
   *          without version are useful for external link generation.
   * @return The application internal URL for the link.
   */
  public static String relUrlForNaviLink(NaviLink naviLink, boolean withVersion) {
    String uiParam = null;
    if (naviLink instanceof NaviLinkImpl) {
      Map<String, Object> uiParams = ((NaviLinkImpl)naviLink).getParams();
      uiParam = PmJsfUtil.URL_PARAM_CODER.mapToParamValue(uiParams);
    }
    String url = makeUrl(new UrlInfo(naviLink.getPath(), naviLink.getPosOnPage()), uiParam, withVersion);
    return url;
  }

  /**
   * @param i
   *          Contains the URL content.
   * @param uiParam
   *          The optional pm4j parameter to encode and add to the URL.
   * @param withVersion
   *          Defines if the navigation version parameter should be added.
   * @return The generated URL string.
   */
  public static String makeUrl(UrlInfo i, String uiParam, boolean withVersion) {
    NaviHistory h = withVersion
                      ? NaviJsfUtil.getNaviHistory()
                      : null;
    return makeUrl(i, uiParam, h);
  }

  /**
   * @param i
   *          Contains the URL content.
   * @param uiParam
   *          The optional pm4j parameter to encode and add to the URL.
   * @param history
   *          The navigation version parameter will be added if this is not
   *          <code>null</code>.
   * @return The generated URL string.
   */
  public static String makeUrl(UrlInfo i, String uiParam, NaviHistory history) {
    if (StringUtils.isNotBlank(uiParam)) {
      try {
        uiParam = URLEncoder.encode(uiParam, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new NaviRuntimeException("Unable to encode: " + uiParam, e);
      }

      i.getParams().put(PmJsfUtil.PM4J_REQUEST_PARAM, uiParam);
    }

    if (history != null) {
      i.getParams().put(history.getNaviCfg().getVersionParamName(), history.getVersionString());
    }

    return i.buildUrl();
  }

  /**
   * Please make sure that {@link #getNaviManager(NaviHistoryCfg)} was called
   * before within this HTTP session.
   *
   * @return The navigation configuration.
   */
  public static NaviHistoryCfg getNaviCfg() {
    return getNaviManager(null).getNaviCfg();
  }

  /**
   * @param value
   *          The navigation version to be used for the current request.
   */
  public static void setNaviHistoryRequestAttr(NaviHistory h) {
    JsfUtil.getHttpRequest().setAttribute(NAVI_HISTORY_ATTR, h);
  }

  /**
   * @return The navigation history of the current session. Or <code>null</code>
   *         if no history is managed within the current request.
   */
  public static NaviHistory getNaviHistory() {
    return (NaviHistory)JsfUtil.getHttpRequest().getAttribute(NAVI_HISTORY_ATTR);
  }

  /**
   * @param naviCfg
   *          The navigation configuration. <br>
   *          Should be provided for the first call of this method of each
   *          http-session.<br>
   *          When this fist call is done, all other calls within this session
   *          may provide <code>null</code> for this parameter.
   * @return The pm4j navigation manager of the current session.
   */
  public static NaviManager getNaviManager(NaviHistoryCfg naviCfg) {
    HttpSession session = (HttpSession) JsfUtil.getExternalContext().getSession(true);
    NaviManager naviMgr = (NaviManager) session.getAttribute(NAVI_MGR_ATTR);
    if (naviMgr == null) {
      if (naviCfg == null) {
        throw new NaviRuntimeException(
            "Can't create navigation manager without context information. " +
            "Please ensure that the method getNaviManager(naviCfg) gets called first" +
            "with a defined configuration. E.g. on application or phaselister initialization.");
      }

      naviMgr = new NaviManagerImpl(naviCfg);
      session.setAttribute(NAVI_MGR_ATTR, naviMgr);
    }
    return naviMgr;
  }

  /**
   * @param naviCfg
   *          The navigation configuration. <br>
   *          Should be provided for the first call of this method of each
   *          http-session.<br>
   *          When this fist call is done, all other calls within this session
   *          may provide <code>null</code> for this parameter.
   * @return The pm4j navigation manager of the current session or <code>null</code> it there is no one.
   */
  public static NaviManager findNaviManager() {
    HttpSession session = (HttpSession) JsfUtil.getExternalContext().getSession(true);
    return (NaviManager) session.getAttribute(NAVI_MGR_ATTR);
  }

  // TODO olaf: Sollen diese wieder verschwinden
  public static String getHistoryParamKey() {
    NaviHistory h = getNaviHistory();
    return h != null
              ? h.getNaviCfg().getVersionParamName()
              : NaviHistoryCfg.DEFAULT_NAVI_HISTORY_VERSION_PARAM_NAME;
  }

  public static String getHistoryParamValue() {
    NaviHistory h = getNaviHistory();
    return h != null ? h.getVersionString() : "";
  }

  // FIXME move to webflow listener
  public static void setHistoryParamValue(String s) {
    HttpSession session = (HttpSession) JsfUtil.getExternalContext().getSession(false);
    NaviManager naviManager = session != null
          ? (NaviManager)session.getAttribute(NAVI_MGR_ATTR)
          : null;

    if (naviManager != null) {
      session.setAttribute(naviManager.getNaviCfg().getVersionParamName(), s);
    }
    else {
      LOG.warn("Unable to set the history parameter to '" + s +
          "'. No navigation history manager found.");
    }
  }

  /**
   * Generates a string that represents a history version expression that may
   * be used to construct URLs for links to popup windows.
   * <p>
   * The expression provides a hint for the history management to generate a
   * popup specific navigation session.
   *
   * @return A string like 'v=_popup_1.2'
   */
  public static String getPopupNaviParam() {
    NaviHistory h = NaviJsfUtil.getNaviHistory();

    if (h != null) {
      return h.getNaviCfg().getVersionParamName() + "=" +
             NaviManager.NaviMode.POPUP.getPrefix() + h.getVersionString();
    }
    else {
      return "";
    }
  }


  /**
   * Adds the current navigation history version parameter to the given URL.
   * <p>
   * Adds the parameter only if a navigation version history is configured. See
   * {@link NaviHistoryPhaseListener} for more information.
   *
   * @param baseUrl
   *          The URL to extend.
   * @return URL with history version parameter.
   */
  public static String urlWithHistoryVersion(String baseUrl) {
    NaviHistory history = getNaviHistory();
    if (history == null) {
      return baseUrl;
    }

    UrlInfo i = new UrlInfo(baseUrl);
    i.getParams().put(history.getNaviCfg().getVersionParamName(), history.getVersionString());

    return i.buildUrl();
  }

}

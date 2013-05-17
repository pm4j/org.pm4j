package org.pm4j.jsf.deprecated;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.connector.NamedObjectResolver;
import org.pm4j.core.pm.impl.connector.NamedObjectResolverChain;
import org.pm4j.core.pm.impl.connector.PmToViewTechnologyConnector;
import org.pm4j.jsf.PmToJsfConnector;
import org.pm4j.jsf.connector.AttrToJsfViewConnectorWithValueChangeListener;
import org.pm4j.jsf.connector.NamedObjectResolverForJsfEl;
import org.pm4j.jsf.connector.NamedObjectResolverForJsfRequestAttr;
import org.pm4j.jsf.connector.NamedObjectResolverForJsfSessionAttr;
import org.pm4j.jsf.impl.PmToJsfConnectorImpl;
import org.pm4j.jsf.util.JsfUtil;
import org.pm4j.jsf.util.NaviJsfUtil;
import org.pm4j.jsf.util.PmJsfUtil;
import org.pm4j.navi.NaviHistory;
import org.pm4j.navi.impl.NaviLinkImpl;
import org.pm4j.web.UrlInfo;

/**
 * Supports PM operations in JSF environments.
 *
 * @author olaf boede
 * @deprecated Please use the version located in the package 'connector'
 */
@Deprecated
public class PmConnectorForJsf implements PmToViewTechnologyConnector {

  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(PmConnectorForJsf.class);

  /**
   * The JSF API is only used via this connector. That allows unit-testability
   * without special JSF unit test frameworks.
   */
  private PmToJsfConnector pmToJsfConnector = new PmToJsfConnectorImpl();

  private NamedObjectResolver namedObjectResolver = new NamedObjectResolverChain(
      new NamedObjectResolverForJsfRequestAttr(),
      new NamedObjectResolverForJsfSessionAttr(),
      new NamedObjectResolverForJsfEl());

  /**
   * Maintains the navigation history <b>before</b> the JSF redirect takes place.
   * This allows to pass navigation context values by reference to the next
   * navigation position.
   */
  @Override
  public void redirect(NaviLinkImpl naviLink) {
    if (naviLink != null) {
      NaviHistory h = NaviJsfUtil.getNaviHistory();
      if (h != null) {
        h = h.getNaviManager().onNavigateTo(naviLink, h.getVersionString());
        String uiParam = PmJsfUtil.URL_PARAM_CODER.mapToParamValue(naviLink.getParams());
        String url = NaviJsfUtil.makeUrl(new UrlInfo(naviLink.getPath(), naviLink.getPosOnPage()), uiParam, h);
        JsfUtil.redirect(url);
      }
      else {
        boolean withVersion = ! naviLink.isExternalLink();
        String url = PmJsfUtil.relUrlForNaviLink(naviLink, withVersion);
        NaviJsfUtil.redirect(url);
      }
    }
    else {
      // Stay on page. Make sure that the PM URL parameter set stays stable:
      redirectWithRequestParams(null);
    }
  }

  @Override
  public boolean hasRequestParams() {
    return JsfUtil.readReqParam(PmJsfUtil.PM4J_REQUEST_PARAM) != null;
  }

  @Override
  public void redirectWithRequestParams(String url) {
    String toUrl = url;
    if (url == null) {
      HttpServletRequest r = JsfUtil.getHttpRequest();
      toUrl = NaviJsfUtil.makeUrl(new UrlInfo(r), r.getParameter(PmJsfUtil.PM4J_REQUEST_PARAM), true);
    }
    else {
      toUrl = NaviJsfUtil.makeUrl(new UrlInfo(toUrl), JsfUtil.readReqParam(PmJsfUtil.PM4J_REQUEST_PARAM), true);
    }

    NaviJsfUtil.redirect(toUrl);
  }

  @Override
  public String readRequestValue(String attrName) {
    return PmJsfUtil.readPm4jParamValue(attrName);
  }

  @Override
  public NaviHistory getNaviHistory() {
    return pmToJsfConnector.getNaviHistory();
  }

  @Override
  public Object readRequestAttribute(String attrName) {
    return JsfUtil.getHttpRequest().getAttribute(attrName);
  }

  @Override
  public void setRequestAttribute(String attrName, Object value) {
    JsfUtil.getHttpRequest().setAttribute(attrName, value);
  }

  @Override
  public NamedObjectResolver getNamedObjectResolver() {
    return namedObjectResolver;
  }

  @Override
  public Object createPmToViewConnector(PmObject pm) {
    if (pm instanceof PmAttr) {
      return new AttrToJsfViewConnectorWithValueChangeListener((PmAttr<?>)pm);
    }

    // default:
    return null;
  }

  // -- getter setter --

  public PmToJsfConnector getPmToJsfConnector() {
    return pmToJsfConnector;
  }

  public void setPmToJsfConnector(PmToJsfConnector pmToJsfConnector) {
    this.pmToJsfConnector = pmToJsfConnector;
  }

}

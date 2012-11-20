package org.pm4j.jsf.impl;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.pm4j.jsf.PmToJsfConnector;
import org.pm4j.jsf.util.JsfUtil;
import org.pm4j.jsf.util.NaviJsfUtil;
import org.pm4j.navi.NaviHistory;

public class PmToJsfConnectorImpl implements PmToJsfConnector {

  @Override
  public String getRequestContextPath() {
    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
    return externalContext.getRequestContextPath();
  }

  @Override
  public Object getRequestAttr(String paramName) {
    return JsfUtil.getHttpRequest().getAttribute(paramName);
  }

  @Override
  public void setRequestAttr(String paramName, Object paramValue) {
    JsfUtil.getHttpRequest().setAttribute(paramName, paramValue);
  }

  @Override
  public NaviHistory getNaviHistory() {
    return NaviJsfUtil.getNaviHistory();
  }

}

package org.pm4j.core.pm.impl.connector;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTabSet;
import org.pm4j.navi.NaviHistory;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.impl.NaviLinkImpl;

/**
 * Default navigation handler that does not know about the UI
 * platform specific operations (http redirect etc.).<p>
 *
 * @author olaf boede
 */
public class PmToNoViewTechnologyConnector implements PmToViewTechnologyConnector {

  private static final Log LOG = LogFactory.getLog(PmToNoViewTechnologyConnector.class);
  private NaviLink lastNaviLink;

  /**
   * Just reports a warning.
   */
  @Override
  public void redirect(NaviLinkImpl naviLink) {
    lastNaviLink = naviLink;
    LOG.warn("No UI platform navigation handler defined. Please check your PM session configuration. No navigation performed for link: " + naviLink);
  }

  @Override
  public boolean hasRequestParams() {
    return false;
  }

  @Override
  public void redirectWithRequestParams(String toPageId) {
    LOG.warn("No UI platform navigation handler defined. Please check your PM session configuration. No navigation performed for page: " + toPageId);
  }

  @Override
  public String readRequestValue(String attrName) {
    String value = null;
    if (lastNaviLink != null && lastNaviLink instanceof NaviLinkImpl) {
      value = ObjectUtils.toString(
          ((NaviLinkImpl)lastNaviLink).getParams().get(attrName), null);
    }
    return value;
  }

  @Override
  public NaviHistory getNaviHistory() {
    return null;
  }

  @Override
  public Object readRequestAttribute(String attrName) {
    return null;
  }

  @Override
  public void setRequestAttribute(String attrName, Object value) {
  }

  @Override
  public Object findNamedObject(String attrName) {
    return System.getProperty(attrName);
  }

  @Override
  public PmTabSetConnector createTabSetConnector(PmTabSet pmTabSet) {
    return new PmTabSetConnectorDefaultImpl();
  }

  @Override
  public Object createPmToViewAdapter(PmObject pm) {
    return null;
  }
}

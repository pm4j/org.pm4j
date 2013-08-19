package org.pm4j.jsf;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.pm4j.navi.NaviHistory;

/**
 * Test mock that allows to test jsf handlers in a pure jUnit environment.
 */
public class PmToJsfConnectorMock implements PmToJsfConnector {

  private Map<String, Object> attrMap = new HashMap<String, Object>();

  @Override
  public String getRequestContextPath() {
    return "/myAppPath";
  }

  @Override
  public Object getRequestAttr(String attrName) {
    return attrMap.get(attrName);
  }

  @Override
  public void setRequestAttr(String attrName, Object attrValue) {
    attrMap.put(attrName, attrValue);
  }

  @Override
  public NaviHistory getNaviHistory() {
    throw new NotImplementedException();
  }
}

package org.pm4j.jsf;

import org.pm4j.navi.NaviHistory;

/**
 * Interface to the JSF implementation.<p>
 *
 * It was introduced to provide unit testability for JSF related
 * implementation parts.
 *
 * @author olaf boede
 */
public interface PmToJsfConnector {

  /**
   * @return The url path to the current request.
   */
  String getRequestContextPath();

  /**
   * Reads the attribute from the current request.
   *
   * @param attrName
   *          Attribute key.
   * @return The attribute value. Is <code>null</code> when the attribute is
   *         not part of the current request.
   */
  Object getRequestAttr(String attrName);

  /**
   * @param attrName
   *          Attribute key.
   * @param attrValue
   *          The attribute value.
   */
  void setRequestAttr(String attrName, Object attrValue);

  /**
   * @return The navigation history.
   *         Usually stored in the current request.
   */
  public NaviHistory getNaviHistory();

}

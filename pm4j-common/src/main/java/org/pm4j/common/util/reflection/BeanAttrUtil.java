package org.pm4j.common.util.reflection;

import org.apache.commons.lang.StringUtils;

public class BeanAttrUtil {

  public static final String TERM_STRING = ".";

  /**
   *
   *
   * @param startObject The object to read the addressed property from.
   * @param path The dot-separated address path. E.g. 'myAttr.subAttr'
   * @return The addressed object. <code>null</code> if one of the address path's did point to <code>null</code>.
   */
  public static Object resolveReflectionPath(Object startObject, String path) {
    if (startObject == null) {
      return null;
    }

    String attrName = StringUtils.substringBefore(path, TERM_STRING);

    // XXX: Check if it's useful to manage a static accessor map to improve performance.
    BeanAttrAccessor a = new BeanAttrAccessorImpl(startObject.getClass(), attrName);
    Object refedObj = a.getBeanAttrValue(startObject);

    if (attrName.length() == path.length()) {
      return refedObj;
    }
    else {
      return resolveReflectionPath(refedObj, StringUtils.substringAfter(path, TERM_STRING));
    }
  }

}

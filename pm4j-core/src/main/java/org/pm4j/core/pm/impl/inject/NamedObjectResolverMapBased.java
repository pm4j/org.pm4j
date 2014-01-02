package org.pm4j.core.pm.impl.inject;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * A named object resolver that is implemented based on a simple name-value map.
 *
 * @author olaf boede
 */
public class NamedObjectResolverMapBased implements NamedObjectResolver {

  private Map<String, Object> map = new HashMap<String, Object>();

  public void put(Class<?> interfaceClass, Object value) {
    map.put(StringUtils.uncapitalize(interfaceClass.getSimpleName()), value);
  }

  public void put(String name, Object value) {
    map.put(name, value);
  }

  @Override
  public Object findObject(String name) {
    return map.get(name);
  }

}

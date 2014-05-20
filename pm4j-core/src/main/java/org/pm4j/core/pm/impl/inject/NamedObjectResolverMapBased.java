package org.pm4j.core.pm.impl.inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * A named object resolver that is implemented based on a simple name-value map.
 * <p>
 * It may also supports sub-resolvers which will be asked if a named object can't
 * be found in the own map.
 *
 * @author Olaf Boede
 */
public class NamedObjectResolverMapBased implements NamedObjectResolver {

  private Map<String, Object> map = new HashMap<String, Object>();
  private List<NamedObjectResolver> resolvers = new ArrayList<NamedObjectResolver>();

  /**
   * Registers a named object by interface.
   * <p>
   * Uses the uncapitalized simple interface name.
   *
   * @param interfaceClass The interface to register an object (usually a service) for.
   * @param object The object to register.
   * @return the registered object again.
   */
  public <T> T put(Class<?> interfaceClass, T object) {
    map.put(StringUtils.uncapitalize(interfaceClass.getSimpleName()), object);
    return object;
  }

  /**
   * Registers a named object.
   *
   * @param name The object name.
   * @param object The object to register.
   * @return the registered object again.
   */
  public <T> T put(String name, T object) {
    map.put(name, object);
    return object;
  }

  /**
   * Registers a sub-resolver.
   *
   * @param resolver The resolver to add.
   * @return The resolver again for inline usage.
   */
  public <T extends NamedObjectResolver> T addResolver(T resolver) {
    resolvers.add(resolver);
    return resolver;
  }

  /**
   * Finds an object by name.
   * <p>
   * Asks first the own object map. If it is not found in the own map, the sub-resolvers will be asked.
   */
  @Override
  public Object findObject(String name) {
    Object o = map.get(name);
    if (o == null) {
      for (NamedObjectResolver r : resolvers) {
        o = r.findObject(name);
        if (o != null) {
          break;
        }
      }
    }
    return o;
  }

}

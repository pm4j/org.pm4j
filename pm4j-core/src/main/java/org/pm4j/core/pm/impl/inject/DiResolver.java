package org.pm4j.core.pm.impl.inject;

import org.pm4j.core.pm.PmObject;

/**
 * A dependency injection resolver.
 * <p>
 * Implementations resolve dependency injection annotated values for a given object.
 *
 * @author olaf boede
 */
public interface DiResolver {

  /**
   * Resolves dependency injected fields within the given object.
   *
   * @param object the PM object to process.
   */
  void resolveDi(PmObject object);

}

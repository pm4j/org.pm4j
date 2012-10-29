package org.pm4j.core.pm.impl.inject;

public interface DiResolverFactory {
  /**
   * Provides a DI resolver if the provided class has related injections which
   * can be handled by the returned {@link DiResolver}.
   * <p>
   * Returns <code>null</code> if this factory did not find anything that can be
   * injected.
   *
   * @param classToInspect
   *          a class that may contain injection annotations.
   * @return a {@link DiResolver} that can handle the found injections.
   *         <code>null</code> if nothing was found.
   */
  DiResolver makeDiResolver(Class<?> classToInspect);

}

package org.pm4j.core.pm.annotation.customize;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A static API that allows to customize some PM aspects by registering custom
 * annotations and related custom logic handlers.
 * <p>
 * Custom annotation handlers are configured one for the lifetime of the
 * application (Java VM).
 *
 * @author olaf boede
 */
public class PmAnnotationApi {

  /**
   * Registers a custom {@link PermissionAnnotationHandler} for a given custom
   * annotation.
   * <p>
   * The annotation handler logic will be applied <b>before</b> the related
   * <code>isEnabledImpl()</code>... methods get called.
   * <p>
   * This way an application may ensure that common permission rules are
   * considered before screen specific additional logics will be considered.
   *
   * @param annotationType
   *          the custom permission annotation to support.
   * @param handler
   *          the custom permission logic implementation.
   */
  public static <T extends Annotation> void addPermissionAnnotationHandler(Class<T> annotationType,
      PermissionAnnotationHandler<T> handler) {
    assert annotationType != null;
    assert handler != null;

    CustomizedAnnotationUtil.annotationClassToPermissionHandlerMap.put(annotationType, handler);
  }

  /**
   * Provides the set of configured custom permission annotations.
   *
   * @return the set of considered permission annotation types.
   */
  public static Collection<Class<? extends Annotation>> getPermissionAnnotations() {
    return CustomizedAnnotationUtil.annotationClassToPermissionHandlerMap.keySet();
  }

}

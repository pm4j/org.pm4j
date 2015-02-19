package org.pm4j.core.pm.annotation.customize;

import org.pm4j.core.pm.PmObject;

/**
 * Interface for permission related custom PM annotations.
 * <p>
 * To activate a {@link PermissionAnnotationHandler} it needs to be registered by calling
 * {@link PmAnnotationApi#addPermissionAnnotationHandler(Class, PermissionAnnotationHandler)}.
 * <p>
 * The implementations of {@link PmObject#isPmEnabled()},  {@link PmObject#isPmVisible()} and
 * {@link PmObject#isPmReadonly()} call the corresponding methods of registered permission
 * handlers if the PM has a related custom annotation.
 *
 * @author olaf boede
 *
 * @param <T_ANNOTATION> the custom annotation to support.
 */
public interface PermissionAnnotationHandler<T_ANNOTATION> {

  /**
   * Provides the handler specific enablement logic.
   *
   * @param pm the annotated PM.
   * @param annotation the found annotation.
   * @return the handler logic evaluation result.
   */
  boolean isEnabled(PmObject pm, T_ANNOTATION annotation);

  /**
   * Provides the handler specific visiblity logic.
   *
   * @param pm the annotated PM.
   * @param annotation the found annotation.
   * @return the handler logic evaluation result.
   */
  boolean isVisible(PmObject pm, T_ANNOTATION annotation);

  /**
   * Provides the handler specific read-only logic.
   * <p>
   * Please notice that (in difference to enabled and visible) read-only gets effective for
   * a sub-tree of PMs.
   *
   * @param pm the annotated PM.
   * @param annotation the found annotation.
   * @return the handler logic evaluation result.
   */
  boolean isReadonly(PmObject pm, T_ANNOTATION annotation);
}

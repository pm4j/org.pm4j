package org.pm4j.core.pm.annotation.customize;

import java.lang.annotation.Annotation;
import java.util.Collection;

public class PmAnnotationApi {

  public static <T extends Annotation> void addPermissionAnnotationHandler(Class<T> annotationType, PermissionAnnotationHandler<T> handler) {
    assert annotationType != null;
    assert handler != null;

    CustomizedAnnotationUtil.annotationClassToPermissionHandlerMap.put(annotationType, handler);
  }

  /**
   * Provides the set of configured permission annotations.
   *
   * @return the considered permission annotation types.
   */
  public static Collection<Class<? extends Annotation>> getPermissionAnnotations() {
    return CustomizedAnnotationUtil.annotationClassToPermissionHandlerMap.keySet();
  }


}

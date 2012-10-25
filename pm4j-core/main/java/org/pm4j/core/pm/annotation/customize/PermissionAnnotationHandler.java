package org.pm4j.core.pm.annotation.customize;

import org.pm4j.core.pm.PmObject;

public interface PermissionAnnotationHandler<T_ANNOTATION> {

  boolean isEnabled(PmObject pm, T_ANNOTATION annotation);

  boolean isVisible(PmObject pm, T_ANNOTATION annotation);

  boolean isReadonly(PmObject pm, T_ANNOTATION annotation);
}

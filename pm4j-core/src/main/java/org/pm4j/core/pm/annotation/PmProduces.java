package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as a producer of a named object that may be resolved by a
 * {@link PmInject} annotated field (or setter).
 * <p>
 * Limitation for first implementation step:<br>
 * Parent PM's produce for the injection points of their child tree PM's.
 *
 *
 * @author Olaf Boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface PmProduces {

  /**
   * A (optional) name for the shared object.
   * <p>
   * If not provided the name of the annotated field or getter-method name will
   * be used.<br>
   * In case of a method the getter-base name will be used according to the java
   * bean property conventions.
   */
  String name() default "";

}

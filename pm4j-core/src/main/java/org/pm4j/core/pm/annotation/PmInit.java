package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark methods that must be called when the meta data part of a PM is
 * initialized and assigned to the instance.
 * <p>
 * Can be used on multiple methods also on different class hierarchy levels. The
 * methods are processed along the class hierarchy from top to bottom. The call
 * order for methods declared in the same class is random.
 * <p>
 * If an annotated method is overridden in the class hierarchy, it inherits the
 * annotation, but it is also valid to annotate the method in its sub class
 * again.
 *
 * @author SDOLKE
 *
 * @deprecated please override <code>onPmInit()</code> of your PM.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Deprecated
public @interface PmInit {

}

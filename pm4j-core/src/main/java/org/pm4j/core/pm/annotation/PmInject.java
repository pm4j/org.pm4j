package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.inject.NamedObjectResolver;

/**
 * Marks a field to be injected by the PM framework. The target must not be null before injection.
 * <p>
 * Supports reference resolution by
 * <ul>
 * <li>name - looks for a corresponding instance using the {@link NamedObjectResolver}s provided by the {@link PmConversation}</li>
 * <li>parent of type - looks for the nearest parent PM that implements the corresponding type.</li>
 * </ul>
 *
 * @author Olaf Boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD, ElementType.METHOD })
public @interface PmInject {

  /**
   * The set of supported injection modes.
   */
  public enum Mode {
    /**
     * The expression within the attribute 'value' gets used. If that's not
     * configured the name of the annotate property will be used as the name
     * of a named object to find.
     */
    EXPRESSION,
    /**
     * A parent PM that has the field type (interface or class) will be serched.
     */
    PARENT_OF_TYPE
  }

  /**
   * @return the injection mode.
   */
  Mode mode() default Mode.EXPRESSION;

  /**
   * A PM-EL expression that provides the content for the annotated field.<br>
   * If the name is not specified, the name of the annotated field will be used
   * to provide the PM-EL expression.
   * <p>
   * Attention: In difference to PM-EL expressions used in
   * {@link PmObject#findPmProperty(String)}, this expression does not
   * address the field itself. This allows to use injected fields that uses the
   * same name as a variable that provides the injection content.
   */
  String value() default "";

  /**
   * Defines whether <code>null</code> is an accepted value for the injected field.
   * If <code>true</code> and the value to inject is actually true, 
   * the null value will actually be injected 
   * (e.g. setter called with null or field set to null). 
   * <p>
   * Default value: <code>false</code>.
   */
  boolean nullAllowed() default false;
}

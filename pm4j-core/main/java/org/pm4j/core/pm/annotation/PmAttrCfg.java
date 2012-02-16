package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;


/**
 * Name of the field to address.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmAttrCfg {

  /**
   * @return An optional expression that describes how to access the attribute value.
   *         <p>
   *         TODO: document the format and provide an example.
   */
  String valuePath() default "";

  /**
   * @return <code>true</code> when only field with values that are not empty
   *         should be shown.<br>
   *         Default value is <code>false</code>.
   */
  boolean hideWhenEmpty() default false;

  /**
   * @return <code>true</code> when the field has to be filled on each data entry form.
   */
  boolean required() default false;

  /**
   * @return <code>true</code> for attributes that can't be set..
   */
  boolean readOnly() default false;

  /**
   * @return maximal string length.
   */
  int maxLen() default -1;

  /**
   * @return minimal string length.
   */
  int minLen() default 0;

  /**
   * @return The default to be assigned to the attribute when the value of the attribute
   *         is <code>null</code>.
   */
  String defaultValue() default "";

  /**
   * Supports usage of JSR-303 bean validation annotations of a related bean class.
   * <p>
   * If the attribute is simply bound to a corresponding bean attribute having the
   * same name, the JSR-303 annotations of the bound field are considered automatically
   * for the attribute.<br>
   * But if the field is somehow bound to a field of a different bean class (e.g. by value expression
   * or getBackingValue implementation), the framework can't access these definitions.<br>
   * This annotation may be used to provides a reference to a class that defines JSR-303 restrictions.<br>
   * The annotation attribute {@link #beanInfoField()} allows to specify the field with the JSR-303 restriction definition.
   * The {@link #beanInfoField()} definition is not required if the field has the same name as this attribute.
   *
   * @return The class that provides the related JSR-303 validation restrictions.
   */
  Class<?> beanInfoClass() default Void.class;

  /**
   * Allows to specify the name of the field to read JSR-303 validation information from.<br>
   * It is not required to define this if the PM attribute and the related field have the same name.
   * <p>
   * See also: {@link #beanInfoClass()}.
   *
   * @return The name of the related bean field to read JSR-303 information from.
   */
  String beanInfoField() default "";

  /**
   * May be specified alternatively to {@link #defaultValue()}.
   * <p>
   * Denotes a path expression that points to the default value.
   * <p>
   * If used together with {@link #defaultValue()}, first this path will be
   * evaluated. If that results to <code>null</code>, the specified default
   * value will be used.
   *
   * @return A path that will be evaluated using the method
   *         {@link PmObject#findPmProperty(String)}.
   */
  String defaultPath() default "";

  /**
   * @return Key of a format definition used for string conversions.
   */
  String formatResKey() default "";

  /**
   * Defines value change detection on string level.<br>
   * If set to <code>true</code> a difference between the current string representation
   * and the value provided by the UI will be used to detect a value change.<br>
   * This will usually only be done on the native value representation.
   * <p>
   * The main reason for this option are formatted date fields....
   *
   *  is checked if an entered value should be compared against
   * @return
   */
  boolean checkValueChangeOnStringInput() default false;

  /**
   * @return The data access kind definition.
   */
  AttrAccessKind accessKind() default AttrAccessKind.DEFAULT;

  enum AttrAccessKind {
    /**
     * Means for attributes within {@link PmBean} elements:
     * <p>
     * Use reflection when no {@link PmField#valuePath()} is defined.
     * <p>
     * Use xPath when {@link PmField#valuePath()} is defined.
     */
    DEFAULT,

    /**
     * Only the get- and set methods defined in the attribute presentation model
     * class will be used.
     * <p>
     * No reflection or xPath mechanisms will be used to access the attribute value.
     */
    OVERRIDE,

    /**
     * The attribute value is stored in the attribute local storage only.
     */
    LOCALVALUE,

    /**
     * The attribute value is stored in a property of the PM session.<br>
     * Is only useful for attributes that return a unique value with their
     * implementation of {@link PmAttr#getPmLongName()}.
     */
    @Deprecated
    SESSIONPROPERTY
}

}

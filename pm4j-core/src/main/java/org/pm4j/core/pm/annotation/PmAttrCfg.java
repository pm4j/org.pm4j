package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.common.converter.value.ValueConverter;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmDataInput;


/**
 * Name of the field to address.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmAttrCfg {

  /** Attribute value restrictions.
   * @deprecated experimental feature state.
   * */
  @Deprecated
  public static enum Restriction {
    /**
     * The value is required.<br>
     * Has only effect if the attribute is Enabled !
     */
    REQUIRED,
    /**
     * The value is only required if the attribute is visible.<br>
     * Is a useful configuration for form attributes are made visible based on
     * data scenarios.
     */
    REQUIRED_IF_VISIBLE,
    /**
     * The value can't be updated.
     */
    READ_ONLY,
    /**
     * No required or read-only declared by this annotation.<br>
     * But: The result of the methods isRequiredImpl, isPmEnabledImpl, isPmReadOnly etc. may
     * dynamically define specific attribute restrictions.
     */
    NONE
  }

  /**
   * Scenarios for hiding an element.
   * @deprecated Please use {@link PmObjectCfg#visible()}.
   */
  public static enum HideIf {
    /**
     * The value is empty.
     */
    EMPTY_VALUE,
    /**
     * The value equals the default value.
     */
    DEFAULT_VALUE
  }

  /**
   * @return An optional expression that describes how to access the attribute value.
   * @see {@link https://github.com/pm4j/org.pm4j/wiki/Resolving-path-expressions}
   */
  String valuePath() default "";

  /**
   * @return <code>true</code> when only field with values that are not empty
   *         should be shown.<br>
   *         Default value is <code>false</code>.
   *
   * @deprecated Use {@link #hideIf()}
   */
  @Deprecated
  boolean hideWhenEmpty() default false;

  /**
   * @return List of attribute value scenarios where to hide.
   * @deprecated Please use {@link PmObjectCfg#visible()}
   */
  @Deprecated
  HideIf[] hideIf() default {};


  /**
   * Defines which condition makes the attribute value required.
   * <p>
   * Will replace {@link #required()} and {@link #readOnly()} in future.
   *
   * @return
   */
  Restriction valueRestriction() default Restriction.NONE;

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
   * Allows to define additional validation tigger times.
   *
   * @return the validation strategy definition for this attribute.
   */
  Validate validate() default Validate.ON_VALIDATE_CALL;

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
   * @return Key of a format definition used for string conversions.
   */
  String formatResKey() default "";

  /**
   * @deprecated unused
   */
  @Deprecated
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

  /**
   * Configures the value converter used to convert between external and backing attribute values.
   *
   * @return The converter class to use.<br>
   *         <b>Important:</b> It needs to have a public default constructor.
   */
  @SuppressWarnings("rawtypes")
  Class<? extends ValueConverter> valueConverter() default ValueConverter.class;

  /**
   * Defines attribute validation strategies.<br>
   * Each attribute gets validated when {@link PmDataInput#pmValidate()} gets called.
   * But the strategies {@link #BEFORE_SET} and {@link #AFTER_SET} allow to define additional
   * validation triggers.
   */
  public enum Validate {

    /**
     * Validates the attribute only if {@link PmDataInput#pmValidate()} gets
     * called. This happens usually when a validating {@link PmCommand} gets
     * executed.
     * <p>
     * The validation strategy allows to set invalid attribute values until the
     * user finally triggers an action that validates if all entered values are
     * OK.<br>
     * This strategy is very useful if the validation rules considering more
     * than a single attribute value (E.g. 'A' must be greater that 'B').
     * <p>
     * This is the default validation strategy.
     */
    ON_VALIDATE_CALL,

    /**
     * Validates the received value before setting it to the attribute.
     * <p>
     * The method <code>PmAttrImpl.validate(value)</code> gets called.<br>
     * This validation is limited, because it can't use bean validation and some
     * logic that may be implemented in <code>pmValidate()</code>.
     */
    BEFORE_SET,

    /**
     * Validates the attribute after setting it.<br>
     * That means the validation takes place after changing the backing value.
     * <p>
     * This allows to use (nearly) the complete validation functionality (incl. bean validation).<br>
     * But there are restrictions to be considered:<br>
     * If the validation logic compares to other attribute values it may happen that the corresponding
     * attribute values are not yet set. Consider using the default validation strategy {@link #ON_VALIDATE}
     * in that case.
     * <p>
     * The validation is applied and before
     * <ul>
     *  <li>the afterValueChange() method call,</li>
     *  <li>the after-do method calls to registered value change decorators and</li>
     *  <li>before sending the value change event to registered listeners.</li>
     * </ul>
     * This way it is possible to check the validation result within these post processing steps.
     */
    AFTER_SET
  }

}

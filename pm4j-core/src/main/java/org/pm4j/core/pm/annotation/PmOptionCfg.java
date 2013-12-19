package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.impl.PmAttrBase;

/**
 * Specification of an attribute option set.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
public @interface PmOptionCfg {

  /**
   * Specification of an EL expression that defines the set of objects to build
   * the options from.
   * <p
   * If not specified, the method {@link PmAttrBase#getOptionValues()} will
   * be used to get the option objects.
   *
   * @return Path to the set of objects that is used to build the options.<br>
   *         Example: 'pmBean.allDefinedValues'.
   */
  String values() default NOT_SPECIFIED;

  /**
   * Expression for the path from an item object to its value that provides the option identifier.<br>
   * Example: 'id'.
   *
   * @return The id value expression.
   */
  String id() default "this";

  /**
   * Path from an item object to the value that provides the option title.<br>
   * Example: 'name'.
   *
   * @return The title value expression.
   */
  String title() default "this";

  /**
   * Path from an item object to the value that provides the backing value from the option bean.<br>
   * The specification of {@link #value()} and {@link #backingValue()} allows to define an automatic value
   * converter mechanism.<br>
   * Examples:<br>
   *  '' - specifies the found object itself;<br>
   *  'mySubfield.anotherField' - specifies some related content to use.
   *
   * @return The value expression that resolves from the option bean to the backing value.
   */
  String backingValue() default "this";

  /**
   * Path from an item object to the value that provides the option value.<br>
   * Examples:<br>
   *  '' - specifies the found object itself;<br>
   *  'mySubfield.anotherField' - specifies some related content to use.
   *
   * @return The value expression.
   */
  String value() default NOT_SPECIFIED;

  /**
   * An expression that defines a value to sort the options by.<br>
   * In not specified, the options will be shown as they are
   * provides by the getOptionValues method.
   * <p>
   * Sorts specific to the type of the object returned by the
   * <code>sortBy</code> expression:
   * <p>
   * Examples: Assume <code>getOptionValues()</code> provides a set of <code>User</code> objects.
   * <ul>
   *  <li><code>sortBy=""</code> - sorts by comparing the users.</li>
   *  <li><code>sortBy="title"</code> - sorts by comparing the title of the option.</li>
   *  <li><code>sortBy="id"</code> - sorts by comparing the id of the option.</li>
   *  <li><code>sortBy="value.name"</code> - sorts by comparing the 'name' attribute of the user object behind the option.</li>
   *  <li><code>sortBy="value.name desc"</code> - sorts descending by comparing the 'name' attribute of the users.</li>
   *  <li><code>sortBy="value.address.street"</code> - sorts by comparing the street which is part of address of the provided users.</li>
   *  <li><code>sortBy="value.name desc, value.address.street"</code> - sorts by comparing the street which is part of address of the provided users.</li>
   * </pre>
   *
   * @return The expression to sort the option by.
   */
  String sortBy() default NOT_SPECIFIED;

  /**
   * Defines if a <code>null</code> option should be generated or not.
   */
  static enum NullOption {
    /**
     * A <code>null</code>-option will be provided if the attribute value is
     * optional ({@link PmAttr#isRequired()} returns <code>false</code>).
     */
    FOR_OPTIONAL_ATTR,

    /**
     * A <code>null</code>-option will always be provided.
     */
    YES,

    /**
     * A <code>null</code>-option will not be provided.
     */
    NO,

    /**
     * Defines that the attribute type specific <code>null</code>-option default
     * definition should be used.<br>
     * The default for non-list attribute types is {@link #FOR_OPTIONAL_ATTR}.<br>
     * the default for list attribute types is {@link #NO}.
     * <p>
     * You may define the attribute specific default value by overriding
     * {@link PmAttrImplIf#getNullOptionDefault()}.
     */
    DEFAULT
  };

  /**
   * Defines if a <code>null</code>-option should be generated.
   *
   * @return The <code>null</code>-option definition.
   */
  NullOption nullOption() default NullOption.DEFAULT;

  /**
   * This resource key allows provide a default title for the <code>null</code>
   * option of all option sets.
   * <p>
   * The default may be defined for a specific domain package or for the whole
   * application if it is defined within the topmost package of the application.
   */
  static final String NULL_OPTION_DEFAULT_RESKEY = "nullOptionTitle";

  /**
   * @return The resource key for the title to display for the <code>null</code>
   *         option.
   */
  String nullOptionResKey() default NULL_OPTION_DEFAULT_RESKEY;

  /** Internal identifier string for no sort-by definition. */
  static final String NOT_SPECIFIED = "- Not specified. -";
}


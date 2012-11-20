package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.title.PmTitleProvider;

/**
 * Title definition annotation.
 * <p>
 * Usage:<br>
 * When no {@link PmTitleCfg} annotation or an annotation without any attribute is specified,
 * the title for the presentation model will be based on the resource key {@link PmObject#getPmResKey()} and
 * the provided resource strings found in property files.
 * <p>
 * When only the attribute {@link #attrValue()} is specified, then the value of the
 * attribute with the given name will be used.
 * <p>
 * When the attribute {@link #titleProvider()} is specified, then an instance of the
 * specified implementation of {@link PmTitleProvider} will be used.
 * <p>
 * TODOC olaf: Add the added semantic for fix key and fix title value specification.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmTitleCfg {

  /**
   * Name of an attribute that should be used to get the title value.
   * <p>
   * Example: The value of the user name should be used as title value for user objects.
   * To get that, specify that attribute name ('name') in this field.
   *
   * @return Name of an attribute that should be used for the title.
   */
  String attrValue() default "";

  /**
   * When the default title provider does not match your requirements, you may specify
   * here one that supports your special use case.
   *
   * @return The title provider to be used for this presententation model.
   */
  Class<?> titleProvider() default Void.class;

  /**
   * Specifies a fix resource key to be used for the title, tooltip and
   * icon of this PM.
   * <p>
   * This value has no influence on the resource keys of the children of
   * this Element. Use {@link #resKeyBase()} to define a key that will
   * also be used by the PM composite children.<br>
   * If both, {@link #resKey()} and {@link #resKeyBase()} are specified,
   * the {@link #resKeyBase()} definition will be applied to the composite
   * children and {@link #resKey()} will be applied to the title etc.
   * of this PM.
   * <p>
   * The system constructs PM resource keys usually based on the name of
   * elements and their composite children.<br>
   * Example key 'userPm.password'. <br>
   * See also: {@link PmObject#getPmResKey()}.
   * <p>
   * In some cases it might be useful to have resource keys that are manually
   * specified. If you specify that key here, the default key will be ignored
   * and the specified one will be used.
   *
   * @return The manually specified resource key.
   */
  String resKey() default "";

  /**
   * Specifies a fix resource key, used for this PM and as prefix part of
   * the resource key of the composite children.
   * <p>
   * For more information see {@link #resKey()}.
   *
   * @return The resource key for this PM, used also as prefix part of the composite children.
   */
  String resKeyBase() default "";

  /**
   * @return <code>true</code> when the title should be use as toolTip too.
   */
  boolean tooltipUsesTitle() default false;

  /**
   * Specifies a fix title value to be used.<br>
   * Useful for single language applications that define resources within the Java code.
   * <p>
   * Will only be used when {@link #attrValue()}, {@link #resKey()} and {@link #titleProvider()}
   * are not specified.
   *
   * @return A fix title value specification.
   */
  String title() default "";

  /**
   * Specifies a fix tooltip value to be used.<br>
   * Useful for single language applications that define resources within the Java code.
   * <p>
   * Will only be used when {@link #attrValue()}, {@link #resKey()} and {@link #titleProvider()}
   * are not specified.
   *
   * @return A fix tooltip value specification.
   */
  String tooltip() default "";

  /**
   * Specifies a fix icon value to be used.<br>
   * Useful for single language applications that define resources within the Java code.
   * <p>
   * Will only be used when {@link #attrValue()}, {@link #resKey()} and {@link #titleProvider()}
   * are not specified.
   *
   * @return A fix icon value specification.
   */
  String icon() default "";

}

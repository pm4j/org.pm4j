package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.api.PmExpressionApi;

/**
 * Bean presentation model attributes.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmBeanCfg {

  /**
   * The default attribute name, used to get the the bean identity.
   */
  public static final String DEFAULT_BEAN_ID_ATTR = "id";

  /**
   * The bean class parameter is not mandatory anymore, since each class 
   * implementing {@link #org.pm4j.core.pm.PmBean} declares the bean class by 
   * its generic parameter.
   * 
   * @return The type of bean that can be handled by this presentation model.
   */
  Class<?> beanClass() default Void.class;

  /**
   * The PM may automatically create a backing bean instance in case it is
   * <code>null</code>. <br>
   * The type of the bean to create may be specified by {@link #beanClass()}.
   * <p>
   * This feature saves some code for pages that create and edit new bean
   * instances.
   *
   * @return <code>true</code> if auto create will happen in case of a found
   *         bean value of <code>null</code>.
   */
  boolean autoCreateBean() default false;

  /**
   * An expression to find the bean to be used behind this PM.
   * <p>
   * The default value is an empty string. It defines no expression will be used
   * to find an associated bean.
   * <p>
   * See {@link PmExpressionApi} for more information about PM expressions.
   *
   * @return An expression that addresses the object to use.<br>
   *         Default value is an empty string.
   */
  String findBeanExpr() default "";

  /**
   * The identifier attribute(s) of the bean behind the PM.
   *
   * @return A comma separated list of attribute names that identifies this instance.
   */
  String key() default DEFAULT_BEAN_ID_ATTR;

  /**
   * @return <code>true</code> for PMs with attribute values that shouldn't be changed.
   */
  boolean readOnly() default false;
}

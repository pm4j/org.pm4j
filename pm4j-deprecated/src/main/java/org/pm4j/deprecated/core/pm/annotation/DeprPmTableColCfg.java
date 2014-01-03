package org.pm4j.deprecated.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.deprecated.core.pm.DeprPmTableCol;

/**
 * Annotation configuration for {@link DeprPmTableCol} instances.
 *
 * @author olaf boede
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface DeprPmTableColCfg {

  /**
   * @return The preferred column size value.
   */
  String prefSize() default "";

  /**
   * @return The minimal column size value.
   */
  String minSize() default "";

  /**
   * @return The maximal column size value.
   */
  String maxSize() default "";

  /**
   * @return <code>TRUE</code> if the column may be sorted.<br>
   *         <code>FALSE</code> if the column is not sortable.<br>
   *         <code>UNDEFINED</code> if the table default setting should be applied.
   *         If no table default is defined, the column is not sortable.
   */
  PmBoolean sortable() default PmBoolean.UNDEFINED;

  /**
   * The filter definition(s) used for this column.
   * <p>
   * The provided filter class(es) need to have a constructor signature with a single PM
   * parameter. The column PM will be passed as constructor argument.
   *
   * @return The column filter definitions.
   */
  DeprFilterByCfg[] filterBy() default {};
}

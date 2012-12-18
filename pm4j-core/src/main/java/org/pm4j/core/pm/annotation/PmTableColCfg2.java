package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.PmTableCol2;

/**
 * Annotation configuration for {@link PmTableCol2} instances.
 * It has only effect for in-memory tables! Service base tables configure their
 * sort and filter options within their service.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface PmTableColCfg2 {

  /**
   * @return <code>TRUE</code> if the column may be sorted.<br>
   *         <code>FALSE</code> if the column is not sortable.<br>
   *         <code>UNDEFINED</code> if the table default setting should be applied.
   *         If no table default is defined, the column is not sortable.
   */
  PmBoolean sortable() default PmBoolean.UNDEFINED;

  /**
   * The filter definition used for this column.
   *
   * @return The column filter definitions.
   */
  Class<?> filterType() default Void.class;

}

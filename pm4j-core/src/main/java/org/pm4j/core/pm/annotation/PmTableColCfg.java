package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.joda.time.LocalDate;
import org.pm4j.core.pm.PmTableCol;

/**
 * Annotation configuration for {@link PmTableCol} instances.
 * It has only effect for in-memory tables! Service base tables configure their
 * sort and filter options within their service.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface PmTableColCfg {

  /**
   * @return <code>TRUE</code> if the column may be sorted.<br>
   *         <code>FALSE</code> if the column is not sortable.<br>
   *         <code>UNDEFINED</code> if the table default setting should be applied.
   *         If no table default is defined, the column is not sortable.
   */
  PmBoolean sortable() default PmBoolean.UNDEFINED;

  /**
   * This type specification defines the kind of UI control offered to the user
   * to enter the filter-by compare value for this column. It also affects the set
   * type specific compare operators.<br>
   * This is usually value type like {@link String} or {@link LocalDate}.
   *
   * @return The value type the user can filter by.
   */
  Class<?> filterType() default Void.class;

}

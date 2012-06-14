package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTable.RowSelectMode;
import org.pm4j.core.pm.impl.PmTableImpl;

/**
 * Annotation configuration for {@link PmTable} instances.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface PmTableCfg {

  /**
   * An optional default setting for column sortability.
   *
   * @return <code>TRUE</code> if the column may be sorted by default.<br>
   *         <code>FALSE</code> or <code>UNDEFINED</code> if the column is not sortable by default.
   */
  PmBoolean sortable() default PmBoolean.UNDEFINED;

  /**
   * Defines the (optional) default sort column.
   * <p>
   * Format: column-name
   *
   * @return
   */
  String defaultSortCol() default "";

  /**
   * Defines the {@link RowSelectMode} to use.
   * <p>
   * This definition may be overridden by a call to {@link PmTableImpl#setRowSelectMode(RowSelectMode)}.
   *
   * @return The row selection mode.
   */
  RowSelectMode rowSelectMode() default RowSelectMode.DEFAULT;

  /**
   * Defines the maximum number of rows per page.
   * <p>
   * This definition may be overridden by a call to {@link PmTableImpl#setNumOfPageRows(Integer)}.
   *
   * @return The maximum number of rows per page. Only a positive value has an effect on the table.
   */
  int numOfPageRows() default 0;

}

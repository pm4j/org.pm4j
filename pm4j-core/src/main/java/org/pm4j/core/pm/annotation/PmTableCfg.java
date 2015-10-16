package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;

import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.querybased.QueryService;
import org.pm4j.common.selection.SelectMode;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.api.PmExpressionApi;
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
   * May be used to specify an expression that provides a collection of beans to show in this table.
   * <p>
   * The expression evaluation starts at the parent PM of the table.<br>
   * An example for a table that is used within a containing <code>PmBean</code>:
   * <pre>beanCollectionPath="pmBean.aSubBean.theCollectionToUse"</pre>
   * <p>
   * For more information regarding expressions see: {@link PmExpressionApi}
   * <p>
   * If you leave this annotation undefined, the expression "(o)pmBean.<myTablePmFieldName>". This addresses
   * a collection within the backing bean having the same field name.
   * <p>
   * If you need more flexibility: Please override <code>PmTableImpl2#getPmBeansImpl()</code>.
   * <p>
   * Alternatively - e.g. for pageable query service based tables - you use a specific {@link PageableCollection}
   * behind your table. Then this <code>valuePath</code> is irrelevant.
   *
   * @return an expression string.
   */
  String valuePath() default "";

  /**
   * @return The service to use. The corresponding instance will be found via ServiceLocator.
   */
  @SuppressWarnings("rawtypes")
  Class<? extends QueryService> queryServiceClass() default QueryService.class;


  /**
   * An optional default setting for column sortability.
   * The column may override this default setting.
   *
   * @return <code>true</code> if the columns are sortable by default.<br>
   *         <code>false</code> if the columns is not sortable by default.
   */
  boolean sortable() default false;

  /**
   * Defines the (optional) default sort column.
   * <ul>
   *  <li>Sort by a column using the default 'asc' sort order:  <code>defaultSortCol="myColumnName"</code> </li>
   *  <li>Sort by a column with an explicitly defined sort order:  <code>defaultSortCol="myColumnName,desc"</code> </li>
   * </ul>
   *
   * @return
   * @deprecated Use initialSortCols instead
   */
  @Deprecated
  String initialSortCol() default "";
  
  /**
   * Defines the (optional) default sort columns.
   * <ul>
   *  <li>Sort by a column using the default 'asc' sort order:  <code>initialSortCols="column1"</code> </li>
   *  <li>Sort by a column with an explicitly defined sort order:  <code>initialSortCols="column1 desc"</code> </li>
   *  <li>Sort by multiple columns with different sort order:  <code>initialSortCols="column1 desc, column2, column3 desc"</code> </li>
   * </ul>
   *
   * @return
   */
  String initialSortCols() default "";

  /**
   * Defines a bean comparator that provides the initial table sort order.
   * <p>
   * In difference to {@link #initialSortCol()} this initial sort order gets not reflected
   * within the {@link PmTableCol#getSortOrderAttr()}.
   * It just defines the initial 'unsorted' state of the table items.
   *
   * @return The defined comparator.
   */
  Class<?> initialSortComparator() default Comparator.class;

  /**
   * Defines the {@link SelectMode} to use.
   * <p>
   * This definition may be overridden by a call to {@link PmTableImpl#setPmRowSelectMode(SelectMode)}.
   *
   * @return The row selection mode.
   */
  SelectMode rowSelectMode() default SelectMode.DEFAULT;

  /**
   * Defines the maximum number of rows per page.
   * <p>
   * This definition may be overridden by a call to {@link PmTableImpl#setNumOfPageRowPms(Integer)}.
   * <p>
   * TODO oboede: add a default value to PmDefaults or find another application specific solution.
   *
   * @return The maximum number of rows per page. Only a positive value has an effect on the table.
   */
  int numOfPageRows() default 0;

  // TODO oboede: will be configurable in a future release.
  /** The supported row validation sets. */
  public enum RowsToValidate {
    ALL, UPDATED, ADDED, CURRENT_PAGE
  }

}


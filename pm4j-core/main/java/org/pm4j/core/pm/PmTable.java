package org.pm4j.core.pm;

import java.util.List;

import org.pm4j.core.pm.pageable.PmPager;

public interface PmTable<T_ROW_OBJ> extends PmObject, PmDataInput {

  List<PmTableCol> getColumns();

  List<T_ROW_OBJ> getRows();

  List<PmTableGenericRow<T_ROW_OBJ>> getGenericRows();

  /**
   * @return The total number of rows to display within this table.
   */
  int getRowNum();

  public static interface WithPager<T_ROW_ELEMENT> extends PmTable<T_ROW_ELEMENT>{
    PmPager<T_ROW_ELEMENT> getPager();
  }

}

package org.pm4j.deprecated.core.pm.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.deprecated.core.pm.DeprPmTable;
import org.pm4j.deprecated.core.pm.DeprPmTableCol;
import org.pm4j.deprecated.core.pm.DeprPmTableGenericRow;

import java.util.ArrayList;
import java.util.List;

/**
 * A table row implementation that is specific for {@link DeprecatedPmTableOfPmElementsImpl}.
 * <p>
 * The table cells are provided as follows:<br>
 * A row represents a {@link PmElement}.<br>
 * For each column cell, the element will be asked for a child PM that has the
 * same name as the table column.
 *
 * @author olaf boede
 * @deprecated please use {@link PmTableGenericRowImpl2}
 */
@Deprecated
public class DeprPmTableGenericRowImpl<T_ROW_ELEMENT extends PmElement> implements DeprPmTableGenericRow<T_ROW_ELEMENT> {

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(DeprPmTableGenericRowImpl.class);

  private final DeprPmTable<T_ROW_ELEMENT> pmTable;
  private final T_ROW_ELEMENT rowElement;
  private List<PmObject> items;

  public DeprPmTableGenericRowImpl(DeprPmTable<T_ROW_ELEMENT> pmTable, T_ROW_ELEMENT pmElement) {
    assert pmTable != null;
    assert pmElement != null;

    this.pmTable = pmTable;
    this.rowElement = pmElement;
  }

  @Override
  public DeprPmTable<T_ROW_ELEMENT> getPmTable() {
    return pmTable;
  }

  @Override
  public List<PmObject> getCells() {
    if (items == null) {
      List<PmObject> items = new ArrayList<PmObject>();
      for (DeprPmTableCol c : pmTable.getColumns()) {
        String colName = c.getPmName();
        PmObject pm = PmUtil.findChildPm(rowElement, colName);
        if (pm != null) {
          items.add(pm);
        }
        else {
          // Add an empty dummy label that is only referenced by the list of items.
          // This way it is only bound to the life time of this row.
          items.add(new PmObjectBase(pmTable));
        }
      }
      this.items = items;
    }

    return items;
  }

  @Override
  public PmObject getCell(int colIdx) {
    List<PmObject> cells = getCells();

    if (cells.size() <= colIdx) {
      throw new PmRuntimeException(pmTable, "Invalid column index '" + colIdx
          + "'. Only " + cells.size() + " columns are configured.");
    }

    return cells.get(colIdx);
  }

  @Override
  public T_ROW_ELEMENT getBackingBean() {
    return rowElement;
  }

}

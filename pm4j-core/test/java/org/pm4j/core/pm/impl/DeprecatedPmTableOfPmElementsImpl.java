package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmSortOrder;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.PmTableGenericRow;
import org.pm4j.core.pm.PmVisitor;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmValidationApi;
import org.pm4j.core.pm.filter.Filter;
import org.pm4j.core.pm.filter.FilterByDefinition;

/**
 * @deprecated Use {@link PmTableImpl} now.
 */
@Deprecated
public class DeprecatedPmTableOfPmElementsImpl<T_ROW_ELEMENT_PM extends PmElement> extends PmDataInputBase implements PmTable<T_ROW_ELEMENT_PM> {

  /** The content this table is based on. */
  /* package */ List<PmTableGenericRow<T_ROW_ELEMENT_PM>> rows = null;

  /** Container for the sort specification and the sorted items. */
  private SortOrderSelection sortOrderSelection;

  /**
   * @param pmParent The presentation model context for this table.
   */
  public DeprecatedPmTableOfPmElementsImpl(PmObject pmParent) {
    this(pmParent, null);
  }

  /**
   * @param pmParent
   *          The presentation model context for this table.
   * @param rowElements
   *          The data set to display as table rows.
   */
  public DeprecatedPmTableOfPmElementsImpl(PmObject pmParent, Collection<? extends T_ROW_ELEMENT_PM> rowElements) {
    super(pmParent);
    _makeRowsAndFireValueChange(rowElements);
  }

  @Override
  public void accept(PmVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public List<PmTableCol> getColumns() {
    // Lazy selection and column position setup.
    // Reason: Can't access child PMs onPmInit. A second PM initialization phase could solve this issue.
    // XXX: May get easier with separated UI control settings PM session.
    if (this.sortOrderSelection == null) {
      List<PmTableCol> cols = zz_getPmColumns();
      for (int i=0; i<cols.size(); ++i) {
        cols.get(i).getColPosAttr().setValue(i);
      }
      this.sortOrderSelection = new SortOrderSelection(cols);
      return cols;
    }
    else {
      return zz_getPmColumns();
    }
  }

  @Override
  public List<PmTableGenericRow<T_ROW_ELEMENT_PM>> getGenericRows() {
    return ensureSortedRows(getRowsImpl());
  }

  @Override
  public List<T_ROW_ELEMENT_PM> getRows() {
    ensureSortedRows(getRowsImpl());
    return sortOrderSelection.sortedRowPms;
  }

  @Override
  public int getTotalNumOfRows() {
    return getRowsImpl().size();
  }

  protected List<PmTableGenericRow<T_ROW_ELEMENT_PM>> getRowsImpl() {
    if (rows == null) {
      Collection<T_ROW_ELEMENT_PM> rowElements = getRowElementsImpl();

      if (rowElements == null || rowElements.isEmpty()) {
        rows = new ArrayList<PmTableGenericRow<T_ROW_ELEMENT_PM>>();
      }
      else {
        _makeRowsAndFireValueChange(rowElements);
      }

    }
    return rows;
  }

  /**
   * @return The set of PMs to be represented as a table row.
   */
  protected Collection<T_ROW_ELEMENT_PM> getRowElementsImpl() {
    return Collections.emptyList();
  }

  @Override
  public void clearPmInvalidValues() {
    for (PmTableGenericRow<T_ROW_ELEMENT_PM> r : getRowsImpl()) {
      T_ROW_ELEMENT_PM obj = r.getBackingBean();
      if (obj instanceof PmObject) {
        PmValidationApi.clearInvalidValuesOfSubtree((PmObject)obj);
      }
    }
  }

  @Override
  protected void onPmInit() {
// FIXME olaf: initialization order problem:

//    List<PmTableCol> cols = getColumns();
//    for (int i=0; i<cols.size(); ++i) {
//      cols.get(i).getColPosAttr().setValue(i);
//    }
//    this.sortOrderSelectionListener = new SortOrderSelection();
  }


  public void setPmRowElements(Collection<? extends T_ROW_ELEMENT_PM> rowElements) {
    _makeRowsAndFireValueChange(rowElements);
  }

  public PmTableGenericRow<T_ROW_ELEMENT_PM> addRow(T_ROW_ELEMENT_PM rowElementPm) {
    PmTableGenericRow<T_ROW_ELEMENT_PM> rowPm = new PmTableGenericRowImpl<T_ROW_ELEMENT_PM>(this, rowElementPm);
    getRowsImpl().add(rowPm);
    _invalidateSortedRows();
    return rowPm;
  }

  // -- row sort order support --

  class SortOrderSelection implements PmEventListener
  {
    /** The column to sort the rows by. */
    PmTableCol sortCol = null;

    /** The current sort order. */
    List<PmTableGenericRow<T_ROW_ELEMENT_PM>> sortedRows = null;
    List<T_ROW_ELEMENT_PM> sortedRowPms = null;

    public SortOrderSelection(Collection<PmTableCol> columns) {
      for (PmTableCol c : columns) {
        PmAttrEnum<PmSortOrder> a = c.getSortOrderAttr();

        // Whenever a the sort order attribute of a column changes, the sorted row
        // set needs to be updated.
        PmEventApi.addPmEventListener(a, PmEvent.VALUE_CHANGE, this);

        // Identify the initial sort order (if there is one).
        if (a.getValue() != PmSortOrder.NEUTRAL) {
          sortCol = c;
        }
      }
    }

    @Override
    public void handleEvent(PmEvent event) {
      @SuppressWarnings("unchecked")
      PmAttrEnum<PmSortOrder> a = (PmAttrEnum<PmSortOrder>)event.pm;
      sortCol = a.getValue() != PmSortOrder.NEUTRAL
                  ? (PmTableCol) a.getPmParent()
                  : null;

      // mark current sort order as invalid and fire a value change event.
      _invalidateSortedRows();
      PmEventApi.firePmEvent(DeprecatedPmTableOfPmElementsImpl.this, PmEvent.VALUE_CHANGE);
    }

    private void assignSortedRows(List<PmTableGenericRow<T_ROW_ELEMENT_PM>> rows) {
      List<T_ROW_ELEMENT_PM> sortedPms = new ArrayList<T_ROW_ELEMENT_PM>(rows.size());
      for (PmTableGenericRow<T_ROW_ELEMENT_PM> r : rows) {
        sortedPms.add(r.getBackingBean());
      }
      this.sortedRows = rows;
      this.sortedRowPms = sortedPms;
    }

  }

  /**
   * Sorts the rows according to the result of the compare functionality of
   * cell-PMs of the selected sort column.
   */
  private List<PmTableGenericRow<T_ROW_ELEMENT_PM>> ensureSortedRows(List<PmTableGenericRow<T_ROW_ELEMENT_PM>> rows) {
    if (sortOrderSelection == null) {
      // generates the sort order container.
      getColumns();
    }

    List<PmTableGenericRow<T_ROW_ELEMENT_PM>> existingList = sortOrderSelection.sortedRows;
    if (existingList == null) {
      if (sortOrderSelection.sortCol != null) {
        // Assign a new list after sorting to prevent concurrent modification problems.
        ArrayList<PmTableGenericRow<T_ROW_ELEMENT_PM>> tempList = new ArrayList<PmTableGenericRow<T_ROW_ELEMENT_PM>>(rows);
        Collections.sort(tempList, new RowComparator(sortOrderSelection));
        sortOrderSelection.assignSortedRows(tempList);

        return sortOrderSelection.sortedRows;
      }
      else {
        sortOrderSelection.assignSortedRows(new ArrayList<PmTableGenericRow<T_ROW_ELEMENT_PM>>(rows));
        return sortOrderSelection.sortedRows;
      }
    }
    else {
      return existingList;
    }
  }


  private class RowComparator implements Comparator<PmTableGenericRow<T_ROW_ELEMENT_PM>> {
    private final int colIdx;
    private final PmSortOrder order;

    public RowComparator(SortOrderSelection sortOrderSelection) {
      this.colIdx = sortOrderSelection.sortCol.getColPosAttr().getValue();
      this.order = sortOrderSelection.sortCol.getSortOrderAttr().getValue();
    }

    @Override
    public int compare(PmTableGenericRow<T_ROW_ELEMENT_PM> r1, PmTableGenericRow<T_ROW_ELEMENT_PM> r2) {
      PmObject cell1 = r1.getCell(colIdx);
      PmObject cell2 = r2.getCell(colIdx);
      switch (order) {
        case ASC:  return cell1.compareTo(cell2);
        case DESC: return - cell1.compareTo(cell2);
        default:   return 0;
      }
    }
  }

  private void _invalidateSortedRows() {
    if (sortOrderSelection != null) {
      sortOrderSelection.sortedRows = null;
    }
  }

  // -- helper methods --

  private void _makeRowsAndFireValueChange(Collection<? extends T_ROW_ELEMENT_PM> rowElements) {
    if (rowElements == null || rowElements.isEmpty()) {
      // create on getRowsImpl() call.
      rows = null;
    }
    else {
      rows = new ArrayList<PmTableGenericRow<T_ROW_ELEMENT_PM>>(rowElements.size());
      for (T_ROW_ELEMENT_PM e : rowElements) {
        rows.add(new PmTableGenericRowImpl<T_ROW_ELEMENT_PM>(this, e));
      }
    }

    _invalidateSortedRows();

    PmEventApi.firePmEvent(this, PmEvent.VALUE_CHANGE);
  }

  @Override
  public org.pm4j.core.pm.PmTable.RowSelectMode getRowSelectMode() {
    // TODO Auto-generated method stub
    return RowSelectMode.SINGLE;
  }

  @Override
  public int getNumOfPageRows() {
    // TODO Auto-generated method stub
    return 10;
  }

  @Override
  protected MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  public List<T_ROW_ELEMENT_PM> getRowsWithChanges() {
    // TODO Auto-generated method stub
    return Collections.emptyList();
  }

  @Override
  public T_ROW_ELEMENT_PM getSelectedRow() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<T_ROW_ELEMENT_PM> getSelectedRows() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addDecorator(PmCommandDecorator decorator, org.pm4j.core.pm.PmTable.TableChange... changes) {
    // TODO Auto-generated method stub

  }

  @Override
  public Collection<PmCommandDecorator> getDecorators(org.pm4j.core.pm.PmTable.TableChange change) {
    // TODO Auto-generated method stub
    return Collections.emptyList();
  }

  @Override
  public List<FilterByDefinition> getFilterByDefinitions() {
    // TODO Auto-generated method stub
    return Collections.emptyList();
  }

  @Override
  public boolean setFilter(String filterId, Filter filter) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Filter getFilter(String filterId) {
    // TODO Auto-generated method stub
    return null;
  }

}

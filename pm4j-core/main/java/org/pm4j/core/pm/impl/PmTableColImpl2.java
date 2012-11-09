package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.SortOrder;
import org.pm4j.core.exception.PmResourceData;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEvent.ValueChangeKind;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmSortOrder;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTable2;
import org.pm4j.core.pm.PmTableCol2;
import org.pm4j.core.pm.PmVisitor;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.annotation.PmTableColCfg;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.util.table.ColSizeSpec;

/**
 * Implements the table column PM behavior.
 *
 * @author olaf boede
 */
public class PmTableColImpl2 extends PmObjectBase implements PmTableCol2 {

  public final PmAttrEnum<PmSortOrder> defaultSortOrderAttr = new SortOrderAttr(this);
  public final PmCommand defaultCmdSort = new CmdSortPm();
  private PmSortOrder sortOrder = PmSortOrder.NEUTRAL;

  /**
   * @param pmTable
   *          The table containing this column.
   */
  public PmTableColImpl2(PmTable2<?> pmTable) {
    super(pmTable);
  }

  @Override
  public ColSizeSpec getPmColSize() {
    return getOwnMetaData().colSizeSpec;
  }

  @Override
  public PmAttrEnum<PmSortOrder> getSortOrderAttr() {
    return defaultSortOrderAttr;
  }

  @Override
  public PmCommand getCmdSort() {
    return defaultCmdSort;
  }

  /**
   * Creates the PM for the <code>sortOrderAttr</code> attribute.<br>
   * Concrete subclasses may define here their specific sortOrderAttr PM.
   *
   * @return The <code>sortOrderAttr</code> attribute to use.<br>
   *         Should be not visible if this column is not sortable.
   */
  protected PmAttrEnum<PmSortOrder> makeSortOrderAttr() {
    return new SortOrderAttr(this);
  }

  protected PmCommand makeCmdSort() {
    return new CmdSortPm();
  }

  @PmCommandCfg(beforeDo=BEFORE_DO.DO_NOTHING)
  protected class CmdSortPm extends PmCommandImpl {

    public CmdSortPm() {
      super(PmTableColImpl2.this);
    }

    @Override
    protected boolean isPmEnabledImpl() {
      return getSortOrderAttr().isPmEnabled();
    }

    @Override
    protected boolean isPmVisibleImpl() {
      return getSortOrderAttr().isPmVisible();
    }

    @Override
    public String getPmIconPath() {
      String path;
      PmAttrEnum<PmSortOrder> sortAttr = getSortOrderAttr();

      switch (sortAttr.getValue()) {
        case ASC: path = "pmSortOrder.ASC_icon"; break;
        case DESC: path = "pmSortOrder.DESC_icon"; break;
        case NEUTRAL: path = "pmSortOrder.NEUTRAL_icon"; break;
        default: throw new PmRuntimeException(this, "Unknown enum value: " + sortAttr.getValue());
      }

      return PmLocalizeApi.localize(this, isPmEnabled()
                                          ? path
                                          : path + "Disabled");
    }

    @Override
    protected void doItImpl() {
      PmAttrEnum<PmSortOrder> sortAttr = getSortOrderAttr();
      switch (sortAttr.getValue()) {
        case ASC: sortAttr.setValue(PmSortOrder.DESC); break;
        case DESC: sortAttr.setValue(PmSortOrder.NEUTRAL); break;
        case NEUTRAL: sortAttr.setValue(PmSortOrder.ASC); break;
        default: throw new PmRuntimeException(this, "Unknown enum value: " + sortAttr.getValue());
      }

      // If the attribute change reports a failure, we need to propagate that as
      // a failed command execution.
      List<PmMessage> errors = PmMessageUtil.getPmErrors(sortAttr);
      if (!errors.isEmpty()) {
        PmMessage m = errors.get(0);
        throw new PmRuntimeException(this, new PmResourceData(m.getMsgKey(), m.getMsgArgs()));
      }
    }
  }

  /**
   * Identifies the corresponding cell PM within the given row.
   * <p>
   * The default implementation just looks for a child with a similar name within the given row.
   *
   * @param rowPm PM of the row that contains
   * @return The found cell PM. <code>null</code> if there is no corresponding item.
   */
  protected PmObject findCorrespondingRowCell(PmElement rowPm) {
    return PmUtil.findChildPm(rowPm, this.getPmName());
  }

  /**
   * @return The {@link PmTable} that contains this column.
   */
  private PmTableImpl2<?, ?> getPmTableImpl() {
    return (PmTableImpl2<?, ?>)getPmParent();
  }

  public PmSortOrder getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(PmSortOrder sortOrder) {
    this.sortOrder = sortOrder;
  }

  /**
   * The default implementation provides the {@link SortOrder} defined by the {@link QueryOptions}
   * of the {@link PmTable2}.
   * <p>
   * Subclasses may provide here alternate {@link SortOrder}s.
   *
   * @return the {@link SortOrder} that may be used for this column.
   */
  protected SortOrder getSortOrderQueryOption() {
    String colName = PmTableColImpl2.this.getPmName();
    return getPmTableImpl().getPmPageableCollection().getQueryOptions().getSortOrder(colName);
  }

  @Override
  public void accept(PmVisitor visitor) {
    visitor.visit(this);
  }



  /**
   * Default sort order PM attribute class.
   * <p>
   * Changes the value only after successful validation if the table content.
   * <p>
   * May be extended or replaced by domain specific implementations.
   */
  public class SortOrderAttr extends PmAttrEnumImpl<PmSortOrder> {
    public SortOrderAttr(PmObject pmParent) {
      super(pmParent, PmSortOrder.class);
    }

    @Override
    protected PmSortOrder getDefaultValueImpl() {
      return PmSortOrder.NEUTRAL;
    }

    @Override
    protected boolean isPmEnabledImpl() {
      return  (getSortOrderQueryOption() != null) &&
              (getPmTableImpl().getTotalNumOfRows() > 1);
    }

    /**
     * Should not be influenced by the read-only state of a dialog part. It
     * should still be possible to sort read-only tables. Check if it's the
     * place for this logic.
     **/
    @Override
    protected boolean isPmReadonlyImpl() {
      return false;
    }

    @Override
    protected boolean isPmVisibleImpl() {
      return isPmEnabledImpl();
    }

    @Override
    protected Collection<PmCommandDecorator> getValueChangeDecorators() {
      ArrayList<PmCommandDecorator> list = new ArrayList<PmCommandDecorator>(getPmTableImpl().getPmDecorators(PmTable2.TableChange.SORT));
      list.addAll(super.getValueChangeDecorators());
      return list;
    }

    @Override
    protected void afterValueChange(PmSortOrder oldValue, PmSortOrder newValue) {
      SortOrder sortOrder = null;
      if (newValue != PmSortOrder.NEUTRAL) {
        sortOrder = getSortOrderQueryOption();
        if ((sortOrder != null) && (newValue == PmSortOrder.DESC)) {
          sortOrder = sortOrder.getReverseSortOrder();
        }
      }
      PmTableImpl2<?, ?> pmTable = getPmTableImpl();
      pmTable.getPmPageableCollection().getQuery().setSortOrder(sortOrder);

      // TODO: move to a listerner within the table implementation.
      // fire a value change event.
      PmEventApi.firePmEventIfInitialized(pmTable, PmEvent.VALUE_CHANGE, ValueChangeKind.SORT_ORDER);
    }

    @Override
    protected PmSortOrder getBackingValueImpl() {
      return sortOrder;
    }

    @Override
    protected void setBackingValueImpl(PmSortOrder value) {
      sortOrder = value;
    }
  }

  // ======== Meta data ======== //

  @Override
  protected org.pm4j.core.pm.impl.PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;

    PmTableColCfg annotation = AnnotationUtil.findAnnotation(this, PmTableColCfg.class);
    if (annotation != null) {
      myMetaData.colSizeSpec = new ColSizeSpec(
          annotation.prefSize(), annotation.minSize(), annotation.maxSize());

      if (annotation.sortable() != PmBoolean.UNDEFINED) {
        throw new PmRuntimeException(this, "The sortable annotation is not supported by PmTableColImpl2. Please use the table query options.");
      }

      if (annotation.filterBy().length > 0) {
        throw new PmRuntimeException(this, "The filterBy annotation is not supported by PmTableColImpl2. Please use the table query options.");
      }
    }
  }

  protected static class MetaData extends PmObjectBase.MetaData {
    private ColSizeSpec colSizeSpec = null;
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }


}

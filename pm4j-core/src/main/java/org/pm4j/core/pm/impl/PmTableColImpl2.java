package org.pm4j.core.pm.impl;

import java.util.Collection;
import java.util.List;

import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.query.filter.FilterDefinition;
import org.pm4j.common.query.filter.FilterDefinitionFactory;
import org.pm4j.common.util.collection.ListUtil;
import org.pm4j.core.exception.PmResourceData;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEvent.ValueChangeKind;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmSortOrder;
import org.pm4j.core.pm.PmTable2;
import org.pm4j.core.pm.PmTableCol2;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.annotation.PmTableColCfg2;
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

  /** PM for the column sort order. */
  public final PmAttrEnum<PmSortOrder> sortOrderAttr = createSortOrderAttrPm();

  /** A command that switches the sort order attribute. */
  public final PmCommand cmdSort = createCmdSortPm();

  /** Cached sort order option the user may sort the column by. */
  private SortOrder sortOrderOption;

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
    return sortOrderAttr;
  }

  @Override
  public PmCommand getCmdSort() {
    return cmdSort;
  }

  /**
   * Creates the PM for the {@link #sortOrderAttr} attribute.<br>
   * Subclasses may define here their specific sortOrderAttr PM.
   *
   * @return The <code>sortOrderAttr</code> attribute to use.
   */
  protected PmAttrEnum<PmSortOrder> createSortOrderAttrPm() {
    return new SortOrderAttr(this);
  }

  /**
   * Creates the PM for {@link #cmdSort}.<br>
   * Subclasses may define here their specific command PM.
   *
   * @return The command to use.
   */
  protected PmCommand createCmdSortPm() {
    return new CmdSortPm();
  }

  /**
   * Creates column specific filter meta data.
   */
  protected FilterDefinition createFilterCompareDefinition(FilterDefinitionFactory fcdf) {
    FilterDefinition fcd = null;
    if (getOwnMetaData().filterType != Void.class) {
      fcd = fcdf.createCompareDefinition(getColQueryAttr());
    }
    return fcd;
  }

  /**
   * @return The {@link PmTable} that contains this column.
   */
  private PmTable2<?> getPmTable() {
    return (PmTable2<?>)getPmParent();
  }

  /**
   * Provides the {@link SortOrder} defined by the {@link QueryOptions}
   * of the {@link PmTable2}.
   *
   * @return The {@link SortOrder} that may be used for this column.<br>
   *         Is <code>null</code> if this column is not sortable.
   */
  public final SortOrder getColSortOrderOption() {
    if (sortOrderOption == null) {
      sortOrderOption = getPmQueryOptions().getSortOrder(getColQueryAttrName());
    }
    return sortOrderOption;
  }

  /**
   * Provides the name of the corresponding {@link QueryAttr}.<br>
   * The default implementation returns {@link #getPmName()}.
   * <p>
   * This name is used to find the matching sort or filter option from the table {@link QueryOptions}.
   * <p>
   * In case of in-memory tables it is also used for query option generation.
   *
   * @return The corresponding query attribute name.
   */
  protected String getColQueryAttrName() {
    return getPmName();
  }

  /**
   * Gets (or creates) the {@link QueryAttr} that addresses the value to filter/sort within this column.
   *
   * @return
   */
  protected QueryAttr getColQueryAttr() {
    String name = getColQueryAttrName();
    return new QueryAttr(name, name, getOwnMetaData().filterType, getPmTitle());
  }

  /**
   * @return The {@link QueryOptions} provided by the pageable collection behind the table.
   */
  protected QueryOptions getPmQueryOptions() {
    return getPmTable().getPmPageableCollection().getQueryOptions();
  }

  /**
   * A command that switches the sort order attribute.
   * <p>
   * Delegates all calls to {@link PmTableColImpl2#sortOrderAttr}.
   */
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
      PmSortOrder so = sortOrderAttr.getValue();
      return PmLocalizeApi.localize(this, isPmEnabled()
                                          ? so.resKeyIcon
                                          : so.resKeyIconDisabled);
    }

    /**
     * Each do-call performs a round-robin step through the {@link PmSortOrder} value set.
     */
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
   * Default sort order PM attribute class.
   * <p>
   * Changes the value only after successful validation if the table content.
   * <p>
   * May be extended or replaced by domain specific implementations.
   */
  public class SortOrderAttr extends PmAttrEnumImpl<PmSortOrder> {
    public SortOrderAttr(PmObject pmParent) {
      super(pmParent, PmSortOrder.class);

      /** Adjusts the sort order whenever the table's sort order changes. */
      PmEventListener tableSortOrderChangeListener = new PmEventListener() {
        @Override
        public void handleEvent(PmEvent event) {
          if ((event.getValueChangeKind() == ValueChangeKind.SORT_ORDER) &&
              // Checks if the event source is not this column to prevent set value ping-pong games.
              (event.getPm() != PmTableColImpl2.this)) {
            SortOrder tableSortOrder = getPmTable().getPmPageableCollection().getQueryParams().getSortOrder();
            SortOrder columnSortOrderOption = getColSortOrderOption();
            if (tableSortOrder != null &&
                SortOrder.bothOrdersUseTheSameAttributeSet(tableSortOrder, columnSortOrderOption)) {
              setBackingValue(tableSortOrder.isAscending() ? PmSortOrder.ASC : PmSortOrder.DESC);
            } else {
              setBackingValue(PmSortOrder.NEUTRAL);
            }
          }
        }
      };
      PmEventApi.addPmEventListener(getPmTable(), PmEvent.VALUE_CHANGE, tableSortOrderChangeListener);
    }

    @Override
    protected PmSortOrder getDefaultValueImpl() {
      return PmSortOrder.NEUTRAL;
    }

    @Override
    protected boolean isPmEnabledImpl() {
      return  (getColSortOrderOption() != null) &&
              (getPmTable().getTotalNumOfPmRows() > 1);
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

    /**
     * A sort order change is by default not handled as a data change.<br>
     * This information is usually not persistent.
     */
    @Override
    protected boolean isPmValueChangedImpl() {
      return false;
    }

    @Override
    protected Collection<PmCommandDecorator> getValueChangeDecorators() {
      return ListUtil.collectionsToList(
          getPmTable().getPmDecorators(PmTable2.TableChange.SORT),
          super.getValueChangeDecorators());
    }

    private SortOrder getOwnQuerySortOrder() {
      PmSortOrder sortDirection = getValue();
      SortOrder querySortOrder = null;
      if (sortDirection != PmSortOrder.NEUTRAL) {
        querySortOrder = getColSortOrderOption();
        if ((querySortOrder != null) && (sortDirection == PmSortOrder.DESC)) {
          querySortOrder = querySortOrder.getReverseSortOrder();
        }
      }
      return querySortOrder;
    }

    @Override
    protected void afterValueChange(PmSortOrder oldValue, PmSortOrder newValue) {
      SortOrder querySortOrder = getOwnQuerySortOrder();
      PmTable2<?> pmTable = getPmTable();
      pmTable.getPmPageableCollection().getQueryParams().setSortOrder(querySortOrder);

      // TODO: move to a listener within the table implementation.
      // fire a value change event.
      PmEventApi.firePmEventIfInitialized(pmTable, new PmEvent(PmTableColImpl2.this, pmTable, PmEvent.VALUE_CHANGE, ValueChangeKind.SORT_ORDER));
    }
  }

  // ======== Public details layer access definition ======== //

  /** Interface for other PMs. E.g. the table. */
  private class ColumnDetailsImpl implements ImplDetails {

    @Override
    public String getQueryAttrName() {
      return PmTableColImpl2.this.getColQueryAttrName();
    }

    @Override
    public Boolean isSortableConfigured() {
      return PmTableColImpl2.this.getOwnMetaData().sortable;
    }

    @Override
    public FilterDefinition getFilterCompareDefinition(FilterDefinitionFactory fcdf) {
      return PmTableColImpl2.this.createFilterCompareDefinition(fcdf);
    }
  }

  @Override
  public final ImplDetails getPmImplDetails() {
    return new ColumnDetailsImpl();
  }

  // ======== Meta data ======== //

  @Override
  protected org.pm4j.core.pm.impl.PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData md = (MetaData) metaData;

    PmTableColCfg2 a = AnnotationUtil.findAnnotation(this, PmTableColCfg2.class);
    if (a != null) {
      if (a.sortable() != PmBoolean.UNDEFINED) {
        md.sortable = a.sortable() == PmBoolean.TRUE;
      }

      md.filterType = a.filterType();
    }
  }

  protected static class MetaData extends PmObjectBase.MetaData {
    private Boolean sortable;
    private Class<?> filterType = Void.class;
    private ColSizeSpec colSizeSpec = null;
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }

}



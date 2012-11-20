package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmSortOrder;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTable.TableChange;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.PmVisitor;
import org.pm4j.core.pm.annotation.FilterByCfg;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.annotation.PmTableCfg;
import org.pm4j.core.pm.annotation.PmTableColCfg;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.filter.FilterByDefinition;
import org.pm4j.core.pm.filter.impl.FilterByDefinitionBase;
import org.pm4j.core.util.reflection.ClassUtil;
import org.pm4j.core.util.table.ColSizeSpec;

/**
 * Implements the table column PM behavior.
 *
 * @author olaf boede
 */
public class PmTableColImpl extends PmObjectBase implements PmTableCol {

  public final PmAttrEnum<PmSortOrder> defaultSortOrderAttr = new SortOrderAttr(this);
  public final PmCommand defaultCmdSort = new CmdSortPm();
  public final PmAttrInteger defaultColPosAttr = new PmAttrIntegerImpl(this);
  private PmSortOrder sortOrder = PmSortOrder.NEUTRAL;

  /** The filter definitions for this column. */
  private List<FilterByDefinition> filterByDefinitions;



  /**
   * @param pmTable
   *          The table containing this column.
   */
  public PmTableColImpl(PmTable<?> pmTable) {
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

  @Override
  public PmAttrInteger getColPosAttr() {
    return defaultColPosAttr;
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
      super(PmTableColImpl.this);
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

  @Override
  public void accept(PmVisitor visitor) {
    visitor.visit(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<FilterByDefinition> getFilterByDefinitions() {
    if (filterByDefinitions == null) {
      List<FilterByDefinition> list = getFilterByDefinitionsImpl();
      filterByDefinitions = (list != null)
                ? list
                : Collections.EMPTY_LIST;
    }

    return filterByDefinitions;
  }

  /**
   * Provides the set of filter definitions the user may use for this table
   * column.
   * <p>
   * The default implementation calls {@link #getFilterByDefinitionImpl()} and adds
   * filter instances for the filters declared in {@link PmTableColCfg#filterBy()}.
   *
   * @return The set of column filter definitions.<br>
   *         May return <code>null</code> if there is no filter definition.
   */
  protected List<FilterByDefinition> getFilterByDefinitionsImpl() {
    FilterByDefinition fd = getFilterByDefinitionImpl();
    int itemCount = getOwnMetaData().filterByCfgs.length + (fd != null ? 1 : 0);
    if (itemCount == 0) {
      return null;
    }
    else {
      List<FilterByDefinition> list = new ArrayList<FilterByDefinition>(itemCount);
      if (fd != null) {
        list.add(fd);
      }
      for (FilterByCfg cfg : getOwnMetaData().filterByCfgs) {
        Class<?> filterClass = cfg.value() != FilterByDefinition.class
            ? cfg.value()
            : getPmConversation().getPmDefaults().getDefaultFilterByDefintionClass();
        fd = ClassUtil.newInstance(filterClass, this);
        if (cfg.valueAttrPm() != PmAttr.class) {
          ((FilterByDefinitionBase<?, ?>)fd).setValueAttrPmClass(cfg.valueAttrPm());
        }
        list.add(fd);
      }
      return list;
    }
  }

  /**
   * A convenience method that allows to define a single user filter definition
   * for this column.
   * <p>
   * Gets called by {@link #getFilterByDefinitionsImpl()}.
   *
   * @return A single filter definition or <code>null</code>.
   */
  protected FilterByDefinition getFilterByDefinitionImpl() {
    return null;
  }

  /**
   * @return The {@link PmTable} that contains this column.
   */
  private PmTableImpl<?> getPmTableImpl() {
    return (PmTableImpl<?>)getPmParent();
  }

  public PmSortOrder getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(PmSortOrder sortOrder) {
    this.sortOrder = sortOrder;
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
        myMetaData.sortable = annotation.sortable();
      }

      if (annotation.filterBy().length > 0) {
        myMetaData.filterByCfgs = annotation.filterBy();
      }
    }

    if (myMetaData.sortable == PmBoolean.UNDEFINED) {
      // Read the default sort definition from the table.
      PmTableCfg tableCfg = AnnotationUtil.findAnnotation((PmObjectBase)getPmParent(), PmTableCfg.class);
      if (tableCfg != null)  {
        myMetaData.sortable = (tableCfg.sortable() == PmBoolean.TRUE)
              ? PmBoolean.TRUE
              : PmBoolean.FALSE;

        // the column gets automatically sortable if it is mentioned as default sort column.
        if (tableCfg.defaultSortCol().length() > 0 &&
            (tableCfg.defaultSortCol().equals(getPmName()) ||
             getPmName().equals(StringUtils.substringBefore(tableCfg.defaultSortCol(), ",")))) {
          myMetaData.sortable = PmBoolean.TRUE;
        }
      }
    }
  }

  protected static class MetaData extends PmObjectBase.MetaData {
    private ColSizeSpec colSizeSpec = null;
    private PmBoolean sortable = PmBoolean.UNDEFINED;
    private FilterByCfg[] filterByCfgs = {};
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
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
      return  (getOwnMetaData().sortable != PmBoolean.FALSE) &&
              (getPmTableImpl().getTotalNumOfRows() > 1);
    }

    // XXX olaf: Should not be influenced by the read-only state of a dialog part.
    //           It should still be possible to sort read-only tables.
    //           Check if it's the place for this logic.
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
      ArrayList<PmCommandDecorator> list = new ArrayList<PmCommandDecorator>(getPmTableImpl().getDecorators(TableChange.SORT));
      list.addAll(super.getValueChangeDecorators());
      return list;
    }

    @Override
    protected void afterValueChange(PmSortOrder oldValue, PmSortOrder newValue) {
      getPmTableImpl().sortBy(PmTableColImpl.this, newValue);
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

}

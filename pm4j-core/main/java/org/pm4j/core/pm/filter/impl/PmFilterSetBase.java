package org.pm4j.core.pm.filter.impl;

import java.util.List;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrProxy;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.filter.CombinedBy;
import org.pm4j.core.pm.filter.Filter;
import org.pm4j.core.pm.filter.FilterByDefinitionProvider;
import org.pm4j.core.pm.filter.FilterItem;
import org.pm4j.core.pm.filter.FilterSet;
import org.pm4j.core.pm.filter.Filterable;
import org.pm4j.core.pm.filter.PmFilterItem;
import org.pm4j.core.pm.filter.PmFilterSet;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmAttrPmListImpl;
import org.pm4j.core.pm.impl.PmAttrProxyImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmCommandProxy;

@PmBeanCfg(beanClass=FilterSet.class)
public class PmFilterSetBase extends PmBeanBase<FilterSet> implements PmFilterSet {

  public final PmAttrProxy<List<PmFilterItem>> filterItems = new PmAttrProxyImpl<List<PmFilterItem>>(this);

  public final PmAttrProxy<CombinedBy> combinedBy = new PmAttrProxyImpl<CombinedBy>(this);

  public final PmCommandProxy cmdApply = new PmCommandProxy(this);

  public final PmCommandProxy cmdClear = new PmCommandProxy(this);

  private String filterId = USER_FILTER_SET_ID;
  private int numOfFilterConditionLines = 5;
  private FilterByDefinitionProvider filterByDefinitionProvider;
  private Filterable filterable;

  public PmFilterSetBase(PmObject pmParent, FilterByDefinitionProvider filterByDefinitionProvider, Filterable filterable) {
    super(pmParent, null);
    this.filterByDefinitionProvider = filterByDefinitionProvider;
    this.filterable = filterable;
  }

  public PmFilterSetBase(PmObject pmParent) {
    this(pmParent, (FilterByDefinitionProvider)pmParent, (Filterable)pmParent);
  }

  /**
   * Creates a default {@link FilterSet}.
   */
  @Override
  protected FilterSet findPmBeanImpl() {
    FilterSet fs = null;
    // 1. Get the already existing filter to display.
    if (filterable != null) {
      Filter f = filterable.getFilter(filterId);
      if (f instanceof FilterSetFilter) {
        fs = ((FilterSetFilter)f).getFilterSet();
        if (fs != null) {
          return fs;
        }
      }
    }

    // 2. No existing filter set: Create a new one based on the filter definitions.
    return filterByDefinitionProvider != null
              ? getDefaultFilterSet()
              : new FilterSet();
  }

  /**
   * Provides an initial, clear filter for entering a completely new filter definition.
   * <p>
   * Subclasses may override this method to provide their specific initial filter set configurations.
   *
   * @return A initial filter set instance.
   */
  protected FilterSet getDefaultFilterSet() {
    return FilterSetUtil.makeFilterSetStartingWithDefaultConditions(filterByDefinitionProvider.getFilterByDefinitions(), numOfFilterConditionLines);
  }


  @PmFactoryCfg(beanPmClasses=PmFilterItemDefaultImpl.class)
  @PmAttrCfg(valuePath="pmBean.filterItems")
  public class FilterItemsAttrPm extends PmAttrPmListImpl<PmFilterItem, FilterItem> {

    public FilterItemsAttrPm() {
      super(PmFilterSetBase.this);
      filterItems.setDelegate(this);
    }
  }

  public class CombinedByAttrPm extends PmAttrEnumImpl<CombinedBy> {

    public CombinedByAttrPm() {
      super(PmFilterSetBase.this, CombinedBy.class);
      combinedBy.setDelegate(this);
    }

    @Override
    protected CombinedBy getBackingValueImpl() {
      return getPmBean().getCombindedBy();
    }

    @Override
    protected void setBackingValueImpl(CombinedBy value) {
      getPmBean().setCombindedBy(value);
    }

    @Override
    public boolean isRequired() {
      return true;
    }

  }

  /**
   * Applies the filter definition to the {@link Filterable} target object.
   */
  public class CmdApply extends PmCommandImpl {

    public CmdApply() {
      super(PmFilterSetBase.this);
      cmdApply.setDelegateCmd(this);
    }

    @Override
    protected void doItImpl() {
      FilterSetFilter filter = new FilterSetFilter(getPmBean());
      filterable.setFilter(filterId, filter.isEffective()
                                      ? filter
                                      : null);
    }
  }

  /**
   * Clears the filter definitions of the currently edited {@link FilterSet}.
   * <p>
   * Does not reset the active filter. The cleared filter definition may get active if {@link CmdApply} gets
   * executed afterwards.
   */
  public class CmdClear extends PmCommandImpl {

    public CmdClear() {
      super(PmFilterSetBase.this);
      cmdClear.setDelegateCmd(this);
    }

    @Override
    protected void doItImpl() throws Exception {
      setPmBean(getDefaultFilterSet());
    }
  }


  // -- getter --

  @Override
  public PmAttr<List<PmFilterItem>> getFilterItems() { return filterItems; }
  @Override
  public PmAttr<CombinedBy> getCombinedBy() { return combinedBy; }

  public int getNumOfFilterConditionLines() { return numOfFilterConditionLines; }
  public void setNumOfFilterConditionLines(int numOfFilterConditionLines) { this.numOfFilterConditionLines = numOfFilterConditionLines; }

  public FilterByDefinitionProvider getFilterByDefinitionProvider() { return filterByDefinitionProvider; }
  public void setFilterByDefinitionProvider(FilterByDefinitionProvider filterByDefinitionProvider) {
    setPmBean(null);
    this.filterByDefinitionProvider = filterByDefinitionProvider;
  }

  public Filterable getFilterable() { return filterable; }
  public void setFilterable(Filterable filterable) { this.filterable = filterable; }

  @Override
  public PmCommand getCmdApply() {
    return cmdApply;
  }

  @Override
  public PmCommand getCmdClear() {
    return cmdClear;
  }

}

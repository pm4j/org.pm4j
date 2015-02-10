package org.pm4j.deprecated.core.pm.filter.impl;

import java.util.List;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrProxy;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmAttrPmListImpl;
import org.pm4j.core.pm.impl.PmAttrProxyImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmCommandProxy;
import org.pm4j.deprecated.core.pm.filter.DeprCombinedBy;
import org.pm4j.deprecated.core.pm.filter.DeprFilterByDefinitionProvider;
import org.pm4j.deprecated.core.pm.filter.DeprFilterItem;
import org.pm4j.deprecated.core.pm.filter.DeprFilterSet;
import org.pm4j.deprecated.core.pm.filter.DeprFilterable;
import org.pm4j.deprecated.core.pm.filter.DeprPmFilterItem;
import org.pm4j.deprecated.core.pm.filter.DeprPmFilterSet;

@Deprecated
@PmBeanCfg(beanClass=DeprFilterSet.class)
public class DeprPmFilterSetBase extends PmBeanBase<DeprFilterSet> implements DeprPmFilterSet {

  public final PmAttrProxy<List<DeprPmFilterItem>> filterItems = new PmAttrProxyImpl<List<DeprPmFilterItem>>(this);

  public final PmAttrProxy<DeprCombinedBy> combinedBy = new PmAttrProxyImpl<DeprCombinedBy>(this);

  public final PmCommandProxy cmdApply = new PmCommandProxy(this);

  public final PmCommandProxy cmdClear = new PmCommandProxy(this);

  private String filterId = USER_FILTER_SET_ID;
  private int numOfFilterConditionLines = 5;
  private DeprFilterByDefinitionProvider filterByDefinitionProvider;
  private DeprFilterable filterable;

  public DeprPmFilterSetBase(PmObject pmParent, DeprFilterByDefinitionProvider filterByDefinitionProvider, DeprFilterable filterable) {
    super(pmParent, null);
    this.filterByDefinitionProvider = filterByDefinitionProvider;
    this.filterable = filterable;
  }

  public DeprPmFilterSetBase(PmObject pmParent) {
    this(pmParent, (DeprFilterByDefinitionProvider)pmParent, (DeprFilterable)pmParent);
  }

  /**
   * Creates a default {@link DeprFilterSet}.
   */
  @Override
  protected DeprFilterSet findPmBeanImpl() {
    // 1. Get the already existing filter to display.
    if (filterable != null) {
      DeprFilterSet fs = DeprFilterSetUtil.findActiveFilterSet(filterable, filterId);
      if (fs != null) {
        return fs;
      }
    }

    // 2. No existing filter set: Create a new one based on the filter definitions.
    return filterByDefinitionProvider != null
              ? getDefaultFilterSet()
              : new DeprFilterSet();
  }

  /**
   * Provides an initial, clear filter for entering a completely new filter definition.
   * <p>
   * Subclasses may override this method to provide their specific initial filter set configurations.
   *
   * @return A initial filter set instance.
   */
  protected DeprFilterSet getDefaultFilterSet() {
    return DeprFilterSetUtil.makeFilterSetStartingWithDefaultConditions(filterByDefinitionProvider.getFilterByDefinitions(), numOfFilterConditionLines);
  }


  @PmFactoryCfg(beanPmClasses=DeprPmFilterItemDefaultImpl.class)
  @PmAttrCfg(valuePath="pmBean.filterItems")
  public class FilterItemsAttrPm extends PmAttrPmListImpl<DeprPmFilterItem, DeprFilterItem> {

    public FilterItemsAttrPm() {
      super(DeprPmFilterSetBase.this);
      filterItems.setDelegate(this);
    }
  }

  public class CombinedByAttrPm extends PmAttrEnumImpl<DeprCombinedBy> {

    public CombinedByAttrPm() {
      super(DeprPmFilterSetBase.this, DeprCombinedBy.class);
      combinedBy.setDelegate(this);
    }

    @Override
    protected DeprCombinedBy getBackingValueImpl() {
      return getPmBean().getCombindedBy();
    }

    @Override
    protected void setBackingValueImpl(DeprCombinedBy value) {
      getPmBean().setCombindedBy(value);
    }

    @Override
    protected boolean isRequiredImpl() {
      return true;
    }

  }

  /**
   * Applies the filter definition to the {@link DeprFilterable} target object.
   */
  public class CmdApply extends PmCommandImpl {

    public CmdApply() {
      super(DeprPmFilterSetBase.this);
      cmdApply.setDelegateCmd(this);
    }

    @Override
    protected void doItImpl() {
      DeprFilterSetFilter filter = new DeprFilterSetFilter(getPmBean());
      filterable.setFilter(filterId, filter.isEffective()
                                      ? filter
                                      : null);
    }
  }

  /**
   * Clears the filter definitions of the currently edited {@link DeprFilterSet}.
   * <p>
   * Does not reset the active filter. The cleared filter definition may get active if {@link CmdApply} gets
   * executed afterwards.
   */
  public class CmdClear extends PmCommandImpl {

    public CmdClear() {
      super(DeprPmFilterSetBase.this);
      cmdClear.setDelegateCmd(this);
    }

    @Override
    protected void doItImpl()  {
      setPmBean(getDefaultFilterSet());
    }
  }


  // -- getter --

  @Override
  public PmAttr<List<DeprPmFilterItem>> getFilterItems() { return filterItems; }
  @Override
  public PmAttr<DeprCombinedBy> getCombinedBy() { return combinedBy; }

  public int getNumOfFilterConditionLines() { return numOfFilterConditionLines; }
  public void setNumOfFilterConditionLines(int numOfFilterConditionLines) { this.numOfFilterConditionLines = numOfFilterConditionLines; }

  public DeprFilterByDefinitionProvider getFilterByDefinitionProvider() { return filterByDefinitionProvider; }
  public void setFilterByDefinitionProvider(DeprFilterByDefinitionProvider filterByDefinitionProvider) {
    setPmBean(null);
    this.filterByDefinitionProvider = filterByDefinitionProvider;
  }

  public DeprFilterable getFilterable() { return filterable; }
  public void setFilterable(DeprFilterable filterable) { this.filterable = filterable; }

  @Override
  public PmCommand getCmdApply() {
    return cmdApply;
  }

  @Override
  public PmCommand getCmdClear() {
    return cmdClear;
  }

}

package org.pm4j.standards.filter;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.filter.CombinedBy;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmAttrPmListImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmCommandImpl;

@PmBeanCfg(beanClass=FilterSet.class)
public class FilterSetPm extends PmBeanBase<FilterSet> {

  @PmFactoryCfg(beanPmClasses=FilterItemPm.class)
  public final PmAttrPmList<FilterItemPm<FilterItem>> filterItems = new PmAttrPmListImpl<FilterItemPm<FilterItem>, FilterItem>(this);

  // XXX olaf: use a simple boolean. Add a simplified api for defining custom resource strings.
  @PmAttrCfg(required=true)
  public final PmAttr<CombinedBy> combinedBy = new PmAttrEnumImpl<CombinedBy>(this, CombinedBy.class);

  /** Applies the filter definition to the {@link FilterSetProvider}. */
  public final PmCommand cmdApply = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      doApply();
    }
    protected boolean isPmEnabledImpl() {
      return filterSetProvider != null;
    }
  };

  public final PmCommand cmdClear = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      doClear();
    }
  };


  private int numOfFilterConditionLines = 5;
  private FilterSetProvider filterSetProvider;


  public FilterSetPm(PmObject pmParent) {
    super(pmParent, null);
  }

  /**
   * The default implementation re-sets the backing bean to the result of
   * {@link #getDefaultFilterSet()}.
   */
  protected void doClear() {
    setPmBean(getDefaultFilterSet());
  }

  /**
   * The default implementation sets the current bean as the filter set of
   * the active {@link FilterSetProvider}.
   */
  protected void doApply() {
    filterSetProvider.setActivePmFilterSet(getPmBean());
  }

  /**
   * Provides an initial, clear filter for entering a completely new filter definition.
   * <p>
   * Subclasses may override this method to provide their specific initial filter set configurations.
   *
   * @return An initial filter set instance.
   */
  protected FilterSet getDefaultFilterSet() {
    return FilterSetUtil.makeFilterSetStartingWithDefaultConditions(filterSetProvider.getAvailablePmFilterCompareDefinitions(), numOfFilterConditionLines);
  }

  /**
   * Creates a default {@link FilterSet}.
   */
  @Override
  protected FilterSet getPmBeanImpl() {
    // 1. Get the already existing filter to display.
    FilterSet fs = filterSetProvider.getActivePmFilterSet();
    if (!fs.isEmpty()) {
      return fs;
    }

    // 2. No existing filter set: Create a new one based on the filter definitions.
    return filterSetProvider != null
        ? getDefaultFilterSet()
        : new FilterSet();
  }

  // -- getter --

  public int getNumOfFilterConditionLines() { return numOfFilterConditionLines; }
  public void setNumOfFilterConditionLines(int numOfFilterConditionLines) { this.numOfFilterConditionLines = numOfFilterConditionLines; }

  public FilterSetProvider getFilterSetProvider() { return filterSetProvider; }
  public void setFilterSetProvider(FilterSetProvider filterSetProvider) {
    // resets the current bean. The next getPmBean call will regenerate a default instance.
    setPmBean(null);
    this.filterSetProvider = filterSetProvider;
  }
}

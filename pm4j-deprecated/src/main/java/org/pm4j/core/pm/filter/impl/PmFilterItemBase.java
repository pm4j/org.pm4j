package org.pm4j.core.pm.filter.impl;

import java.util.Collections;
import java.util.Iterator;

import org.pm4j.common.util.collection.IterableUtil;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrProxy;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.filter.CompOp;
import org.pm4j.core.pm.filter.FilterByDefinition;
import org.pm4j.core.pm.filter.FilterItem;
import org.pm4j.core.pm.filter.PmFilterItem;
import org.pm4j.core.pm.filter.CompOp.ValueNeeded;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmAttrProxyImpl;
import org.pm4j.core.pm.impl.PmAttrValueChangeDecorator;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmInitApi;

/**
 * <p>
 * This implementation just provides some proxy attributes which should be configured to forward
 * their logic to attributes that implement the application specific filter attribute logic.
 *
 * @author olaf boede
 */
@PmBeanCfg(beanClass=FilterItem.class)
public class PmFilterItemBase extends PmBeanBase<FilterItem> implements PmFilterItem {

  public final PmAttrProxy<FilterByDefinition> filterBy = new PmAttrProxyImpl<FilterByDefinition>(this);
  public final PmAttrProxy<CompOp> compOp = new PmAttrProxyImpl<CompOp>(this);
  public final PmAttrProxy<Object> filterByValue = new PmAttrProxyImpl<Object>(this);

  /** The entered filter values have to be transferred to the filter value. */
  protected PmAttrValueChangeDecorator<?> filterValueChangeDecorator = new PmAttrValueChangeDecorator<Object>() {
    protected void afterChange(PmAttr<Object> pmAttr, Object oldValue, Object newValue) {
        getPmBean().setFilterByValue(pmAttr.getValue());
    };
  };

  /**
   * Whenever the bean behind the PM changes, we need to transfer the actual filter value to the value field PM.
   */
  @Override
  protected void onPmValueChange(PmEvent event) {
    reGenerateFilterByValueAttr();
  }

  protected void reGenerateFilterByValueAttr() {
    FilterByDefinition fd = filterBy.getValue();
    if (fd != null) {
      CompOp co = compOp.getValue();
      if (co != null) {
        @SuppressWarnings("unchecked")
        PmAttr<Object> a = (PmAttr<Object>) fd.makeValueAttrPm(this);
        if (a != null) {
          a =  PmInitApi.initDynamicPmAttr(a, "filterBy");
          a.setValue(getPmBean().getFilterByValue());
          // XXX olaf: should be done on proxy level...
          PmEventApi.addValueChangeDecorator(a, filterValueChangeDecorator);
          filterByValue.setDelegate(a);
          return;
        }
      }
    }
    // in all other cases:
    filterByValue.setDelegate(null);
  }

  /**
   * Basic PM logic implementation for the <code>filterBy</code> attribute.
   * <p>
   * It allows to defined a filter-by value.<br>
   * On value change the attributes <code>compOp</code> and <code>compValue</code> will
   * be reset.
   */
  @PmAttrCfg(valuePath="pmBean.filterBy")
  public class FilterByAttrPm extends PmAttrImpl<FilterByDefinition> {

    public FilterByAttrPm() {
      super(PmFilterItemBase.this);
      PmFilterItemBase.this.filterBy.setDelegate(this);
    }

    @Override
    protected void afterValueChange(FilterByDefinition oldValue, FilterByDefinition newValue) {
      getPmBean().setFilterByValue(null);
      getCompOp().resetPmValues();
      reGenerateFilterByValueAttr();
    }

    @Override
    protected boolean isPmEnabledImpl() {
      return super.isPmEnabledImpl() &&
             ! IterableUtil.isEmpty(getOptionValues());
    }

    @Override
    @PmOptionCfg(id="name", title="title", nullOption=NullOption.NO)
    public Iterable<?> getOptionValues() {
      return getPmBean().getFilterByOptions();
    }
  }

  /**
   * Basic PM logic implementation for the <code>compOp</code> attribute.
   * <p>
   * It allows to select the compare operations that correspond to the selected
   * filter-by criterium.
   */
  @PmAttrCfg(valuePath="pmBean.compOp", required=true)
  public class CompOpAttrPm extends PmAttrImpl<CompOp> {

    public CompOpAttrPm() {
      super(PmFilterItemBase.this);
      PmFilterItemBase.this.compOp.setDelegate(this);
    }

    @Override
    protected void onPmInit() {
      super.onPmInit();
      if (getPmBean() != null) {
        reGenerateFilterByValueAttr();
      }
    }

    @Override
    @PmOptionCfg(id="name", title="title", nullOption=NullOption.NO)
    public Iterable<?> getOptionValues() {
      FilterByDefinition fbd = filterBy.getValue();
      return (fbd != null)
          ? fbd.getCompOps()
          : Collections.EMPTY_LIST;
    }

    @Override
    protected CompOp getDefaultValueImpl() {
      Iterable<?> optionValues = getOptionValues();
      @SuppressWarnings("unchecked")
      Iterator<CompOp> iter = (Iterator<CompOp>)optionValues.iterator();
      return iter.hasNext()
              ? iter.next()
              : null;
    }

    @Override
    protected boolean isPmEnabledImpl() {
      return super.isPmEnabledImpl() &&
             !IterableUtil.isEmpty(getOptionValues());
    }

    @Override
    protected void afterValueChange(CompOp oldValue, CompOp newValue) {
      boolean oldFilterByPmExists = filterByValue.getDelegate() != null;
      boolean newFilterByPmNeeded = (newValue != null) && (newValue.getValueNeeded() != ValueNeeded.NO);

      if (newFilterByPmNeeded) {
        if (!oldFilterByPmExists) {
          reGenerateFilterByValueAttr();
        }
      }
      else {
        if (oldFilterByPmExists) {
          filterByValue.setDelegate(null);
        }
      }
    }
  }

  @Override
  public PmAttr<? extends FilterByDefinition> getFilterBy() { return filterBy; }
  @Override
  public PmAttr<?> getCompOp() { return compOp; }
  @Override
  public PmAttr<?> getFilterByValue() { return filterByValue; }

}

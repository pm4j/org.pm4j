package org.pm4j.standards.filter;

import java.util.Collections;
import java.util.Iterator;

import org.pm4j.common.query.CompOp;
import org.pm4j.common.query.filter.FilterDefinition;
import org.pm4j.common.util.collection.IterableUtil;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrProxy;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.annotation.PmInject.Mode;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmAttrProxyImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmAttrValueChangeDecorator;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmInitApi;
import org.pm4j.core.pm.impl.options.PmOptionImpl;
import org.pm4j.core.pm.impl.options.PmOptionSetImpl;
import org.pm4j.standards.filter.FilterSetProvider.FilterByValuePmAttrFactory;

/**
 * <p>
 * This implementation just provides some proxy attributes which should be configured to forward
 * their logic to attributes that implement the application specific filter attribute logic.
 *
 * @param T_ITEM the filter item bean type is paramerterized to allow sub classing for derived types.
 *
 * @author olaf boede
 */
public abstract class FilterItemPm<T_ITEM extends FilterItem> extends PmBeanBase<T_ITEM> {

  /**
   * Allows to select the attribute to filter by.<br>
   * On value change the attributes <code>compOp</code> and <code>compValue</code> will
   * be reset.
   */
  public final PmAttr<FilterDefinition> filterBy = new PmAttrImpl<FilterDefinition>(this) {

    @Override
    protected void afterValueChange(FilterDefinition oldValue, FilterDefinition newValue) {
      getPmBean().setFilterByValue(null);
      compOp.resetPmValues();
      reGenerateFilterByValueAttr();
    }

    @Override
    protected boolean isPmEnabledImpl() {
      return ! IterableUtil.isEmpty(getOptionValues());
    }

    @Override
    @PmOptionCfg(id="attr.name", title="attrTitle", nullOption=NullOption.NO)
    public Iterable<?> getOptionValues() {
      return getPmBean().getFilterByOptions();
    }
  };


  /**
   * Allows to select the compare operations that correspond to the selected
   * filter-by criterium.
   */
  @PmAttrCfg(required=true)
  public final PmAttr<CompOp> compOp = new PmAttrImpl<CompOp>(this) {
    @Override
    protected void onPmInit() {
      super.onPmInit();
      if (getPmBean() != null) {
        reGenerateFilterByValueAttr();
      }
    }

    // TODO olaf: we need a title for the compOp here!
    @Override
    @PmOptionCfg(id="name", title="name", nullOption=NullOption.NO)
    public Iterable<?> getOptionValues() {
      FilterDefinition fbd = filterBy.getValue();
      return (fbd != null)
          ? fbd.getCompOps()
          : Collections.EMPTY_LIST;
    }

    @SuppressWarnings("unchecked")
    protected PmOptionSet getOptionSetImpl() {
      PmOptionSetImpl os = new PmOptionSetImpl();
      for (CompOp compOp : (Iterable<CompOp>)getOptionValues()) {
        os.addOption(new PmOptionImpl(compOp.getName(), PmLocalizeApi.localize(this, compOp.getName()), compOp));
      }

      return os;
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
      return !IterableUtil.isEmpty(getOptionValues());
    }

    @Override
    protected void afterValueChange(CompOp oldValue, CompOp newValue) {
      boolean oldFilterByPmExists = filterByValue != null && filterByValue.isPmVisible();
      boolean newFilterByPmNeeded = (newValue != null) && (newValue.getValueNeeded() != CompOp.ValueNeeded.NO);

      // XXX: are all if's needed? Just re-generate?
      if (newFilterByPmNeeded) {
        if (!oldFilterByPmExists) {
          reGenerateFilterByValueAttr();
        }
      }
      else {
        if (oldFilterByPmExists) {
          reGenerateFilterByValueAttr();
        }
      }
    }
  };

  /** A proxy attribute in front of the value type specific value attribute. */
  private PmAttr<?> filterByValue;

  /** The entered filter values have to be transferred to the filter value. */
  protected PmAttrValueChangeDecorator<?> filterValueChangeDecorator = new PmAttrValueChangeDecorator<Object>() {
    protected void afterChange(PmAttr<Object> pmAttr, Object oldValue, Object newValue) {
        getPmBean().setFilterByValue(pmAttr.getValue());
    }
  };

  /** Needed for mapping a type to a pm attribute for dynamic switching. */
  private FilterByValuePmAttrFactory filterByValuePmAttrFactory = new FilterByValuePmAttrFactoryImpl();

  public PmAttr<?> getFilterByValue() {
    if (filterByValue == null) {
      reGenerateFilterByValueAttr();
    }
    return filterByValue;
  }
  
  @Override
  protected void onPmInit() {
    super.onPmInit();
    if (getPmBean() != null) {
      reGenerateFilterByValueAttr();
    }
  }
  
  /**
   * Whenever the bean behind the PM changes, we need to transfer the actual filter value to the value field PM.
   */
  @Override
  protected void onPmValueChange(PmEvent event) {
    reGenerateFilterByValueAttr();
  }

  protected void reGenerateFilterByValueAttr() {
    T_ITEM bean = getPmBean();
    FilterDefinition fd = bean.getFilterBy();
    if (fd != null) {
      // TODO olaf: the getValue method is called within pmInit. should not happen...
      CompOp co = compOp.getValue();
      // DeprCompOp co = bean.getCompOp();
      if (co != null) {
        @SuppressWarnings("unchecked")
        PmAttr<Object> a =(PmAttr<Object>)getFilterByValuePmAttrFactory().makeValueAttrPm(this, fd, co);
        if (a != null) {
          a =  PmInitApi.initDynamicPmAttr(a, "filterBy");
          if (bean.getFilterByValue() != null) {
            a.setValue(bean.getFilterByValue());
          }
          // XXX olaf: should be done on proxy level...
          PmEventApi.addValueChangeDecorator(a, filterValueChangeDecorator);
          filterByValue = a;
          return;
        }
      }
    }
    // in all other cases:
    filterByValue = new PmAttrStringImpl(this) {
      @Override
      protected boolean isPmVisibleImpl() {
        return false;
      }
    };
  }

  /**
   * gets the FilterByValuePmAttrFactory.
   * @return a factory.
   */
  protected abstract FilterByValuePmAttrFactory getFilterByValuePmAttrFactory();

  /**
   * sets the FilterByValuePmAttrFactory.
   * @param factory
   */
  public void setFilterByValuePmAttrFactory( FilterByValuePmAttrFactory factory) {
    filterByValuePmAttrFactory = factory;
  }

}

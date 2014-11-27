package org.pm4j.core.pm.impl;

import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrProxy;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmAttrCfg.AttrAccessKind;
import org.pm4j.core.pm.api.PmValidationApi;

/**
 *
 * A place holder PM for another attribute that
 *
 * @author olaf boede
 *
 * @param <T_VALUE> The (external) attribute value type.
 */
// TODO olaf: The implementation does not yet forward all calls to the delegate.
//            Check if it just should implement PmAttr to ensure complete interface coverage.
@PmAttrCfg(accessKind=AttrAccessKind.OVERRIDE)
public class PmAttrProxyImpl<T_VALUE> extends PmAttrImpl<T_VALUE> implements PmAttrProxy<T_VALUE> {

  /** The delegate to forward the calls to. */
  private PmAttr<T_VALUE>   delegate;

  /** Defines what to do in case of a missing delegate. */
  private OnMissingDelegate onMissingDelegate;

  /**
   * Creates an attribute proxy that is disabled if the {@link #delegate} is <code>null</code>.
   *
   * @param pmParent The PM tree parent.
   */
  public PmAttrProxyImpl(PmObject pmParent) {
    this(pmParent, OnMissingDelegate.DISABLE);
  }

  /**
   * @param pmParent The PM tree parent.
   * @param onMissingDelegate Defines the behavior of the attribute if the {@link #delegate} is <code>null</code>.
   */
  public PmAttrProxyImpl(PmObject pmParent, OnMissingDelegate onMissingDelegate) {
    super(pmParent);
    this.onMissingDelegate = onMissingDelegate;
  }

  /**
   * @return the delegate to forward the calls to.
   */
  @Override
  public PmAttr<? extends T_VALUE> getDelegate() {
    return delegate;
  }

  /**
   * @param delegate the delegate to forward the calls to.
   */
  @SuppressWarnings("unchecked")
  @Override
  public void setDelegate(PmAttr<? extends T_VALUE> delegate) {
    this.delegate = (PmAttr<T_VALUE>)delegate;
  }

  @Override
  protected T_VALUE getValueImpl() {
    return delegate != null
        ? delegate.getValue()
        : null;
  }

  @Override
  protected boolean setValueImpl(SetValueContainer<T_VALUE> value) {
    if (delegate == null) {
      throw new PmRuntimeException(this, "Unable to set an attribute value using a proxy that has no delegate.");
    }

    T_VALUE v = value.getPmValue();
    delegate.setValue(v);
    // a beforeValueChange check may have prevented the change.
    return ObjectUtils.equals(value.getPmValue(), delegate.getValue());
  }

  @SuppressWarnings("unchecked")
  @Override
  protected T_VALUE stringToValueImpl(String s) throws PmConverterException {
    return (delegate != null)
        ? ((PmAttrBase<T_VALUE, ?>)delegate).stringToValueImpl(s)
        : null;
  }

  @SuppressWarnings("unchecked")
  protected String valueToStringImpl(T_VALUE v) {
    return (delegate != null)
        ? ((PmAttrBase<T_VALUE, ?>)delegate).valueToStringImpl(v)
        : null;
  }

  @Override
  public void resetPmValues() {
    if (delegate != null) {
      delegate.resetPmValues();
    }
  }

  @Override
  protected PmOptionSet getOptionSetImpl() {
    return (delegate != null)
        ? delegate.getOptionSet()
        : null;
  }

  @Override
  protected boolean isPmEnabledImpl() {
      return delegate != null
        ? delegate.isPmEnabled()
        : false;
  }

  @Override
  protected boolean isPmReadonlyImpl() {
    return delegate != null
        ? delegate.isPmReadonly()
        : true;
  }

  @Override
  protected boolean isPmVisibleImpl() {
      return delegate != null
        ? delegate.isPmVisible()
        : onMissingDelegate != OnMissingDelegate.HIDE;
  }

  @Override
  protected String getPmTitleImpl() {
    return delegate != null
        ? delegate.getPmTitle()
        : super.getPmTitleImpl();
  }

  @Override
  protected String getPmTooltipImpl() {
    return delegate != null
        ? delegate.getPmTooltip()
        : super.getPmTitleImpl();
  }

  @Override
  protected void getPmStyleClassesImpl(Set<String> styleClassSet) {
    if (delegate != null) {
      styleClassSet.addAll(delegate.getPmStyleClasses());
    }
    else {
      super.getPmStyleClassesImpl(styleClassSet);
    }
  }
  
  @Override
  protected void validate(T_VALUE value) throws PmValidationException {
    if(delegate != null) {
      PmValidationApi.validateSubTree(delegate);
    }
  }

}

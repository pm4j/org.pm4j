package org.pm4j.core.pm.impl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.common.util.collection.MapUtil;
import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.exception.PmResourceData;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmAspect;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.PmVisitor;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmAttrCfg.Restriction;
import org.pm4j.core.pm.annotation.PmCacheCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg.CacheMode;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.impl.cache.PmCacheStrategy;
import org.pm4j.core.pm.impl.cache.PmCacheStrategyBase;
import org.pm4j.core.pm.impl.cache.PmCacheStrategyNoCache;
import org.pm4j.core.pm.impl.cache.PmCacheStrategyRequest;
import org.pm4j.core.pm.impl.converter.PmConverterErrorMessage;
import org.pm4j.core.pm.impl.converter.PmConverterOptionBased;
import org.pm4j.core.pm.impl.options.GenericOptionSetDef;
import org.pm4j.core.pm.impl.options.OptionSetDefNoOption;
import org.pm4j.core.pm.impl.options.PmOptionSetDef;
import org.pm4j.core.pm.impl.pathresolver.PassThroughPathResolver;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;
import org.pm4j.core.pm.impl.pathresolver.PmExpressionPathResolver;
import org.pm4j.core.util.reflection.BeanAttrAccessor;
import org.pm4j.core.util.reflection.BeanAttrAccessorImpl;
import org.pm4j.core.util.reflection.ClassUtil;
import org.pm4j.core.util.reflection.ReflectionException;
import org.pm4j.navi.NaviLink;

/**
 * <p> Basic implementation for PM attributes.  </p>
 *
 * <p> Note: If you are looking for a generic PmAttr implementation
 * for use in client code, use PmAttrImpl, not this base class.  </p>
 *
 * TODOC:
 *
 * @param <T_PM_VALUE>
 *          The external PM api type.<br>
 *          Examples:<br>
 *           For a string field: the type {@link String};<br>
 *           For a bean reference: the PM class for the referenced bean.
 * @param <T_BEAN_VALUE>
 *          The bean field type. <br>
 *          Examples:<br>
 *           For a string field: the type {@link String};<br>
 *           For a reference: The referenced class.
 *
 * @author olaf boede
 */
public abstract class PmAttrBase<T_PM_VALUE, T_BEAN_VALUE>
        extends PmDataInputBase
        implements PmAttr<T_PM_VALUE> {

  private static final Log LOG = LogFactory.getLog(PmAttrBase.class);

  /**
   * Indicates if the value was explicitly set. This information is especially
   * important for the default value logic. Default values may have only effect
   * on values that are not explicitly set.
   */
  private boolean valueWasSet = false;

  /**
   * Contains optional attribute data that in most cases doesn't exist for usual
   * bean attributes.
   */
  private PmAttrDataContainer<T_PM_VALUE, T_BEAN_VALUE> dataContainer;

  /**
   * Keeps a reference to the entered value in case of buffered data entry.
   */
  private Object bufferedValue = UNKNOWN_VALUE_INDICATOR;

  /** Shortcut to the next parent element. */
  private PmElement pmParentElement;

  /** The decorators to execute before and after setting the attribute value. */
  private Collection<PmCommandDecorator> valueChangeDecorators = Collections.emptyList();


  public PmAttrBase(PmObject pmParent) {
    super(pmParent);

    pmParentElement = (pmParent instanceof PmElement)
        ? (PmElement)pmParent
        : PmUtil.getPmParentOfType(pmParent, PmElement.class);
  }


  /**
   * @return The next parent of type {@link PmElement}.
   */
  protected PmElement getPmParentElement() {
    return pmParentElement;
  }

  @Override
  public final PmOptionSet getOptionSet() {
    MetaData md = getOwnMetaData();
    Object ov = md.cacheStrategyForOptions.getCachedValue(this);

    if (ov != PmCacheStrategy.NO_CACHE_VALUE) {
      // just return the cache hit (if there was one)
      return (PmOptionSet) ov;
    }
    else {
      try {
        PmOptionSet os = getOptionSetImpl();
        // TODO: ensure that there is an option for the current value.

        md.cacheStrategyForOptions.setAndReturnCachedValue(this, os);
        return os;
      }
      catch (RuntimeException e) {
        PmRuntimeException forwardedEx = PmRuntimeException.asPmRuntimeException(this, e);
        // TODO olaf: Logging is required here for JSF. Make it configurable for other
        // UI frameworks with better exception handling.
        LOG.error("getOptionSet failed", forwardedEx);
        throw forwardedEx;
      }
    }
  }

  /**
   * Override this method to provide a specific option set.
   * <p>
   * Alternatively you may use {@link PmOptionCfg} in combination with an
   * overridden {@link #getOptionValues()} method.
   *
   * @return An option set. In case of no options an empty option set.
   */
  protected PmOptionSet getOptionSetImpl() {
    PmOptionSet os = getOwnMetaData().optionSetDef.makeOptions(this);
    return os;
  }

  /**
   * A combination of {@link PmOptionCfg} and the implementation of this method
   * may be used to define the options for the attribute value.
   * <p>
   * The id, title and value attributes of the annotation will be applied to
   * the items of the provided object set to create the option set.
   *
   * @return The object to generate the options from.<br>
   *         May return <code>null</code> in case of no option values.
   */
  // XXX olaf: is currently public because of the package location of OptionSetDefBase.
  public Iterable<?> getOptionValues() {
    throw new PmRuntimeException(this, "Please don't forget to implement getOptionValues() if you don't specifiy the options in the @PmOptions annotation.");
  }

  // XXX olaf: is currently public because of the package location of OptionSetDefBase.
  /**
   * Provides the attribute type specific default definition, if an option set
   * should contain a <code>null</code> option definition or not.
   * <p>
   * Usualy non-list attributes provide the default
   * {@link PmOptionCfg.NullOption#FOR_OPTIONAL_ATTR} and list attributes
   * {@link PmOptionCfg.NullOption#NO}.
   *
   * @return The attribute type specific null-option generation default value.
   */
  public NullOption getNullOptionDefault() {
    return NullOption.FOR_OPTIONAL_ATTR;
  }

  /**
   * Checks first if the PM is enabled.<br>
   * Only if that's the case the logic provided by {@link #isPmReadonlyImpl()}
   * will be used.
   */
  @Override
  public final boolean isRequired() {
    return isPmEnabled() &&
           isRequiredImpl();
  }

  /**
   * Subclasses may implement here specific logic.
   * <p>
   * Please notice that the result of the external {@link #isRequired()} method
   * is influenced by the {@link #isPmEnabled()} result: A disabled attribute is
   * automatically NOT required.
   *
   * @return <code>true</code> if the attribute is required.
   */
  protected boolean isRequiredImpl() {
    MetaData md = getOwnMetaData();

    switch (md.valueRestriction) {
      case REQUIRED:            return true;
      case REQUIRED_IF_VISIBLE: return isPmVisible();
      case READ_ONLY:           return false;
      case NONE:                return md.required; // fall back for old annotation attribute
      default:         throw new PmRuntimeException(this, "Unknown enum value found.");
    }
  }

  @Override
  protected boolean isPmReadonlyImpl() {
    return super.isPmReadonlyImpl() ||
           getPmParentElement().isPmReadonly();
  }

  @Override
  public final boolean isPmEnabled() {
    // link enable status to read only even if the default impl of isPmEnabledImpl is overwritten
    return super.isPmEnabled() && !isPmReadonly();
  }

  // TODO olaf: move common logic to isPmVisible. Additional effort: ensure that isPmVisible stays final
  // for all PM sub classes.
  @Override
  protected boolean isPmVisibleImpl() {
    boolean visible = super.isPmVisibleImpl();

    if (visible && getOwnMetaData().hideWhenEmpty) {
      visible = !isEmptyValue(getValue());
    }

    return visible;
  }

  @Override
  protected void getPmStyleClassesImpl(Set<String> styleClassSet) {
    super.getPmStyleClassesImpl(styleClassSet);
    if (isRequired()) {
      styleClassSet.add(STYLE_CLASS_REQUIRED);
    }
  }

  @Override
  public void accept(PmVisitor visitor) {
    visitor.visit(this);
  }

  // ======= Messages =======

  @Override
  public void clearPmInvalidValues() {
    boolean wasValid = isPmValid();
    if (dataContainer != null) {
      if (dataContainer.invalidValue != null) {
        dataContainer.invalidValue = null;
      }
    }

    if (!wasValid) {
      for (PmMessage m : PmMessageUtil.getPmErrors(this)) {
        this.getPmConversationImpl()._clearPmMessage(m);
      }
      PmEventApi.firePmEvent(this, getOwnMetaData().validationChangeEventMask);
    }
  }

  /**
   * The default implementation compares the results of {@link #getValueLocalized()}
   * according to the collation sequence of the current {@link Locale}.
   */
  @Override
  public int compareTo(PmObject otherPm) {
    return PmUtil.getAbsoluteName(this).equals(PmUtil.getAbsoluteName(otherPm))
        ? CompareUtil.compare(getValueLocalized(), ((PmAttr<?>)otherPm).getValueLocalized(), getPmConversation().getPmLocale())
        : super.compareTo(otherPm);
  }

  /**
   * Checks if two instances represent the same value.
   * <p>
   * Sub classes may override this method to provide their specific equals-conditions.
   * <p>
   * This correct implementation of this method is important for the changed state handling.
   *
   * @see #isPmValueChanged()
   * @see #onPmValueChange(PmEvent)
   * @see PmEvent#VALUE_CHANGE
   *
   * @param v1 A value. May be <code>null</code>.
   * @param v2 Another value. May be <code>null</code>.
   * @return <code>true</code> if both parameters represent the same value.
   */
  protected boolean equalValues(T_PM_VALUE v1, T_PM_VALUE v2) {
    return ObjectUtils.equals(v1, v2);
  }

  // ======== Attribute value access ======== //

  @SuppressWarnings("unchecked")
  @Override
  public final T_PM_VALUE getValue() {
    MetaData md = getOwnMetaData();
    Object ov = md.cacheStrategyForValue.getCachedValue(this);

    if (ov != PmCacheStrategy.NO_CACHE_VALUE) {
      // just return the cache hit (if there was one)
      return (T_PM_VALUE) ov;
    }
    else {
      try {
        T_PM_VALUE v = null;

        if (isInvalidValue() &&
            dataContainer.invalidValue.isPmValueSet()) {
          v = dataContainer.invalidValue.getPmValue();
        }
        else {
          // In case of converter problems: Return the current value.
          v = getValueImpl();
        }

        return (T_PM_VALUE) md.cacheStrategyForValue.setAndReturnCachedValue(this, v);
      }
      catch (RuntimeException e) {
        PmRuntimeException forwardedEx = PmRuntimeException.asPmRuntimeException(this, e);
        // TODO olaf: Logging is required here for JSF. Make it configurable for other
        // UI frameworks with better exception handling.
        LOG.error("getValue failed", forwardedEx);
        throw forwardedEx;
      }
    }
  }

  @Override
  public final void setValue(T_PM_VALUE value) {
    if (!isPmReadonly()) {
      SetValueContainer<T_PM_VALUE> vc = SetValueContainer.makeWithPmValue(this, value);
      setValueImpl(vc);
    }
    else {
      // XXX olaf: is only a workaround for the standard jsf-form behavior...
      //           Approach: add a configuration parameter
      if (LOG.isInfoEnabled()) {
        LOG.info("Ignored setValue() call for read-only attribute: " + PmUtil.getPmLogString(this));
      }
    }
  }

  @Override
  public final String getValueAsString() {
    try {
      T_PM_VALUE value;
      if (isInvalidValue()) {
        if (dataContainer.invalidValue.isStringValueSet()) {
          return dataContainer.invalidValue.getStringValue();
        }
        else {
          value = dataContainer.invalidValue.getPmValue();
        }
      }
      else {
        value = getValue();
      }

      return value != null
                ? valueToStringImpl(value)
                : null;
    }
    catch (PmRuntimeException pmrex) {
      throw pmrex;
    }
    catch (RuntimeException e) {
      PmRuntimeException forwardedEx = PmRuntimeException.asPmRuntimeException(this, e);
      LOG.error("getValueAsString failed", forwardedEx);
      throw forwardedEx;
    }
  }

  /**
   * The default implementation returns the result of {@link #getValueAsString()}.<br>
   * If the attribute has options it tries to identify the selected option and returns the
   * title for the current option.
   */
  @Override
  public String getValueLocalized() {
    String valueAsString = getValueAsString();
    PmOptionSet os = getOptionSet();
    if (os != null) {
      PmOption option = os.findOptionForIdString(valueAsString);
      if (option != null) {
        return option.getPmTitle();
      }
    }
    // default:
    return valueAsString;
  }

  /**
   * The default implementation returns 0.
   */
  @Override
  public int getMinLen() {
    return getOwnMetaDataWithoutPmInitCall().minLen;
  }

  @Override
  public int getMaxLen() {
    return getOwnMetaDataWithoutPmInitCall().getMaxLen();
  }

  @Override
  public final void setValueAsString(String text) {
    zz_ensurePmInitialization();
    SetValueContainer<T_PM_VALUE> vc = new SetValueContainer<T_PM_VALUE>(this, text);

    try {
      if (isPmReadonly() &&
          getFormatString() != null) {
        // Some UI controls (e.g. SWT Text) send an immediate value change event when they get initialized.
        // To prevent unnecessary (and problematic) set value loops, nothing happens if the actual
        // value gets set to a read-only attribute.
        // In case of formatted string representations the value change detection mechanism on value level
        // may detect a change if the format does not represent all details of the actual value.
        // To prevent such effects, this code checks if the formatted string output is still the same...
        //
        // TODO: What about changing a
        if (! StringUtils.equalsIgnoreCase(getValueAsString(), vc.getStringValue())) {
          throw new PmRuntimeException(this, "Illegal attempt to set a new value to a read only attribute.");
        }
        return;
      }

      clearPmInvalidValues();
      try {
        vc.setPmValue(StringUtils.isNotBlank(text)
                          ? stringToValueImpl(text)
                          : null);
      } catch (PmRuntimeException e) {
        PmResourceData resData = e.getResourceData();
        if (resData == null) {
          setInvalidValue(vc);
          getPmConversationImpl().getPmExceptionHandler().onException(this, e, false);
        } else {
          setAndPropagateValueConverterMessage(vc, resData.msgKey, resData.msgArgs);
          if (LOG.isDebugEnabled()) {
            LOG.debug("String to value conversion failed in attribute '" + PmUtil.getPmLogString(this) + "'. String value: " + text);
          }
        }
        return;
      } catch (PmConverterException e) {
        PmResourceData resData = e.getResourceData();
        Object[] args = Arrays.copyOf(resData.msgArgs, resData.msgArgs.length+1);
        args[resData.msgArgs.length] = getPmShortTitle();
        setAndPropagateValueConverterMessage(vc, resData.msgKey, args);
        if (LOG.isDebugEnabled()) {
          LOG.debug("String to value conversion failed in attribute '" + PmUtil.getPmLogString(this) +
              "'. String value: '" + text +
              "'. Caused by: " + e.getMessage());
        }
        return;
      } catch (RuntimeException e) {
        setInvalidValue(vc);
        getPmConversationImpl().getPmExceptionHandler().onException(this, e, false);
        if (LOG.isDebugEnabled()) {
          LOG.debug("String to value conversion failed in attribute '" + PmUtil.getPmLogString(this) + "'. String value: " + text,
              e);
        }
        return;
      }

      setValueImpl(vc);
    }
    catch (RuntimeException e) {
      PmRuntimeException pme = PmRuntimeException.asPmRuntimeException(this, e);
      PmMessageUtil.makeExceptionMsg(this, Severity.ERROR, pme);
      LOG.error("setValueAsString failed to set value '" + vc.getStringValue() + "'", pme);
      throw pme;
    }
  }

  @Override
  public void resetPmValues() {
    boolean isWritable = !isPmReadonly();
    if (isWritable) {
      PmCacheApi.clearCachedPmValues(this);
      this.valueWasSet = false;
    }
    clearPmInvalidValues();
    if (isWritable) {
      setValue(getDefaultValue());
    }
  }

  @Override
  protected void clearCachedPmValues(Set<PmCacheApi.CacheKind> cacheSet) {
    super.clearCachedPmValues(cacheSet);
    MetaData sd = getOwnMetaData();

    if (cacheSet.contains(PmCacheApi.CacheKind.VALUE))
      sd.cacheStrategyForValue.clear(this);

    if (cacheSet.contains(PmCacheApi.CacheKind.OPTIONS))
      sd.cacheStrategyForOptions.clear(this);
  }

  /**
   * Gets attribute value directly from the bound data source. Does not use the
   * cache and does not consider any temporarily set invalid values.
   *
   * @return The current attribute value.
   */
  public final T_PM_VALUE getUncachedValidValue() {
    return getValueImpl();
  }

  protected T_PM_VALUE getValueImpl() {
    try {
      T_BEAN_VALUE beanAttrValue = getBackingValue();

      // TODO olaf: After conversion the value should be checked using
      // #isEmptyValue() to be able to control the default value logic precisely.
      if (beanAttrValue != null) {
        return convertBackingValueToPmValue(beanAttrValue);
      }
      else {
        // Default values may have only effect if the value was not set
        // by the user:
        if (valueWasSet) {
          return null;
        }
        else {
          T_PM_VALUE defaultValue = getDefaultValue();
          if (defaultValue != null) {
            beanAttrValue = convertPmValueToBackingValue(defaultValue);
            // XXX olaf: The backing value gets changed within the 'get' functionality.
            //           Check if that can be postponed...
            setBackingValue(beanAttrValue);
          }
          return defaultValue;
        }
      }
    }
    catch (Exception e) {
      throw PmRuntimeException.asPmRuntimeException(this, e);
    }
  }

  /**
   * Performs a smart set operation.
   * Validates the value before applying it.
   *
   * @param value The new value.
   * @return <code>true</code> when the attribute value was really changed.
   */
  protected boolean setValueImpl(SetValueContainer<T_PM_VALUE> value) {
    PmEventApi.ensureThreadEventSource(this);

    try {
      assert value.isPmValueSet();

      T_PM_VALUE newPmValue = value.getPmValue();
      T_PM_VALUE currentValue = getUncachedValidValue();
      // Check both values for null-value because they might be different but
      // may both represent a null-value.
      boolean pmValueChanged = (! equalValues(currentValue, newPmValue)) &&
                               (! (isEmptyValue(newPmValue) && isEmptyValue(currentValue)));

      ValueChangeCommandImpl<T_PM_VALUE> cmd = new ValueChangeCommandImpl<T_PM_VALUE>(this, currentValue, newPmValue);

      // New game. Forget all the old invalid stuff.
      clearPmInvalidValues();

      // Ensure that primitive types will not be set to null.
      if ((newPmValue == null) && getOwnMetaData().primitiveType) {
        setAndPropagateInvalidValue(value, PmConstants.MSGKEY_VALIDATION_MISSING_REQUIRED_VALUE);
        return false;
      }

      // FIXME olaf: read only control should be done within the calling setValueAsString method!
      //             The set operation should not be performed in this case. Check for side effects...
      if (pmValueChanged && isPmReadonly()) {
        PmMessageUtil.makeMsg(this, Severity.ERROR, PmConstants.MSGKEY_VALIDATION_READONLY);
        return false;
      }

      if (isValidatingOnSetPmValue()) {
        // Validate even if nothing was changed. The currentValue may be invalid too.
        // Example: New object with empty attributes values.
        try {
          validate(newPmValue);
        }
        catch (PmValidationException e) {
          PmResourceData resData = e.getResourceData();
          setAndPropagateInvalidValue(value, resData.msgKey, resData.msgArgs);
          return false;
        }
      }

      if (pmValueChanged) {
        if (!beforeValueChange(currentValue, newPmValue)) {
          LOG.debug("Value '" + getPmRelativeName() + "' was not changed because of the beforeDo result of the beforeValueChange() implementation");
          return false;
        }
        for (PmCommandDecorator d : getValueChangeDecorators()) {
          if (!d.beforeDo(cmd)) {
            LOG.debug("Value '" + getPmRelativeName() + "' was not changed because of the beforeDo result of decorator: " + d);
            return false;
          }
        }

        // TODO olaf: a quick hack to hide password data. Should be done more general for other field names too.
        if (LOG.isDebugEnabled() && !ObjectUtils.equals(getPmName(), "password")) {
          LOG.debug("Changing PM value of '" + PmUtil.getPmLogString(this) + "' from '" + currentValue + "' to '" + newPmValue + "'.");
        }

        T_BEAN_VALUE beanAttrValue = (newPmValue != null)
                        ? convertPmValueToBackingValue(newPmValue)
                        : null;
        setBackingValue(beanAttrValue);
        getOwnMetaData().cacheStrategyForValue.setAndReturnCachedValue(this, newPmValue);

        // From now on the value should be handled as intentionally modified.
        // That means that the default value shouldn't be returned, even if the
        // value was set to <code>null</code>.
        valueWasSet = true;

        setValueChanged(currentValue, newPmValue);
        for (PmCommandDecorator d : getValueChangeDecorators()) {
          d.afterDo(cmd);
        }
        afterValueChange(currentValue, newPmValue);

        PmEventApi.firePmEvent(this, PmEvent.VALUE_CHANGE);
        getPmConversation().getPmCommandHistory().commandDone(cmd);

        return true;
      }
      else {
        return false;
      }
    } catch (RuntimeException e) {
      getPmConversationImpl().getPmExceptionHandler().onException(this, e, false);
      return false;
    }
  }

  /**
   * Gets called before a value change will be applied.
   * <p>
   * It may be overridden for application specific needs.<br>
   * It may prevent the change to be applied by returning <code>false</code>.
   *
   * @param oldValue The old (current) attribute value.
   * @param newValue The new value that should be applied.
   * @return A return value <code>false</code> will prevent the value change.
   */
  protected boolean beforeValueChange(T_PM_VALUE oldValue, T_PM_VALUE newValue) {
    return true;
  }

  /**
   * Gets called after a value change.
   *
   * @param oldValue The old attribute value.
   * @param newValue The new (current) value.
   */
  protected void afterValueChange(T_PM_VALUE oldValue, T_PM_VALUE newValue) {
  }

  /**
   * Gets called whenever a string value needs to be converted to the attribute value type.
   * <p>
   * The default implementation uses the converter provided by {@link #getConverter()}.
   *
   * @param s The string to convert.
   * @return The converted value.
   * @throws PmConverterException If the given string can't be converted.
   */
  protected T_PM_VALUE stringToValueImpl(String s) throws PmConverterException {
    return (T_PM_VALUE) getConverter().stringToValue(this, s);
  }

  /**
   * Gets called whenever the attribute value needs to be represented as a string.
   * <p>
   * The default implementation uses the converter provided by {@link #getConverter()}.
   *
   * @param v A value to convert.
   * @return The string representation.
   */
  protected String valueToStringImpl(T_PM_VALUE v) {
    return getConverter().valueToString(this, v);
  }

  /**
   * @return The converter that translates from and to the corresponding string representation.
   */
  @SuppressWarnings("unchecked")
  protected Converter<T_PM_VALUE> getConverter() {
    Converter<T_PM_VALUE> c = (Converter<T_PM_VALUE>) getOwnMetaData().converter;
    if (c == null) {
      throw new PmRuntimeException(this, "Missing value converter.");
    }
    return c;
  }

  @Override
  protected boolean isPmValueChangedImpl() {
    return  (dataContainer != null &&
             dataContainer.originalValue != UNCHANGED_VALUE_INDICATOR) ||
             super.isPmValueChangedImpl();
  }


  // TODO olaf: distinguish explicite changed flag and value change.
  @Override
  protected void setPmValueChangedImpl(boolean newChangedState) {
    PmEventApi.ensureThreadEventSource(this);
    setValueChanged(UNKNOWN_VALUE_INDICATOR, newChangedState
            ? CHANGED_VALUE_INDICATOR
            : UNCHANGED_VALUE_INDICATOR);
    super.setPmValueChangedImpl(newChangedState);
  }

  static final String UNCHANGED_VALUE_INDICATOR = "### the attribute value is not marked as changed ###";
  static final String CHANGED_VALUE_INDICATOR   = "### the attribute value is marked as changed ###";
  static final String UNKNOWN_VALUE_INDICATOR   = "### the original attribute value is unknown ###";

  /**
   * Detects a change by checking the <code>oldValue</code> and <code>newValue</code>
   * parameters.
   * <p>
   * Passing {@link #UNCHANGED_VALUE_INDICATOR} as <code>newValue</code> causes
   * a change of the internal managed <code>originalValue</code> to 'unchanged'.<br>
   * This way the current attribute value gets accepted as the 'original unchange'
   * value.
   *
   * @param oldValue The old value.
   * @param newValue The new value.
   */
  private void setValueChanged(Object oldValue, Object newValue) {
    boolean fireStateChange = false;
    // Reset to an unchanged value state. Accepts the current value as the original one.
    if (newValue == UNCHANGED_VALUE_INDICATOR) {
      // prevent creation of a data container only to store the default value...
      if (dataContainer != null) {
        fireStateChange = (dataContainer.originalValue != UNCHANGED_VALUE_INDICATOR);
        dataContainer.originalValue = UNCHANGED_VALUE_INDICATOR;
      }
    }
    // Set the value:
    else {
      // Setting the attribute to the original value leads to an 'unchanged' state.
      if (ObjectUtils.equals(zz_getDataContainer().originalValue, newValue)) {
        fireStateChange = (dataContainer.originalValue != UNCHANGED_VALUE_INDICATOR);
        dataContainer.originalValue = UNCHANGED_VALUE_INDICATOR;
      }
      // Setting a value which is not the original one:
      else {
        // Remember the current value as the original one if the attribute was in an unchanged
        // state:
        if (dataContainer.originalValue == UNCHANGED_VALUE_INDICATOR) {
          fireStateChange = true;
          dataContainer.originalValue = oldValue;

        }
        else {
          // If the attribute gets changed back to its original value: Mark it as unchanged.
          if (ObjectUtils.equals(dataContainer.originalValue, newValue)) {
            fireStateChange = true;
            dataContainer.originalValue = UNCHANGED_VALUE_INDICATOR;
          }
        }
      }
    }

    if (fireStateChange) {
      PmEventApi.firePmEvent(this, PmEvent.VALUE_CHANGED_STATE_CHANGE);
    }
  }

  /**
   * The default implementation provides the default value provided by the
   * annotation {@link PmAttrCfg#defaultValue()}.
   * <p>
   * Subclasses may override the method {@link #getDefaultValueImpl()} to provide
   * some special implementation.
   *
   * @return The default value for this attribute.
   */
  protected final T_PM_VALUE getDefaultValue() {
    return getDefaultValueImpl();
  }

  /**
   * Reads a PM request attribute with the name of the attribute.
   *
   * @return The read request attribute, converted to the attribute specific
   *         type. <code>null</code> if there is no default value attribute
   *         within the given request.
   * @deprecated remove as soon as a customer scenario is cleaned.
   */
  private T_PM_VALUE getDefaultValueFromRequest() {
    String reqValue = getPmConversationImpl().getPmToViewTechnologyConnector().readRequestValue(getPmName());
    try {
      return (reqValue != null)
                ? stringToValueImpl(reqValue)
                : null;
    } catch (PmConverterException e) {
      throw new PmRuntimeException(this, e);
    }
  }

  /**
   * The default implementation provides the value provided by {@link PmAttrCfg#defaultValue()}.
   * <p>
   * Subclasses may override this method to provide some special implementation.
   *
   * @return The default value for this attribute.
   */
  // FIXME olaf: shouldn't that method return T_BEAN_VALUE ?
  //             not yet changed because of the effort to change the meta data handling code...
  // alternatively: add a second method getDefaultBackingValue()...
  protected T_PM_VALUE getDefaultValueImpl() {
    MetaData md = getOwnMetaData();
    T_PM_VALUE defaultValue = null;

    if (StringUtils.isNotBlank(md.defaultValueString)) {
      try {
        defaultValue = stringToValueImpl(md.defaultValueString);
      } catch (PmConverterException e) {
        throw new PmRuntimeException(this, e);
      }
    }

    return (T_PM_VALUE) (defaultValue != null
            ? defaultValue
            : getDefaultValueFromRequest());
  }

  /**
   * Sets the invalid value and propagates a {@link PmValidationMessage} to the
   * conversation.
   *
   * @param invValue
   *          The invalid value.
   * @param msgKey
   *          Key for the user message.
   * @param msgArgs
   *          Values for the user message.
   */
  private void setAndPropagateInvalidValue(SetValueContainer<T_PM_VALUE> invValue, String msgKey, Object... msgArgs) {
    this.getPmConversationImpl().addPmMessage(new PmValidationMessage(this, invValue, msgKey, msgArgs));
    setInvalidValue(invValue);
  }

  /**
   * Sets the invalid value and notifies listeners
   *
   * @param invValue
   *          The invalid value.
   */
  private void setInvalidValue(SetValueContainer<T_PM_VALUE> invValue) {
    zz_getDataContainer().invalidValue = invValue;
    PmEventApi.firePmEvent(this, getOwnMetaData().validationChangeEventMask);
  }

  /**
   * @param invValue
   *          The invalid value.
   * @param msgKey
   *          Key for the user message.
   * @param msgArgs
   *          Values for the user message.
   */
  private void setAndPropagateValueConverterMessage(SetValueContainer<T_PM_VALUE> invValue, String msgKey,
      Object... msgArgs) {
    this.getPmConversationImpl().addPmMessage(new PmConverterErrorMessage(this, invValue, msgKey, msgArgs));
    setInvalidValue(invValue);
  }


  /**
   * @return <code>true</code> when there is an invalid user data entry.
   */
  private final boolean isInvalidValue() {
    return (dataContainer != null) && (dataContainer.invalidValue != null);
  }

  /**
   * Checks the attribute type specific <code>null</code> or empty value
   * condition.
   *
   * @return <code>true</code> when the value is a <code>null</code> or empty
   *         value equivalent.
   */
  protected boolean isEmptyValue(T_PM_VALUE value) {
    return (value == null);
  }

  /**
   * If this method returns <code>true</code>, each {@link #setValue(Object)} will cause an
   * immediate call to {@link #pmValidate()}.
   * <p>
   * Alternatively validation is usually triggered by a command.
   */
  protected boolean isValidatingOnSetPmValue() {
    return false;
  }

  /**
   * The default validation checks just the required condition.
   * More specific attribute classes have to add their specific validation
   * by overriding this method.
   *
   * @param value The value to validate.
   */
  protected void validate(T_PM_VALUE value) throws PmValidationException {
    if (isRequired() &&
        isEmptyValue(value)) {
      throw new PmValidationException(PmMessageUtil.makeRequiredWarning(this));
    }
  }

  void performJsr303Validations() {
    Validator validator = PmImplUtil.getBeanValidator();
    if (validator != null) {
      Object validationBean = getOwnMetaData().valueAccessStrategy.getPropertyContainingBean(this);
      if (validationBean != null &&
          getOwnMetaData().validationFieldName != null) {
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>)(Object)validator.validateProperty(validationBean, getOwnMetaData().validationFieldName);
        PmImplUtil.beanConstraintViolationsToPmMessages(this, violations);
      }
    }
  }

  @Override
  public void pmValidate() {
    if (isPmVisible() &&
        !isPmReadonly()) {
      // A validation can only be performed if the last setValue() did not generate a converter exception.
      // Otherwise the attribute will simply stay in its value converter error state.
      if (!hasPmConverterErrors()) {
        boolean wasValid = isPmValid();
        PmConversationImpl conversation = getPmConversationImpl();
        conversation.clearPmMessages(this, null);
        try {
          validate(getValue());
          performJsr303Validations();
        }
        catch (PmValidationException e) {
          PmResourceData exResData = e.getResourceData();
          PmValidationMessage msg = new PmValidationMessage(this, exResData.msgKey, exResData.msgArgs);
          conversation.addPmMessage(msg);
        }

        boolean isValid = isPmValid();
        if (isValid != wasValid) {
          PmEventApi.firePmEvent(this, getOwnMetaData().validationChangeEventMask);
        }
      }
    }
  }

  private boolean hasPmConverterErrors() {
    for (PmMessage m : getPmConversation().getPmMessages(this, Severity.ERROR)) {
      if (m instanceof PmConverterErrorMessage) {
        return true;
      }
    }
    // no converter message found.
    return false;
  }

  // ======== Buffered data input support ======== //

  public boolean isBufferedPmValueMode() {
    return getPmParentElement().isBufferedPmValueMode();
  }

  @SuppressWarnings("unchecked")
  public void commitBufferedPmChanges() {
    if (isBufferedPmValueMode() &&
        bufferedValue != UNKNOWN_VALUE_INDICATOR) {
      setBackingValueImpl((T_BEAN_VALUE)bufferedValue);
      bufferedValue = UNKNOWN_VALUE_INDICATOR;
    }
  }

  public void rollbackBufferedPmChanges() {
    bufferedValue = UNKNOWN_VALUE_INDICATOR;
  }

  // ======== Attribute raw data access ======== //

  @SuppressWarnings("unchecked")
  public T_PM_VALUE convertBackingValueToPmValue(T_BEAN_VALUE backingValue) {
    return (T_PM_VALUE) backingValue;
  }

  @SuppressWarnings("unchecked")
  public T_BEAN_VALUE convertPmValueToBackingValue(T_PM_VALUE pmAttrValue) {
    return (T_BEAN_VALUE) pmAttrValue;
  }

  @SuppressWarnings("unchecked")
  public final T_BEAN_VALUE getBackingValue() {
    return (bufferedValue != UNKNOWN_VALUE_INDICATOR)
         ? (T_BEAN_VALUE)bufferedValue
         : getBackingValueImpl();
  }

  public final void setBackingValue(T_BEAN_VALUE value) {
    if (isBufferedPmValueMode()) {
      bufferedValue = value;
    }
    else {
      setBackingValueImpl(value);
    }
  }

  /**
   * Provides the internal (non PM) data type representation of the attribute value.
   * <p>
   * Attributes may override this method to provide a specific implementation.
   *
   * @return The value (internal data type representation).
   */
  @SuppressWarnings("unchecked")
  protected T_BEAN_VALUE getBackingValueImpl() {
    return (T_BEAN_VALUE) getOwnMetaData().valueAccessStrategy.getValue(this);
  }

  /**
   * Sets the internal (non PM) data type representation of the attribute value.
   * <p>
   * Attributes may override this method to provide a specific implementation.
   *
   * @param value The value to assign (internal data type representation).
   */
  protected void setBackingValueImpl(T_BEAN_VALUE value) {
    getOwnMetaData().valueAccessStrategy.setValue(this, value);
  }

  @Override
  public String getFormatString() {
    String key = getOwnMetaData().formatResKey;
    String format = null;

    // 1. fix resource key definition
    if (key != null) {
      format = PmLocalizeApi.findLocalization(this, key);
      if (format == null) {
        throw new PmRuntimeException(this, "No resource string found for configured format resource key '" + key +"'.");
      }
    }
    // 2. no fix key: try to find a postfix based definition
    else {
      key = getPmResKey() + PmConstants.RESKEY_POSTFIX_FORMAT;
      format = PmLocalizeApi.findLocalization(this, key);

      // 3. Try a default key (if defined for this attribute)
      if (format == null) {
        key = getFormatDefaultResKey();
        if (key != null) {
          format = PmLocalizeApi.findLocalization(this, key);
        }
      }
    }

    return format;
  }

  /**
   * Concrete attribute classes may specify here a default format resource key
   * as a fallback for unspecified format localizations.
   *
   * @return The fallback resource key or <code>null</code> if there is none.
   */
  protected String getFormatDefaultResKey() {
    return null;
  }

  /* package */ void addValueChangeDecorator(PmCommandDecorator decorator) {
    if (valueChangeDecorators.isEmpty()) {
      valueChangeDecorators = new ArrayList<PmCommandDecorator>();
    }
    valueChangeDecorators.add(decorator);
  }

  @Override
  Serializable getPmContentAspect(PmAspect aspect) {
    switch (aspect) {
      case VALUE:
        T_PM_VALUE v = getValue();
        return getConverter().valueToSerializable(this, v);
      default:
        return super.getPmContentAspect(aspect);
    }
  }

  /**
   * @return The set of decorators to consider on value change.
   */
  protected Collection<PmCommandDecorator> getValueChangeDecorators() {
    return valueChangeDecorators;
  }

  @Override
  void setPmContentAspect(PmAspect aspect, Serializable value) throws PmConverterException {
    PmEventApi.ensureThreadEventSource(this);
    switch (aspect) {
      case VALUE:
        setValue(getConverter().serializeableToValue(this, value));
        break;
      default:
        super.setPmContentAspect(aspect, value);
    }
  }

  /**
   * Provides a data container. Creates it on the fly when it does not already
   * exist.
   *
   * @return The container for optional data parts.
   */
  private final PmAttrDataContainer<T_PM_VALUE, T_BEAN_VALUE> zz_getDataContainer() {
    if (dataContainer == null) {
      dataContainer = new PmAttrDataContainer<T_PM_VALUE, T_BEAN_VALUE>();
    }
    return dataContainer;
  }

  // ======== meta data ======== //

  // XXX olaf: really required? May be solved by overriding initMetaData too.
  protected PmOptionSetDef<?> makeOptionSetDef(PmOptionCfg cfg, Method getOptionValuesMethod) {
    return cfg != null
              ? new GenericOptionSetDef(cfg, getOptionValuesMethod)
              : OptionSetDefNoOption.INSTANCE;
  }

  @SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);

    MetaData myMetaData = (MetaData) metaData;

    Class<?> beanClass = (getPmParent() instanceof PmBean)
          ? ((PmBean)getPmParent()).getPmBeanClass()
          : null;

    PmAttrCfg fieldAnnotation = AnnotationUtil.findAnnotation(this, PmAttrCfg.class);

    zz_readBeanValidationRestrictions(beanClass, fieldAnnotation, myMetaData);

    // read the option configuration first from the getOptionValues()
    // method. If not found there: From the attribute- and class declaration.
    Method getOptionValuesMethod = null;
    PmOptionCfg optionCfg = null;
    try {
      getOptionValuesMethod = getClass().getMethod("getOptionValues");
      if (getOptionValuesMethod.getDeclaringClass() != PmAttrBase.class) {
        optionCfg = getOptionValuesMethod.getAnnotation(PmOptionCfg.class);
        // XXX: not really fine for security contexts.
        getOptionValuesMethod.setAccessible(true);
      }
      else {
        getOptionValuesMethod = null;
      }
    } catch (Exception e) {
      throw new PmRuntimeException(this, "Unable to access method 'getOptionValues'.", e);
    }

    if (optionCfg == null) {
      optionCfg = AnnotationUtil.findAnnotation(this, PmOptionCfg.class);
    }

    myMetaData.optionSetDef = (PmOptionSetDef)
          makeOptionSetDef(optionCfg, getOptionValuesMethod);

    if (myMetaData.optionSetDef != OptionSetDefNoOption.INSTANCE) {
      myMetaData.setItemConverter(
          new PmConverterOptionBased(optionCfg != null ? optionCfg.id() : ""));
    }
    // TODO olaf: implement a simplified and more consistent option implementation...
    if (optionCfg != null) {
      myMetaData.nullOption = optionCfg.nullOption();
    }


    PmAttrCfg.AttrAccessKind accessKindCfgValue = PmAttrCfg.AttrAccessKind.DEFAULT;
    boolean useReflection = true;
    if (fieldAnnotation != null) {
      myMetaData.hideWhenEmpty = fieldAnnotation.hideWhenEmpty();
      myMetaData.setReadOnly((fieldAnnotation.valueRestriction() == Restriction.READ_ONLY) || fieldAnnotation.readOnly());

      // The pm can force more constraints. It should not define less constraints as
      // the bean validation definition:
      if (fieldAnnotation.required()) {
        myMetaData.required = true;
      }
      // TODO: replace 'required' and 'readOnly'.
      if (fieldAnnotation.valueRestriction() != Restriction.NONE) {
        myMetaData.valueRestriction = fieldAnnotation.valueRestriction();
      }

      accessKindCfgValue = fieldAnnotation.accessKind();
      myMetaData.formatResKey = StringUtils.defaultIfEmpty(fieldAnnotation.formatResKey(), null);
      if (StringUtils.isNotEmpty(fieldAnnotation.defaultValue())) {
        myMetaData.defaultValueString = fieldAnnotation.defaultValue();
      }

      myMetaData.maxLen = fieldAnnotation.maxLen();
      myMetaData.minLen = fieldAnnotation.minLen();
      if (myMetaData.maxLen != -1 &&
          myMetaData.minLen > myMetaData.maxLen) {
        throw new PmRuntimeException(this, "minLen(" + myMetaData.minLen +
                                           ") > maxLen(" + myMetaData.maxLen + ")");
      }

      switch (accessKindCfgValue) {
        case DEFAULT:
          if (StringUtils.isNotBlank(fieldAnnotation.valuePath())) {
            myMetaData.valuePathResolver = PmExpressionPathResolver.parse(fieldAnnotation.valuePath(), true);
            int lastDotPos = fieldAnnotation.valuePath().lastIndexOf('.');
            if (lastDotPos > 0) {
              String parentPath = fieldAnnotation.valuePath().substring(0, lastDotPos);
              myMetaData.valueContainingObjPathResolver = PmExpressionPathResolver.parse(parentPath, true);
            }
            useReflection = false;
            myMetaData.valueAccessStrategy = ValueAccessByExpression.INSTANCE;
          }
          break;
        case OVERRIDE:
          useReflection = false;
          myMetaData.valueAccessStrategy = ValueAccessOverride.INSTANCE;
          break;
        case SESSIONPROPERTY:
          useReflection = false;
          myMetaData.valueAccessStrategy = ValueAccessSessionProperty.INSTANCE;
          break;
        case LOCALVALUE:
          useReflection = false;
          myMetaData.valueAccessStrategy = ValueAccessLocal.INSTANCE;
          break;

        default:
          throw new PmRuntimeException(this, "Unknown annotation kind: " + fieldAnnotation.accessKind());
      }
    }

    // Automatic reflection access is only supported for fix PmAttr fields in a PmBean container:
    if (useReflection  &&
        myMetaData.isPmField &&
        beanClass != null) {
      try {
        myMetaData.beanAttrAccessor = new BeanAttrAccessorImpl(beanClass, getPmName());

        if (myMetaData.beanAttrAccessor.getFieldClass().isPrimitive()) {
          myMetaData.primitiveType = true;
          if (fieldAnnotation == null) {
            myMetaData.required = true;
          }
        }

        myMetaData.valueAccessStrategy = ValueAccessReflection.INSTANCE;
      }
      catch (ReflectionException e) {
        if (ClassUtil.isMethodOverridden(PmAttrBase.class, getClass(), "getBackingValueImpl")) {
          myMetaData.valueAccessStrategy = ValueAccessOverride.INSTANCE;
        } else {
          PmObjectUtil.throwAsPmRuntimeException(this, e);
        }
      }
    }

    // Use default attribute title provider if no specific provider was configured.
    if (metaData.getPmTitleProvider() == getPmConversation().getPmDefaults().getPmTitleProvider()) {
      metaData.setPmTitleProvider(getPmConversation().getPmDefaults().getPmAttrTitleProvider());
    }

    // -- Cache configuration --
    List<PmCacheCfg> cacheAnnotations = new ArrayList<PmCacheCfg>();
    AnnotationUtil.findAnnotationsInPmHierarchy(this, PmCacheCfg.class, cacheAnnotations);

    myMetaData.cacheStrategyForOptions = readCacheStrategy(PmCacheCfg.ATTR_OPTIONS, cacheAnnotations, CACHE_STRATEGIES_FOR_OPTIONS);
    myMetaData.cacheStrategyForValue = readCacheStrategy(PmCacheCfg.ATTR_VALUE, cacheAnnotations, CACHE_STRATEGIES_FOR_VALUE);

  }


  private void zz_readBeanValidationRestrictions(Class<?> beanClass, PmAttrCfg fieldAnnotation, MetaData myMetaData) {
    if (PmImplUtil.getBeanValidator() == null)
      return;

    Class<?> srcClass = ((fieldAnnotation != null) &&
                         (fieldAnnotation.beanInfoClass() != Void.class))
          ? fieldAnnotation.beanInfoClass()
          : beanClass;

    if (srcClass != null) {
      BeanDescriptor beanDescriptor = PmImplUtil.getBeanValidator().getConstraintsForClass(srcClass);

      if (beanDescriptor != null) {
        myMetaData.validationFieldName = (fieldAnnotation != null) &&
            (StringUtils.isNotBlank(fieldAnnotation.beanInfoField()))
           ? fieldAnnotation.beanInfoField()
           : myMetaData.getName();

        PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty(myMetaData.validationFieldName);
        if (propertyDescriptor == null ||
            propertyDescriptor.getConstraintDescriptors().isEmpty()) {
          myMetaData.validationFieldName = null;
        }
        else {
          for (ConstraintDescriptor<?> cd : propertyDescriptor.getConstraintDescriptors()) {
            initMetaDataBeanConstraint(cd);
          }
        }
      }
    }
  }

  /**
   * Gets called for each found {@link ConstraintDescriptor}.<br>
   * The default implementation just checks the {@link NotNull} restrictions.<br/>
   * Sub classes override this method to consider other restrictions.
   *
   * @param cd The {@link ConstraintDescriptor} to consider for this attribute.
   */
  protected void initMetaDataBeanConstraint(ConstraintDescriptor<?> cd) {
    MetaData metaData = getOwnMetaDataWithoutPmInitCall();
    if (cd.getAnnotation() instanceof NotNull) {
      metaData.valueRestriction = Restriction.REQUIRED;
    }
    else if (cd.getAnnotation() instanceof Size) {
      Size annotation = (Size)cd.getAnnotation();

      if (annotation.min() > 0) {
        metaData.minLen = annotation.min();
      }
      if (annotation.max() < Integer.MAX_VALUE) {
        metaData.maxLen = annotation.max();
      }
    }
  }

  /**
   * Shared meta data for all attributes of the same kind.
   * E.g. for all 'myapp.User.name' attributes.
   */
  protected static class MetaData extends PmObjectBase.MetaData {
    static final Object                     NOT_INITIALIZED         = "NOT_INITIALIZED";

    private BeanAttrAccessor                beanAttrAccessor;
    private PmOptionSetDef<PmAttr<?>>       optionSetDef            = OptionSetDefNoOption.INSTANCE;
    private PmOptionCfg.NullOption          nullOption              = NullOption.DEFAULT;
    private boolean                         hideWhenEmpty;
    private boolean                         required;
    private Restriction                     valueRestriction        = Restriction.NONE;
    private boolean                         primitiveType;
    private PathResolver                    valuePathResolver;
    private PathResolver                    valueContainingObjPathResolver = PassThroughPathResolver.INSTANCE;
    private String                          formatResKey;
    private String                          defaultValueString;
    private PmCacheStrategy                 cacheStrategyForOptions = PmCacheStrategyNoCache.INSTANCE;
    private PmCacheStrategy                 cacheStrategyForValue   = PmCacheStrategyNoCache.INSTANCE;
    private Converter<?>                    converter;
    private BackingValueAccessStrategy      valueAccessStrategy     = ValueAccessLocal.INSTANCE;
    /** Name of the field configured for JSR 303-validation.<br>
     * Is <code>null</code> if there is nothing to validate this way. */
    private String                          validationFieldName;
    private int                             maxLen                  = -1;
    private int                             minLen                  = 0;
    private int                             maxLenDefault;

    /**
     * @param maxDefaultLen the attribute type specific maximum number of characters.
     */
    public MetaData(int maxDefaultLen) {
      this.maxLenDefault = maxDefaultLen;
    }

    /** @return The statically defined option set algorithm. */
    public PmOptionSetDef<PmAttr<?>> getOptionSetDef() { return optionSetDef; }
    public PmOptionCfg.NullOption getNullOption() { return nullOption; }

    public String getFormatResKey() { return formatResKey; }
    public void setFormatResKey(String formatResKey) { this.formatResKey = formatResKey; }

    public PmCacheStrategy getCacheStrategyForOptions() { return cacheStrategyForOptions; }
    public PmCacheStrategy getCacheStrategyForValue() { return cacheStrategyForValue; }

    public Converter<?> getConverter() { return converter; }
    public void setConverter(Converter<?> converter) { this.converter = converter; }

    /**
     * Multi value attributes (like lists) have specific item converters.
     * <p>
     * For single value attributes there is no difference between
     * {@link #converter} and the <code>itemConverter</code>.
     *
     * @param converter The converter for attribute items.
     */
    public void setItemConverter(Converter<?> converter) { setConverter(converter); }

    /** @see #setItemConverter(org.pm4j.core.pm.PmAttr.Converter) */
    public Converter<?> getItemConverter() { return getConverter(); }

    /**
     * If the converter was not explicitly defined by a user annotation, this
     * method will be used to define a attribute type specific converter.
     */
    public void setConverterDefault(Converter<?> converter) {
      assert converter != null;
      if (this.converter == null)
        this.converter = converter;
    }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public int getMinLen() { return minLen; }

    public int getMaxLen() {
      if (maxLen == -1) {
        maxLen = getMaxLenDefault();
      }
      return maxLen;
    }

    /**
     * Provides the attribute type specific default max length.
     *
     * @return The maximal number of characters default.
     */
    protected int getMaxLenDefault() {
      return maxLenDefault;
    }
  }

  /**
   * The default implementation defines meta data for an attribute with
   * 'unlimited' length ({@link Short#MAX_VALUE}).
   */
  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData(Short.MAX_VALUE);
  }


  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }

  private final MetaData getOwnMetaDataWithoutPmInitCall() {
    return (MetaData) getPmMetaDataWithoutPmInitCall();
  }

  // ====== Cache strategies ====== //

  private static final PmCacheStrategy CACHE_VALUE_LOCAL = new PmCacheStrategyBase<PmAttrBase<?,?>>("CACHE_VALUE_LOCAL") {
    @Override protected Object readRawValue(PmAttrBase<?, ?> pm) {
      return (pm.dataContainer != null)
                ? pm.dataContainer.cachedValue
                : null;
    }
    @Override protected void writeRawValue(PmAttrBase<?, ?> pm, Object value) {
      pm.zz_getDataContainer().cachedValue = value;
    }
    @Override protected void clearImpl(PmAttrBase<?, ?> pm) {
      if (pm.dataContainer != null) {
        pm.dataContainer.cachedValue = null;
      }
    }
  };

  private static final PmCacheStrategy CACHE_OPTIONS_LOCAL = new PmCacheStrategyBase<PmAttrBase<?,?>>("CACHE_OPTIONS_LOCAL") {
    @Override protected Object readRawValue(PmAttrBase<?, ?> pm) {
      return (pm.dataContainer != null)
                ? pm.dataContainer.cachedOptionSet
                : null;
    }
    @Override protected void writeRawValue(PmAttrBase<?, ?> pm, Object value) {
      pm.zz_getDataContainer().cachedOptionSet = value;
    }
    @Override protected void clearImpl(PmAttrBase<?, ?> pm) {
      if (pm.dataContainer != null) {
        pm.dataContainer.cachedOptionSet = null;
      }
    }
  };


  private static final Map<CacheMode, PmCacheStrategy> CACHE_STRATEGIES_FOR_VALUE =
    MapUtil.makeFixHashMap(
      CacheMode.OFF,      PmCacheStrategyNoCache.INSTANCE,
      CacheMode.ON,    CACHE_VALUE_LOCAL,
      CacheMode.REQUEST,  new PmCacheStrategyRequest("CACHE_VALUE_IN_REQUEST", "v")
    );

  private static final Map<CacheMode, PmCacheStrategy> CACHE_STRATEGIES_FOR_OPTIONS =
    MapUtil.makeFixHashMap(
        CacheMode.OFF,      PmCacheStrategyNoCache.INSTANCE,
        CacheMode.ON,    CACHE_OPTIONS_LOCAL,
        CacheMode.REQUEST,  new PmCacheStrategyRequest("CACHE_OPTIONS_IN_REQUEST", "os")
      );

  // ====== Backing value access strategies ====== //

  interface BackingValueAccessStrategy {
    Object getValue(PmAttrBase<?, ?> attr);
    void setValue(PmAttrBase<?, ?> attr, Object value);
    Object getPropertyContainingBean(PmAttrBase<?, ?> attr);
  }

  static class ValueAccessLocal implements BackingValueAccessStrategy {
    static final BackingValueAccessStrategy INSTANCE = new ValueAccessLocal();

    @Override
    public Object getValue(PmAttrBase<?, ?> attr) {
      return attr.dataContainer != null
                    ? attr.dataContainer.localValue
                    : null;
    }

    @Override
    public void setValue(PmAttrBase<?, ?> attr, Object value) {
      attr.zz_getDataContainer().localValue = value;
    }

    @Override
    public Object getPropertyContainingBean(PmAttrBase<?, ?> attr) {
      return null;
    }
  }

  static class ValueAccessSessionProperty implements BackingValueAccessStrategy {
    static final BackingValueAccessStrategy INSTANCE = new ValueAccessSessionProperty();

    @Override
    public Object getValue(PmAttrBase<?, ?> attr) {
      return attr.getPmConversation().getPmNamedObject(PmUtil.getAbsoluteName(attr));
    }

    @Override
    public void setValue(PmAttrBase<?, ?> attr, Object value) {
      attr.getPmConversation().setPmNamedObject(PmUtil.getAbsoluteName(attr), value);
    }

    @Override
    public Object getPropertyContainingBean(PmAttrBase<?, ?> attr) {
      return null;
    }
  }

  // TODO: should disappear as soon as the value strategy configuration is complete.
  static class ValueAccessOverride implements BackingValueAccessStrategy {
    static final BackingValueAccessStrategy INSTANCE = new ValueAccessOverride();

    @Override
    public Object getValue(PmAttrBase<?, ?> attr) {
      throw new PmRuntimeException(attr, "getBackingValueImpl() method is not implemented.");
    }

    @Override
    public void setValue(PmAttrBase<?, ?> attr, Object value) {
      throw new PmRuntimeException(attr, "setBackingValueImpl() method is not implemented.");
    }

    @Override
    public Object getPropertyContainingBean(PmAttrBase<?, ?> attr) {
      return null;
    }
  }

  static class ValueAccessByExpression implements BackingValueAccessStrategy {
    static final BackingValueAccessStrategy INSTANCE = new ValueAccessByExpression();

    @Override
    public Object getValue(PmAttrBase<?, ?> attr) {
      return attr.getOwnMetaData().valuePathResolver.getValue(attr.getPmParent());
    }

    @Override
    public void setValue(PmAttrBase<?, ?> attr, Object value) {
      attr.getOwnMetaData().valuePathResolver.setValue(attr.getPmParent(), value);
    }

    @Override
    public Object getPropertyContainingBean(PmAttrBase<?, ?> attr) {
      return attr.getOwnMetaData().valueContainingObjPathResolver.getValue(attr.getPmParent());
    }
  }

  static class ValueAccessReflection implements BackingValueAccessStrategy {
    static final BackingValueAccessStrategy INSTANCE = new ValueAccessReflection();

    @Override
    public Object getValue(PmAttrBase<?, ?> attr) {
      @SuppressWarnings("unchecked")
      Object bean = ((PmBean<Object>)attr.getPmParentElement()).getPmBean();
      return bean != null
              ? attr.getOwnMetaData().beanAttrAccessor.<Object>getBeanAttrValue(bean)
              : null;
    }

    @Override
    public void setValue(PmAttrBase<?, ?> attr, Object value) {
      @SuppressWarnings("unchecked")
      Object bean = ((PmBean<Object>)attr.getPmParentElement()).getPmBean();
      if (bean == null) {
        throw new PmRuntimeException(attr, "Unable to access an attribute value for a backing pmBean that is 'null'.");
      }
      attr.getOwnMetaData().beanAttrAccessor.setBeanAttrValue(bean, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getPropertyContainingBean(PmAttrBase<?, ?> attr) {
      return ((PmBean<Object>)attr.getPmParentElement()).getPmBean();
    }
  }


  /**
   * A command that changes an attribute value.
   */
  @PmTitleCfg(resKey="pmValueChangeCommand")
  @PmCommandCfg(beforeDo=BEFORE_DO.DO_NOTHING)
  public static class ValueChangeCommandImpl<T_VALUE> extends PmCommandImpl implements ValueChangeCommand<T_VALUE> {

    private final T_VALUE oldValue;
    private final T_VALUE newValue;

    public ValueChangeCommandImpl(PmAttrBase<T_VALUE,?> changedPmAttr, T_VALUE oldValue, T_VALUE newValue) {
      super(changedPmAttr);
      this.oldValue = oldValue;
      this.newValue = newValue;
      setUndoCommand(new ValueChangeCommandImpl<T_VALUE>(this));
    }

    /**
     * Constructor for the corresponding undo command.
     *
     * @param doCommand The command to undo.
     */
    private ValueChangeCommandImpl(ValueChangeCommandImpl<T_VALUE> doCommand) {
      super(doCommand.getPmParent());
      this.newValue = doCommand.oldValue;
      this.oldValue = doCommand.newValue;
      setUndoCommand(doCommand);
    }

    @Override @SuppressWarnings("unchecked")
    protected void doItImpl() throws Exception {
      ((PmAttrBase<Object, ?>)getPmParent()).setValue(newValue);
    }

    /**
     * The referenced presentation model should be enabled.
     */
    @Override
    protected boolean isPmEnabledImpl() {
      return super.isPmEnabledImpl() && getPmParent().isPmEnabled();
    }

    protected NaviLink afterDo(boolean changeCommandHistory) {
      if (changeCommandHistory) {
        getPmConversationImpl().getPmCommandHistory().commandDone(this);
      }
//      PmEventApi.firePmEvent(this, PmEvent.EXEC_COMMAND);
      return null;
    }

    public T_VALUE getNewValue() {
      return newValue;
    }

    public T_VALUE getOldValue() {
      return oldValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PmAttr<T_VALUE> getPmAttr() {
      return (PmAttr<T_VALUE>) getPmParent();
    }
  }

}
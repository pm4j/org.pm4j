package org.pm4j.core.pm.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

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
import org.pm4j.common.cache.CacheStrategy;
import org.pm4j.common.cache.CacheStrategyNoCache;
import org.pm4j.common.converter.string.StringConverter;
import org.pm4j.common.converter.string.StringConverterParseException;
import org.pm4j.common.converter.value.ValueConverter;
import org.pm4j.common.converter.value.ValueConverterDefault;
import org.pm4j.common.expr.Expression.SyntaxVersion;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.common.util.GenericsUtil;
import org.pm4j.common.util.collection.MapUtil;
import org.pm4j.common.util.reflection.BeanAttrAccessor;
import org.pm4j.common.util.reflection.BeanAttrAccessorImpl;
import org.pm4j.common.util.reflection.ClassUtil;
import org.pm4j.common.util.reflection.ReflectionException;
import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.exception.PmResourceData;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmAttrCfg.Restriction;
import org.pm4j.core.pm.annotation.PmAttrCfg.Validate;
import org.pm4j.core.pm.annotation.PmCacheCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg.CacheMode;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.impl.cache.CacheStrategyBase;
import org.pm4j.core.pm.impl.cache.CacheStrategyRequest;
import org.pm4j.core.pm.impl.converter.PmConverterErrorMessage;
import org.pm4j.core.pm.impl.converter.PmConverterOptionBased;
import org.pm4j.core.pm.impl.options.GenericOptionSetDef;
import org.pm4j.core.pm.impl.options.OptionSetDefNoOption;
import org.pm4j.core.pm.impl.options.PmOptionSetDef;
import org.pm4j.core.pm.impl.pathresolver.PassThroughPathResolver;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;
import org.pm4j.core.pm.impl.pathresolver.PmExpressionPathResolver;
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
  private boolean valueChangedBySetValue = false;

  /**
   * Contains optional attribute data that in most cases doesn't exist for usual
   * bean attributes.
   */
  private PmAttrDataContainer<T_PM_VALUE, T_BEAN_VALUE> dataContainer;

  /**
   * Keeps a reference to the entered value in case of buffered data entry.
   */
  private Object bufferedValue = UNKNOWN_VALUE_INDICATOR;

  /** A cache member. Is only used in case for {@link ValueAccessReflection}. */
  private PmBean<Object> parentPmBean;

  /** The decorators to execute before and after setting the attribute value. */
  private Collection<PmCommandDecorator> valueChangeDecorators = Collections.emptyList();

  /** Converts between external and backing values. */
  private ValueConverter<T_PM_VALUE, T_BEAN_VALUE> valueConverter;

  /** Converts between external value type and its string representation. */
  private StringConverter<T_PM_VALUE> stringConverter;

  /** A lightweight helper that provides converter operation context information. */
  private AttrConverterCtxt converterCtxt = makeConverterCtxt();

  /**
   * @param pmParent The PM hierarchy parent.
   */
  public PmAttrBase(PmObject pmParent) {
    super(pmParent);
  }

  @Override
  public final PmOptionSet getOptionSet() {
    MetaData md = getOwnMetaData();
    Object ov = md.cacheStrategyForOptions.getCachedValue(this);

    if (ov != CacheStrategy.NO_CACHE_VALUE) {
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
           getPmParent().isPmReadonly();
  }

  @Override
  public final boolean isPmEnabled() {
    // link enable status to read only even if the default impl of isPmEnabledImpl is overwritten
    // The read-only check is done first because some domain implementations of isPmEnabledImpl()
    // are implemented in a way that fails under some read-only conditions (e.g. in case of a missing backing bean).
    return !isPmReadonly() && super.isPmEnabled();
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
      for (PmMessage m : PmMessageApi.getMessages(this, Severity.ERROR)) {
        this.getPmConversationImpl()._clearPmMessage(m);
      }
      PmEventApi.firePmEvent(this, getOwnMetaData().validationChangeEventMask);
    }
  }

  /**
   * The default implementation compares the results of {@link #getValueLocalized()}
   * according to the collation sequence of the current {@link Locale}.
   *
   * @deprecated PM based compare operations are no longer supported. Please compare the related data objects.
   */
  @Override
  @Deprecated
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

    if (ov != CacheStrategy.NO_CACHE_VALUE) {
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
          setAndPropagateValueConverterMessage(vc, e, resData.msgKey, resData.msgArgs);
          if (LOG.isDebugEnabled()) {
            LOG.debug("String to value conversion failed in attribute '" + PmUtil.getPmLogString(this) + "'. String value: " + text);
          }
        }
        return;
      } catch (PmConverterException e) {
        PmResourceData resData = e.getResourceData();
        Object[] args = Arrays.copyOf(resData.msgArgs, resData.msgArgs.length+1);
        args[resData.msgArgs.length] = getPmTitle();
        setAndPropagateValueConverterMessage(vc, e, resData.msgKey, args);
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
      PmCacheApi.clearPmCache(this);
      this.valueChangedBySetValue = false;
    }
    clearPmInvalidValues();
    if (isWritable) {
      T_PM_VALUE dv = getDefaultValue();
      // TODO olaf: handle scalar values!
//      if (dv == null && getOwnMetaData().primitiveType) {
//        getOwnMetaData().
//      }
      setValue(dv);
    }

    // For composite attribute PM's: reset the children.
    List<PmDataInput> children = PmUtil.getPmChildrenOfType(this, PmDataInput.class);
    for (PmDataInput child : children) {
      child.resetPmValues();
    }
  }

  @Override
  protected void clearCachedPmValues(Set<PmCacheApi.CacheKind> cacheSet) {
    if (pmInitState != PmInitState.INITIALIZED)
      return;

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

  /**
   *
   * @return
   */
  // TODO oboede: is there a use case that is not covered by overriding getBackingValueImpl()?
  // Can we make this method final and private?
  protected T_PM_VALUE getValueImpl() {
    try {
      // the method will try to populate pmValue with different approaches
      // and return it as result
      T_PM_VALUE pmValue = null;

      T_BEAN_VALUE beanAttrValue = getBackingValue();

      // return the converted beanAttrValue if it has #isEmptyValue() set to false.
      if (beanAttrValue != null) {
        pmValue = convertBackingValueToPmValue(beanAttrValue);
        if(!isEmptyValue(pmValue)) {
          return pmValue;
        }
      }

      // Default values may have only effect if the value was not set by the user:
      if (valueChangedBySetValue) {
        return pmValue;
      }

      // At this point pmValue is still either null or empty.
      // If a default value exists this shall be used to populate it.

      // The default value will be show in read-only scenarios instead of
      // the real 'null' value.
      // TODO oboede: this behavior needs to be checked/well documented!
      T_PM_VALUE defaultValue = getDefaultValue();
      if (defaultValue != null) {
        // The backing value gets changed within the 'get' functionality.
        // This is ok according to the default value logic. See: Wiki entry TODO
        //
        // This modification can't be done for disabled attributes, since this
        // operation may fail in some cases. (E.g. if the backing bean is 'null'.)
        //
        // FIXME oboede: It was not possible to ask 'isPmEnabled' because some isPmEnabledImpl
        // code asked 'getValue()', which generates a stack overflow.
        // We need a clear documented way to fix this issue.
        if (!isPmReadonly()) {
          T_BEAN_VALUE defaultBeanAttrValue = convertPmValueToBackingValue(defaultValue);
          setBackingValue(defaultBeanAttrValue);
        }
        return defaultValue;
      }

      // If non of the above approaches was successful we can do nothing else
      // then return the pmValue that is either null or an empty list.
      return pmValue;

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
  // TODO oboede: make it final or package private? Which functionality is not covered by setBackingValueImpl()?
  protected boolean setValueImpl(SetValueContainer<T_PM_VALUE> value) {
    PmEventApi.ensureThreadEventSource(this);
    MetaData metaData = getOwnMetaData();

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
      if ((newPmValue == null) && metaData.primitiveType) {
        setAndPropagateInvalidValue(value, PmConstants.MSGKEY_VALIDATION_MISSING_REQUIRED_VALUE);
        return false;
      }

      // FIXME olaf: read only control should be done within the calling setValueAsString method!
      //             The set operation should not be performed in this case. Check for side effects...
      if (pmValueChanged && isPmReadonly()) {
        PmMessageApi.addMessage(this, Severity.ERROR, PmConstants.MSGKEY_VALIDATION_READONLY);
        return false;
      }

      if (metaData.validate == Validate.BEFORE_SET || isValidatingOnSetPmValue()) {
        // Validate even if nothing was changed. The currentValue may be invalid too.
        // Example: New object with empty attributes values.
        try {
          validate(newPmValue);
        }
        // TODO: we have to handle other validation exceptions too.
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
        metaData.cacheStrategyForValue.setAndReturnCachedValue(this, newPmValue);

        // From now on the value should be handled as intentionally modified.
        // That means that the default value shouldn't be returned, even if the
        // value was set to <code>null</code>.
        valueChangedBySetValue = true;

        setValueChanged(currentValue, newPmValue);

        // optional after-set validation done before afterChange calls. See: Validate.AFTER_SET.
        // TODO olaf: Check domain exception handling here. After an exception the value is
        // changed but no event will get fired.
        if (metaData.validate == Validate.AFTER_SET) {
          pmValidate();
        }

        // internal after-change logic before external after-change logic.
        afterValueChange(currentValue, newPmValue);

        // tighter coupled value change decorators before more unspecific event listeners
        for (PmCommandDecorator d : getValueChangeDecorators()) {
          d.afterDo(cmd);
        }

        PmEventApi.firePmEvent(this, PmEvent.VALUE_CHANGE);

        // after all post-processing it's really done and available for undo calls.
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
   * A method that may be overridden to add domain specific after value change logic.
   * <p>
   * It's called
   * <ul>
   *  <li><b>after</b> changing the attribute value,</li>
   *  <li><b>before</b> the after-do method calls to registered value change decorators and
   *  <li><b>before</b> sending the value change event to registered listeners.</li>
   * </ul>
   *
   * @param oldValue The old attribute value.
   * @param newValue The new (current) value.
   *
   * @see #addValueChangeDecorator(PmCommandDecorator)
   * @see PmEventApi#addPmEventListener(PmObject, int, org.pm4j.core.pm.PmEventListener)
   */
  protected void afterValueChange(T_PM_VALUE oldValue, T_PM_VALUE newValue) {
  }

  /**
   * Gets called whenever a string value needs to be converted to the attribute value type.
   * <p>
   * The default implementation uses the converter provided by {@link #getStringConverter()}.
   *
   * @param s The string to convert.
   * @return The converted value.
   * @throws PmConverterException If the given string can't be converted.
   *
   * @deprecated Please use {@link #getStringConverterImpl()} to define your specific converter.
   */
  @Deprecated
  protected T_PM_VALUE stringToValueImpl(String s) throws PmConverterException {
    try {
      return (T_PM_VALUE) getStringConverter().stringToValue(converterCtxt, s);
    } catch (StringConverterParseException e) {
      throw new PmConverterException(this, e);
    }
  }

  /**
   * Indicates if the value was explicitly set. This information is especially
   * important for the default value logic. Default values may have only effect
   * on values that are not explicitly set.
   */
  protected final boolean isValueChangedBySetValue() {
    return valueChangedBySetValue;
  }

  /**
   * Gets called whenever the attribute value needs to be represented as a string.
   * <p>
   * The default implementation uses the converter provided by {@link #getStringConverter()}.
   *
   * @param v A value to convert.
   * @return The string representation.
   *
   * @deprecated Please use {@link #getStringConverterImpl()} to define your specific converter.
   */
  @Deprecated
  protected String valueToStringImpl(T_PM_VALUE v) {
    return getStringConverter().valueToString(converterCtxt, v);
  }

  /**
   * @return The converter that translates from and to the corresponding string value representation.
   */
  public final StringConverter<T_PM_VALUE> getStringConverter() {
    if (stringConverter == null) {
      zz_ensurePmInitialization();
      stringConverter = getStringConverterImpl();
      if (stringConverter == null) {
        throw new PmRuntimeException(this, "Please ensure that getStringConverterImpl() does not return null.");
      }
    }
    return stringConverter;
  }

  /**
   * A factory method that defines the value string converter to use.
   * <p>
   * Corresponding external interface: {@link #getStringConverter()}
   *
   * @return The attribute value string converter. Never <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  protected StringConverter<T_PM_VALUE> getStringConverterImpl() {
    StringConverter<T_PM_VALUE> c = (StringConverter<T_PM_VALUE>) getOwnMetaData().stringConverter;
    if (c == null) {
      throw new PmRuntimeException(this, "Missing value converter.");
    }
    return c;
  }

  /** @return The converter operation context. */
  protected AttrConverterCtxt getConverterCtxt() {
    return converterCtxt;
  }

  /** A factory method that provides the attribute type specific converter context reference. */
  protected AttrConverterCtxt makeConverterCtxt() {
    return new AttrConverterCtxt(this);
  }

  @Override
  protected boolean isPmValueChangedImpl() {
    return  (dataContainer != null &&
             dataContainer.originalValue != UNCHANGED_VALUE_INDICATOR) ||
             super.isPmValueChangedImpl();
  }

  /**
   * Sub classes may override this to define their attribute specific time zone.
   *
   * @return The externally visible time zone.
   */
  protected TimeZone getPmTimeZoneImpl() {
      return getPmConversation().getPmTimeZone();
  }

  @Override
  protected void setPmValueChangedImpl(boolean newChangedState) {
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
  // XXX olaf kossak: shouldn't that method return T_BEAN_VALUE ?
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
    getPmConversationImpl().addPmMessage(new PmValidationMessage(this, invValue, null, msgKey, msgArgs));
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
  private void setAndPropagateValueConverterMessage(SetValueContainer<T_PM_VALUE> invValue, Throwable cause, String msgKey,
      Object... msgArgs) {
    this.getPmConversationImpl().addPmMessage(new PmConverterErrorMessage(this, invValue, cause, msgKey, msgArgs));
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
   *
   * @deprecated Please use {@link PmAttrCfg#validate()} using the parameter {@link PmAttrCfg.Validate#BEFORE_SET}.
   */
  @Deprecated
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
      throw new PmValidationException(PmMessageApi.addRequiredMessage(this));
    }
  }

  /**
   * Check the JSR-303 bean validation constraints for this attribute.
   * <p>
   * All found violations are reported as error messages in relation to this attribute.
   */
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
        isPmEnabled()) {
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
        catch (RuntimeException e) {
          conversation.getPmExceptionHandler().onExceptionInPmValidation(this, e);
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
  PmDataInput parentPmCtxt = PmUtil.getPmParentOfType(this, PmDataInput.class);
    return parentPmCtxt.isBufferedPmValueMode();
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

  // ======== Backing value access ======== //

  /**
   * Converts the backing attribute value to the external attribute value.
   * <p>
   * The default implementation uses the {@link ValueConverter} provided by {@link #getValueConverter()}.
   */
  public T_PM_VALUE convertBackingValueToPmValue(T_BEAN_VALUE backingValue) {
    return getValueConverter().toExternalValue(converterCtxt, backingValue);
  }

  /**
   * Converts the external attribute value to the backing attribute value.
   * <p>
   * The default implementation uses the {@link ValueConverter} provided by {@link #getValueConverter()}.
   */
  public T_BEAN_VALUE convertPmValueToBackingValue(T_PM_VALUE externalValue) {
    return getValueConverter().toInternalValue(converterCtxt, externalValue);
  }

  /** Provides the value converter that translates between backing- and external value. */
  final ValueConverter<T_PM_VALUE, T_BEAN_VALUE> getValueConverter() {
    if (valueConverter == null) {
      zz_ensurePmInitialization();
      valueConverter = getValueConverterImpl();
      if (valueConverter == null) {
        throw new PmRuntimeException(this, "Please ensure that getValueConverterImpl() does not return null.");
      }
    }
    return (ValueConverter<T_PM_VALUE, T_BEAN_VALUE>) valueConverter;
  }

  /**
   * Provides the value converter used to convert between backing value to external value type.<br>
   * The default implementation uses the information provided in {@link PmAttrCfg#valueConverter()}.
   * If nothing is configured there a simple pass-through converter will be provided.
   * <p>
   * Advanced usage:<br>
   * To provide a shared stateless {@link ValueConverter}, you may call
   * {@link MetaData#setValueConverter(org.pm4j.core.pm.PmAttr.ValueConverter)} within {@link #initMetaData(org.pm4j.core.pm.impl.PmObjectBase.MetaData)}.
   *
   * @return The converter to use. Should not be <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  protected ValueConverter<T_PM_VALUE, T_BEAN_VALUE> getValueConverterImpl() {
    MetaData md = getOwnMetaData();
    return (ValueConverter<T_PM_VALUE, T_BEAN_VALUE>)(
        (md.valueConverter != null)
          ? md.valueConverter
          : ValueConverterDefault.INSTANCE);
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

  @Override
  public Class<?> getValueType() {
    Type t = GenericsUtil.findFirstSuperClassParameterType(getClass());
    if (!(t instanceof Class)) {
      throw new PmRuntimeException(this, "Unable to handle an attribute value type that is not a class or interface. Found type: " + t);
    }
    return (Class<?>) t;
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

  /** INTERNAL method. */
  protected void addValueChangeDecorator(PmCommandDecorator decorator) {
    if (valueChangeDecorators.isEmpty()) {
      valueChangeDecorators = new ArrayList<PmCommandDecorator>();
    }
    valueChangeDecorators.add(decorator);
  }

  /**
   * @return The set of decorators to consider on value change.
   */
  protected Collection<PmCommandDecorator> getValueChangeDecorators() {
    return valueChangeDecorators;
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
              ? new GenericOptionSetDef(this, cfg, getOptionValuesMethod)
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
      myMetaData.setStringConverter(
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
      myMetaData.validate = fieldAnnotation.validate();

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
            SyntaxVersion syntaxVersion = PmExpressionApi.getSyntaxVersion(this);
            myMetaData.valuePathResolver = PmExpressionPathResolver.parse(fieldAnnotation.valuePath(), syntaxVersion);
            int lastDotPos = fieldAnnotation.valuePath().lastIndexOf('.');
            if (lastDotPos > 0) {
              String parentPath = fieldAnnotation.valuePath().substring(0, lastDotPos);
              myMetaData.valueContainingObjPathResolver = PmExpressionPathResolver.parse(parentPath, syntaxVersion);
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

      if (fieldAnnotation.valueConverter() != ValueConverter.class) {
        myMetaData.valueConverter = ClassUtil.newInstance(fieldAnnotation.valueConverter());
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
        if (ClassUtil.findMethods(getClass(), "getBackingValueImpl").size() > 1) {
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
    Collection<PmCacheCfg> cacheAnnotations = AnnotationUtil.findAnnotationsInPmHierarchy(this, PmCacheCfg.class, new ArrayList<PmCacheCfg>());
    myMetaData.cacheStrategyForOptions = AnnotationUtil.evaluateCacheStrategy(this, PmCacheCfg.ATTR_OPTIONS, cacheAnnotations, CACHE_STRATEGIES_FOR_OPTIONS);
    myMetaData.cacheStrategyForValue = AnnotationUtil.evaluateCacheStrategy(this, PmCacheCfg.ATTR_VALUE, cacheAnnotations, CACHE_STRATEGIES_FOR_VALUE);
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
    private CacheStrategy                 cacheStrategyForOptions = CacheStrategyNoCache.INSTANCE;
    private CacheStrategy                 cacheStrategyForValue   = CacheStrategyNoCache.INSTANCE;
    private StringConverter<?>              stringConverter;
    private ValueConverter<?, ?>            valueConverter;
    private BackingValueAccessStrategy      valueAccessStrategy     = ValueAccessLocal.INSTANCE;
    /** Name of the field configured for JSR 303-validation.<br>
     * Is <code>null</code> if there is nothing to validate this way. */
    private String                          validationFieldName;
    private PmAttrCfg.Validate              validate                = PmAttrCfg.Validate.ON_VALIDATE_CALL;
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

    public CacheStrategy getCacheStrategyForOptions() { return cacheStrategyForOptions; }
    public CacheStrategy getCacheStrategyForValue() { return cacheStrategyForValue; }

    public StringConverter<?> getStringConverter() { return stringConverter; }

    /**
     * Defines a <b>state less</b> string converter. Converters that need to have PM state information
     * should be provided by overriding {@link PmAttrBase#getStringConverterImpl()}.
     * @param converter The state less string converter.
     */
    public void setStringConverter(StringConverter<?> converter) { this.stringConverter = converter; }

    /**
     * Defines a <b>state less</b> value converter. Converters that need to have PM state information
     * should be provided by overriding {@link PmAttrBase#getValueConverterImpl()}.
     * @param stringConverter The state less value converter.
     */
    public void setValueConverter(ValueConverter<?, ?> vc) { this.valueConverter = vc; }

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

  private static final CacheStrategy CACHE_VALUE_LOCAL = new CacheStrategyBase<PmAttrBase<?,?>>("CACHE_VALUE_LOCAL") {
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

  private static final CacheStrategy CACHE_OPTIONS_LOCAL = new CacheStrategyBase<PmAttrBase<?,?>>("CACHE_OPTIONS_LOCAL") {
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


  private static final Map<CacheMode, CacheStrategy> CACHE_STRATEGIES_FOR_VALUE =
    MapUtil.makeFixHashMap(
      CacheMode.OFF,      CacheStrategyNoCache.INSTANCE,
      CacheMode.ON,    CACHE_VALUE_LOCAL,
      CacheMode.REQUEST,  new CacheStrategyRequest("CACHE_VALUE_IN_REQUEST", "v")
    );

  private static final Map<CacheMode, CacheStrategy> CACHE_STRATEGIES_FOR_OPTIONS =
    MapUtil.makeFixHashMap(
        CacheMode.OFF,      CacheStrategyNoCache.INSTANCE,
        CacheMode.ON,    CACHE_OPTIONS_LOCAL,
        CacheMode.REQUEST,  new CacheStrategyRequest("CACHE_OPTIONS_IN_REQUEST", "os")
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
      Object bean = getParentPmBean(attr).getPmBean();
      return bean != null
              ? attr.getOwnMetaData().beanAttrAccessor.<Object>getBeanAttrValue(bean)
              : null;
    }

    @Override
    public void setValue(PmAttrBase<?, ?> attr, Object value) {
      Object bean = getParentPmBean(attr).getPmBean();
      if (bean == null) {
        throw new PmRuntimeException(attr, "Unable to access an attribute value for a backing pmBean that is 'null'.");
      }
      attr.getOwnMetaData().beanAttrAccessor.setBeanAttrValue(bean, value);
    }

    @Override
    public Object getPropertyContainingBean(PmAttrBase<?, ?> attr) {
      return getParentPmBean(attr).getPmBean();
    }

    @SuppressWarnings("unchecked")
    private PmBean<Object> getParentPmBean(PmAttrBase<?, ?> attr) {
      if (attr.parentPmBean == null) {
        attr.parentPmBean = PmUtil.getPmParentOfType(attr, PmBean.class);
      }
      return attr.parentPmBean;
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

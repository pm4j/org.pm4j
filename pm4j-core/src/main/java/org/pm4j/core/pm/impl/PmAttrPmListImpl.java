package org.pm4j.core.pm.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.converter.string.StringConverter;
import org.pm4j.common.converter.string.StringConverterList;
import org.pm4j.common.converter.value.ValueConverter;
import org.pm4j.common.converter.value.ValueConverterCtxt;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrPmListCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.api.PmValidationApi;
import org.pm4j.core.pm.impl.commands.PmListAddItemCommand;
import org.pm4j.core.pm.impl.commands.PmListRemoveItemCommand;
import org.pm4j.core.pm.impl.converter.PmConverterOptionBased;
import org.pm4j.core.pm.impl.pathresolver.ExpressionPathResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implements an attribute that represents a list of beans as a list of PM's.
 *
 * @author Olaf Boede
 *
 * @param <T_BEAN> type of backing list items.
 * @param <T_ITEM_PM> type PM used to present a bean item.
 */
public class PmAttrPmListImpl<T_ITEM_PM extends PmBean<T_BEAN>, T_BEAN> extends PmAttrBase<List<T_ITEM_PM>, Collection<T_BEAN>> implements PmAttrPmList<T_ITEM_PM> {

  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(PmAttrPmListImpl.class);

  public PmAttrPmListImpl(PmObject pmParent) {
    super(pmParent);
  }

  @Override
  public void add(T_ITEM_PM pmElement) {
    PmListAddItemCommand cmd = new PmListAddItemCommand(this, pmElement);
    PmEventApi.ensureThreadEventSource(cmd);
    Collection<T_BEAN> beanValue = getBackingValue();
    if (beanValue == null) {
      beanValue = makeBeanCollection();
      setBackingValueInternal(beanValue);
    }
    beanValue.add(pmElement.getPmBean());
    PmEventApi.firePmEventIfInitialized(this, PmEvent.VALUE_CHANGE);
    getPmConversation().getPmCommandHistory().commandDone(cmd);
  }

  @Override
  public boolean remove(T_ITEM_PM pmElement) {
    PmListRemoveItemCommand cmd = new PmListRemoveItemCommand(this, pmElement);
    PmEventApi.ensureThreadEventSource(cmd);

    Collection<T_BEAN> beanValue = getBackingValue();
    if (beanValue != null) {
      boolean didChange = beanValue.remove(pmElement.getPmBean());
      if (didChange) {
        getPmConversation().getPmCommandHistory().commandDone(cmd);
        PmEventApi.firePmEventIfInitialized(this, PmEvent.VALUE_CHANGE);
        return true;
      }
    }

    // nothing was changed if this code gets executed.
    return false;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Checks in addition if the unimplemented default value logic is not used.
   */
  @Override
  protected List<T_ITEM_PM> getValueImpl() {
    if (getDefaultValue() != null) {
      throw new PmRuntimeException(this, "Default values are currently not supported for PmAttrPmList.\n" +
    "Please consider overriding getValueImpl() or getBackingValueImpl() to adjust the default using a setBackingValue call.");
    }
    return super.getValueImpl();
  }

  @Override
  public final List<T_ITEM_PM> getValueAsList() {
    return getValue();
  }

  @Override
  public final void setValueAsList(List<T_ITEM_PM> value) {
    setValue(value);
  }

  @Override
  public List<T_ITEM_PM> getValueSubset(int fromIdx, int numItems) {
    List<T_ITEM_PM> all = getValue();
    int toRow = (numItems == -1)
        ? all.size()
        : Math.min(fromIdx+numItems, all.size());

    List<T_ITEM_PM> subList = all.subList(fromIdx, toRow);
    return subList;
  }

  @Override
  public T_ITEM_PM getFirstItem() {
    List<T_ITEM_PM> all = getValue();
    return all != null && all.size() > 0
              ? all.get(0)
              : null;
  }

  @Override
  public T_ITEM_PM getLastItem() {
    List<T_ITEM_PM> all = getValue();
    return all != null && all.size() > 0
              ? all.get(all.size()-1)
              : null;
  }

  @Override
  public int getSize() {
    List<T_ITEM_PM> v = getValue();
    return (v != null) ? v.size() : 0;
  }

  @Override
  public boolean getHasVisibleItems() {
    for (T_ITEM_PM i : getValue()) {
      if (i.isPmVisible()) {
        return true;
      }
    }

    // no item or no visible item:
    return false;
  }

  @Override
  protected boolean isEmptyValue(List<T_ITEM_PM> value) {
    return (value == null) || value.isEmpty();
  }

  @Override
  public NullOption getNullOptionDefault() {
    return NullOption.NO;
  }


  // ======== Tree node implementation ======== //

  @SuppressWarnings("unchecked")
  @Override
  protected List<PmObject> getPmChildNodesImpl() {
    return (List<PmObject>)(Object) getValue();
  }

  /**
   * The default implementation returns <code>false</code> because the children
   * of a list attribute are usually displayed as tree child nodes.
   */
  @Override
  public boolean isPmTreeLeaf() {
    return false;
  }

  // ======== Value handling ======== //

  /**
   * The collection to put the values to may be very different.
   * Some beans expect a Set, others a List.
   * <p>
   * The default implementation provides an {@link ArrayList}.
   *
   * @return A new empty collection.
   */
  protected Collection<T_BEAN> makeBeanCollection() {
    return new ArrayList<T_BEAN>();
  }

  /**
   * In addition to standard checks (required check) it forwards the validation
   * to the list items provided by the parameter.
   */
  @Override
  public void pmValidate() {
    super.pmValidate();

    List<T_ITEM_PM> value = getValue();
    if (value != null) {
      for (T_ITEM_PM pm : value) {
        PmValidationApi.validate(pm);
      }
    }
  }

  @Override
  protected ValueConverter<List<T_ITEM_PM>, Collection<T_BEAN>> getValueConverterImpl() {
    return new PmListValueConverter();
  }

  /** Translates all items. */
  protected class PmListValueConverter implements ValueConverter<List<T_ITEM_PM>, Collection<T_BEAN>> {

    @Override
    public List<T_ITEM_PM> toExternalValue(ValueConverterCtxt ctxt, Collection<T_BEAN> beanList) {
      if (beanList == null) {
        return null;
      }
      MetaData md = getOwnMetaData();
      @SuppressWarnings("unchecked")
      List<T_ITEM_PM> pmValues = (List<T_ITEM_PM>) PmFactoryApi
            .getPmListForBeans(PmAttrPmListImpl.this,
                               beanList,
                               !md.provideInvisibleItems);
      return pmValues;
    }

    @Override
    public Collection<T_BEAN> toInternalValue(ValueConverterCtxt ctxt, List<T_ITEM_PM> pmAttrValue) {
      if (pmAttrValue == null) {
        return null;
      }
      int listSize = pmAttrValue.size();
      Collection<T_BEAN> objectList = makeBeanCollection();

      for (int i = 0; i < listSize; ++i) {
        T_ITEM_PM itemPm = pmAttrValue.get(i);
        objectList.add(itemPm.getPmBean());
      }

      return objectList;
    }

  }

  // ======== meta data ======== //

  @Override
  protected PmAttrBase.MetaData makeMetaData() {
    return new MetaData();
  }


  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;
    myMetaData.setValidateLengths(false);

    PmAttrPmListCfg annotation = AnnotationUtil.findAnnotation(this, PmAttrPmListCfg.class);
    if (annotation != null) {
      myMetaData.provideInvisibleItems = annotation.provideInvisibleItems();
    }

    StringConverter<Object> itemConverter = new PmConverterOptionBased(ExpressionPathResolver.parse("pmKey"));
    myMetaData.setStringConverter(new StringConverterList<Object>(itemConverter));
  }

  protected static class MetaData extends PmAttrBase.MetaData {
    private boolean provideInvisibleItems = false;

    public MetaData() {
      super(Integer.MAX_VALUE); // maximum valueAsString characters.
    }
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }

}

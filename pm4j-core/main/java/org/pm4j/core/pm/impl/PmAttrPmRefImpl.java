package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmAttrPmRef;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.converter.PmConverterOptionBased;

/**
 * TODOC:
 * <p>
 * Keeps a reference to the bean differently for different scenarios:
 * <ul>
 *   <li>A reference to the bean when...</li>
 *   <li>The id of the referenced bean when...</li>
 *   <li>A PM of the referenced bean when...</li>
 * </ul>
 *
 * @author olaf boede
 *
 * @param <T_REFED_PM>
 */
public class PmAttrPmRefImpl<T_REFED_PM extends PmBean<?>, T_BEAN>
  // bean value type is only Object, because the reference may keep an identifier or bean value.
  extends PmAttrBase<T_REFED_PM, T_BEAN>
  implements PmAttrPmRef<T_REFED_PM> {

  public PmAttrPmRefImpl(PmObject pmParent) {
    super(pmParent);
  }

  // ======== Public interface implementation ======== //

  @Override
  public Object getValueAsBean() {
    T_REFED_PM refedPm = getValue();
    return refedPm != null
              ? refedPm.getPmBean()
              : null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T_REFED_PM setValueAsBean(Object bean) {
    T_REFED_PM refedPm = (T_REFED_PM) PmFactoryApi.getPmForBean(this, bean);
    setValue(refedPm);
    return refedPm;
  }

  /**
   * In addition to standard checks (required check) it forwards the validation
   * to the bean-PM provided by the parameter.
   */
  @Override
  public void pmValidate() {
    super.pmValidate();

    T_REFED_PM value = getValue();
    if (value != null) {
      value.pmValidate();
    }
  }

  // ======== Value handling ======== //

  @Override
  @SuppressWarnings("unchecked")
  public T_REFED_PM convertBackingValueToPmValue(T_BEAN beanAttrValue) {
    return (T_REFED_PM) PmFactoryApi.getPmForBean(this, beanAttrValue);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T_BEAN convertPmValueToBackingValue(T_REFED_PM pmAttrValue) {
    return (T_BEAN) pmAttrValue.getPmBean();
  }

  /**
   * Compares the referenced PM's by calling their {@link #compareTo(PmObject)}
   * method.
   */
  @SuppressWarnings("unchecked")
  @Override
  public int compareTo(PmObject otherPm) {
    if (PmUtil.getAbsoluteName(this).equals(PmUtil.getAbsoluteName(otherPm))) {
      T_REFED_PM lhs = getValue();
      T_REFED_PM rhs = ((PmAttrPmRef<T_REFED_PM>)otherPm).getValue();
      if (lhs != null) {
        return (rhs != null)
              ? lhs.compareTo(rhs)
              : 1;
      }
      else {
        return (rhs == null)
              ? 0
              : -1;
      }
    }
    else {
      return super.compareTo(otherPm);
    }
  }

  // ======== meta data ======== //

  /**
   * Gets called when the meta data instance for this presentation model
   * is not yet available (first call within the VM live time).
   * <p>
   * Subclasses that provide more specific meta data should override this method
   * to provide their meta data information container.
   *
   * @param attrName The name of the attribute. Unique within the parent element scope.
   * @return A meta data container for this presentation model.
   */
  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;

    // XXX olaf: assumes a that the default identifier of domain objects
    //           is named 'id'.
    myMetaData.setConverterDefault(new PmConverterOptionBased("id"));
  }

  protected static class MetaData extends PmAttrBase.MetaData {
    @Override
    protected int getMaxLenDefault() {
      // XXX olaf: check for a real restriction...
      return 100;
    }
  }

}

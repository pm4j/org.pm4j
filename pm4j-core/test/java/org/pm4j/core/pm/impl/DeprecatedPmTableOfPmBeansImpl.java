package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmFactoryApi;

@Deprecated
public abstract class DeprecatedPmTableOfPmBeansImpl<T_BEAN_PM extends PmBean<T_BEAN>, T_BEAN> extends DeprecatedPmTableOfPmElementsImpl<T_BEAN_PM> {

  public DeprecatedPmTableOfPmBeansImpl(PmObject pmParent) {
    super(pmParent);
  }

  @Override
  protected Collection<T_BEAN_PM> getRowElementsImpl() {
    Collection<T_BEAN> beans = getBackingValue();
    return convertBackingValueToPmValue(beans);
  }

  protected abstract Collection<T_BEAN> getBackingValue();

  protected abstract void setBackingValue(Collection<T_BEAN> beans);

  @SuppressWarnings("unchecked")
  public List<T_BEAN_PM> convertBackingValueToPmValue(Object beanList) {
    List<T_BEAN_PM> pmValues = (List<T_BEAN_PM>) PmFactoryApi.getPmListForBeans(this, (Collection<T_BEAN>)beanList, false);
    return pmValues;
  }

  @SuppressWarnings("unchecked")
  public Collection<T_BEAN> convertPmValueToBackingValue(Object rawPmAttrValue) {
    List<T_BEAN_PM> pmAttrValue = (List<T_BEAN_PM>) rawPmAttrValue;
    int listSize = pmAttrValue.size();
    Collection<T_BEAN> objectList = makeBeanCollection();

    for (int i = 0; i < listSize; ++i) {
      T_BEAN_PM itemPm = pmAttrValue.get(i);
      objectList.add(itemPm.getPmBean());
    }

    return objectList;
  }

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


  public static class WithCollection<T_BEAN_PM extends PmBean<T_BEAN>, T_BEAN> extends DeprecatedPmTableOfPmBeansImpl<PmBean<T_BEAN>, T_BEAN> {

    private Collection<T_BEAN> beans = new ArrayList<T_BEAN>();

    public WithCollection(PmObject pmParent) {
      super(pmParent);
    }

    public Collection<T_BEAN> getBeans() {
      return beans;
    }

    public void setBeans(Collection<T_BEAN> beans) {
      this.rows = null;
      this.beans = beans;
    }

    @Override
    protected Collection<T_BEAN> getBackingValue() {
      return beans;
    }

    @Override
    protected void setBackingValue(Collection<T_BEAN> beans) {
      this.beans = beans;
    }

  }

}

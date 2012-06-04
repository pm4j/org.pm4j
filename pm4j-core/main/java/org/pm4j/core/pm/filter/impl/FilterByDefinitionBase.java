package org.pm4j.core.pm.filter.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.filter.CompOp;
import org.pm4j.core.pm.filter.FilterByDefinition;
import org.pm4j.core.util.reflection.ClassUtil;

public abstract class FilterByDefinitionBase<T_ITEM, T_FILTER_VALUE> implements FilterByDefinition {

  private String name;
  private String title;
  private List<CompOp> compOps;
  private Constructor<?> valueAttrConstructor;

  public FilterByDefinitionBase(String name, String title) {
    this.name = name;
    this.title = title;
  }

  public FilterByDefinitionBase(PmObject pmCtxt, Class<?>... compOpClasses) {
    this(pmCtxt.getPmName(), pmCtxt.getPmTitle());
    compOps = new ArrayList<CompOp>(compOpClasses.length);
    for (Class<?> c : compOpClasses) {
      Constructor<?> constructor = ClassUtil.findConstructor(c, PmObject.class);
      if (constructor == null) {
        throw new PmRuntimeException(pmCtxt, "CompOp class needs a constructor with a single PmObject parameter to be useable in this context: " + c);
      }
      CompOp compOp = ClassUtil.newInstance(constructor, pmCtxt);
      compOps.add(compOp);
    }
  }


  /**
   * The type save match implementation version to be implemented.
   *
   * @param item
   * @param compOp
   * @param T_FILTER_VALUE
   * @return
   */
  protected abstract boolean doesItemMatchImpl(T_ITEM item, CompOp compOp, Object T_FILTER_VALUE);

  @SuppressWarnings("unchecked")
  @Override
  public boolean doesItemMatch(Object item, CompOp compOp, Object filterValue) {
    // if there is no comp, the filter usually does not have any effect.
    if (compOp == null) {
      return true;
    }

    return doesItemMatchImpl((T_ITEM)item, compOp, (T_FILTER_VALUE)filterValue);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<CompOp> getCompOps() {
    return (compOps != null)
        ? compOps
        : Collections.EMPTY_LIST;

  }

  /**
   * The default implementation creates a simple string attribute.
   */
  public PmAttr<?> makeValueAttrPm(PmObject parentPm) {
    return ClassUtil.newInstance(valueAttrConstructor, parentPm);
  }

  public void setValueAttrPmClass(Class<?> valueAttrPmClass) {
    this.valueAttrConstructor = ClassUtil.getConstructor(valueAttrPmClass, PmObject.class);
  }

  /**
   * The default implementation checks if the compare operator is not
   * <code>null</code> and asks {@link CompOp#isEffectiveFilterValue(Object)}.
   */
  @Override
  public boolean isEffectiveFilterItem(CompOp compOp, Object filterValue) {
    return  compOp != null &&
            compOp.isEffectiveFilterValue(filterValue);
  }

  // -- getter/setter --

  @Override
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  @Override
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

}

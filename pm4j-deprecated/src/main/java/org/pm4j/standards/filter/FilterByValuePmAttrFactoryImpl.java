package org.pm4j.standards.filter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pm4j.common.query.CompOp;
import org.pm4j.common.query.FilterCompareDefinition;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.impl.PmAttrBigDecimalImpl;
import org.pm4j.core.pm.impl.PmAttrBooleanImpl;
import org.pm4j.core.pm.impl.PmAttrDateImpl;
import org.pm4j.core.pm.impl.PmAttrDoubleImpl;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrLongImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.standards.filter.FilterSetProvider.FilterByValuePmAttrFactory;



public class FilterByValuePmAttrFactoryImpl implements FilterByValuePmAttrFactory{
   private Map<Class<? extends Enum<?>>, List<Enum<?>>> enumOptions = new HashMap<Class<? extends Enum<?>>, List<Enum<?>>>();

  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public PmAttr<?> makeValueAttrPm(FilterItemPm<?> parentPm, FilterCompareDefinition fd, CompOp co) {
    Class<?> attrType = fd.getAttr().getType();
    if (String.class.equals(attrType)) {
      return new PmAttrStringImpl(parentPm);
    }
    if (Integer.class.equals(attrType)) {
      return new PmAttrIntegerImpl(parentPm);
    }
    if (Long.class.equals(attrType)) {
      return new PmAttrLongImpl(parentPm);
    }
    if (Boolean.class.equals(attrType)) {
      return new PmAttrBooleanImpl(parentPm);
    }
    if (BigDecimal.class.equals(attrType)) {
        return new PmAttrBigDecimalImpl(parentPm);
    }
    if (Double.class.equals(attrType)) {
      return new PmAttrDoubleImpl(parentPm);
    }
    if (Enum.class.isAssignableFrom(attrType)) {
// TODO oboede: option restrictions are not yet implemented.
//      List<Enum<?>> enumOptionValues = findEnumValueOptionsForType((Class<Enum<?>>)attrType);
//      if (enumOptionValues != null) {
//          return new PmAttrEnumImpl(parentPm, enumOptionValues);
//
//      } else {
//          return new PmAttrEnumImpl(parentPm, attrType);
//      }
      return new PmAttrEnumImpl(parentPm, attrType);
    }
    if (Date.class.equals(attrType)) {
      return new PmAttrDateImpl(parentPm);
    }
    // XXX DZA: LocalDate, LocalTime please add this cases in v0.6
    
    // fall back:
    return new PmAttrStringImpl(parentPm);
  }
  
  /**
   *   
   * @param type
   * @return
   */
  @SuppressWarnings("unchecked")
  protected <T extends Enum<?>> List<T> findEnumValueOptionsForType(Class<T> type) {
    return (List<T>)enumOptions.get(type);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Enum<?>> void setEnumOptions(Class<T> type, List<T> enums) {
    enumOptions.put(type, (List<Enum<?>>) enums);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Enum<?>> void setEnumOptions(Class<T> type, T... enums) {
    enumOptions.put(type, (List<Enum<?>>) Arrays.<T> asList(enums));
  }
}

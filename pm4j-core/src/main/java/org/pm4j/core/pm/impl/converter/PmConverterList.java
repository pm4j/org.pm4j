package org.pm4j.core.pm.impl.converter;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.pm.PmAttr;

/**
 * Converts a list by using a dedicated {@link #itemConverter} for the
 * list items.
 *
 * @author olaf boede
 *
 * @param <T_ITEM> Type of the list items.
 */
public class PmConverterList<T_ITEM> implements PmAttr.Converter<List<T_ITEM>> {

  private PmAttr.Converter<T_ITEM> itemConverter;
  private String stringSeparator = ",";

  public PmConverterList(PmAttr.Converter<T_ITEM> itemConverter) {
    this.itemConverter = itemConverter;
  }

  @Override
  public List<T_ITEM> stringToValue(PmAttr<?> pmAttr, String s) throws PmConverterException {
    String[] strings = s.split(stringSeparator);
    List<T_ITEM> list = new ArrayList<T_ITEM>(strings.length);

    for (String itemString : strings) {
      list.add(itemConverter.stringToValue(pmAttr, itemString));
    }

    return list;
  }

  @Override
  public String valueToString(PmAttr<?> pmAttr, List<T_ITEM> v) {
    StringBuilder sb = new StringBuilder(100);
    for (T_ITEM item : v) {
      if (sb.length() > 0) {
        sb.append(stringSeparator);
      }
      sb.append(itemConverter.valueToString(pmAttr, item));
    }
    return sb.toString();
  }

}

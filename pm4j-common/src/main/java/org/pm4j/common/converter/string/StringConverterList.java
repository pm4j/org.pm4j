package org.pm4j.common.converter.string;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a list by using a dedicated {@link #itemConverter} for the
 * list items.
 *
 * @param <T_ITEM> Type of the list items.
 *
 * @author Olaf Boede
 */
public class StringConverterList<T_ITEM> implements StringConverter<List<T_ITEM>> {

  private StringConverter<T_ITEM> itemConverter;
  private String stringSeparator = ",";

  public StringConverterList(StringConverter<T_ITEM> itemConverter) {
    this.itemConverter = itemConverter;
  }

  @Override
  public List<T_ITEM> stringToValue(StringConverterCtxt ctxt, String s) throws StringConverterParseException {
    if (s == null) {
      return null;
    }
    String[] strings = s.split(stringSeparator);
    List<T_ITEM> list = new ArrayList<T_ITEM>(strings.length);

    for (String itemString : strings) {
      list.add(itemConverter.stringToValue(ctxt, itemString));
    }

    return list;
  }

  @Override
  public String valueToString(StringConverterCtxt ctxt, List<T_ITEM> v) {
    if (v == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder(100);
    for (T_ITEM item : v) {
      if (sb.length() > 0) {
        sb.append(stringSeparator);
      }
      sb.append(itemConverter.valueToString(ctxt, item));
    }
    return sb.toString();
  }

  /**
   * @param stringSeparator the stringSeparator to set
   */
  public void setStringSeparator(String stringSeparator) {
    this.stringSeparator = stringSeparator;
  }

}

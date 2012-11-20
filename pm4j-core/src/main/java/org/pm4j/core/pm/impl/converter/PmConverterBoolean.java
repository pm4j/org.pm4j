package org.pm4j.core.pm.impl.converter;

import org.pm4j.core.pm.PmAttr;

public class PmConverterBoolean extends PmConverterSerializeableBase<Boolean> {

  public static final PmConverterBoolean INSTANCE = new PmConverterBoolean();

  @Override
  public Boolean stringToValue(PmAttr<?> pmAttr, String s) {
    return Boolean.valueOf(s);
  }

}

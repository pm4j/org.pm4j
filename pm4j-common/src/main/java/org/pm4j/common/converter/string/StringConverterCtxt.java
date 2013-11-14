package org.pm4j.common.converter.string;

import java.util.Locale;
import java.util.TimeZone;

import org.pm4j.common.converter.value.ValueConverterCtxt;

public interface StringConverterCtxt extends ValueConverterCtxt {

  String getFormatString();

  // XXX oboede: too prominent location.
  // May be a property of the converter.
  String getFormatSplitString();

}

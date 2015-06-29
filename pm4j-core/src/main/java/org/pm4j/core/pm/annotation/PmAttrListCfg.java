package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.common.converter.string.StringConverter;
import org.pm4j.common.converter.string.StringConverterToString;


/**
 * PM list attribute constraints.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmAttrListCfg {

  /** By default the string representation of a <code>PmAttrList</code>. */
  public static final String DEFAULT_STRING_ITEM_SEPARATOR = ",";

  /**
   * @return The converter class for items of the list value.
   */
  @SuppressWarnings("rawtypes")
  Class<? extends StringConverter> itemStringConverter() default StringConverterToString.class;

  /**
   * @return The string used by the list-to-string converter to separate the
   *         list items in the to/from string conversion.
   */
  String valueStringSeparator() default DEFAULT_STRING_ITEM_SEPARATOR;
}

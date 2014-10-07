package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.common.converter.string.StringConverter;


/**
 * PM list attribute constraints.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmAttrListCfg {

  /**
   * @return The converter class for items of the list value.
   */
  @SuppressWarnings("rawtypes")
  Class<? extends StringConverter> itemStringConverter() default StringConverter.class;
  
  /**
   * @return The string used by the list-to-string converter to separate the
   *         list items in the to/from string conversion.
   */
  String valueStringSeparator() default ",";
}

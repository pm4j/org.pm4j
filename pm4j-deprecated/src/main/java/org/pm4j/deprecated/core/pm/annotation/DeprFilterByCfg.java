package org.pm4j.deprecated.core.pm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmDefaults;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.deprecated.core.pm.filter.DeprFilterByDefinition;

/**
 * Annotation configuration for a {@link DeprFilterByDefinition}.
 *
 * @author olaf boede
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
public @interface DeprFilterByCfg {

  /**
   * The type of filter definition used for this column.
   * <p>
   * The provided filter class need to have a constructor signature with a
   * single PM parameter. The column PM will be passed as constructor argument.
   * <p>
   * If this parameter is omitted the class provided by
   * {@link PmDefaults#getDefaultFilterByDefintionClass()} will be used.
   *
   * @return The column filter definitions.
   */
  Class<? extends DeprFilterByDefinition> value() default DeprFilterByDefinition.class;

  /**
   * Defines the PM class that can be used to enter the filter value.
   * <p>
   * If the parameter is not passed, the default implementation of the {@link DeprFilterByDefinition} will be used.
   * This is usually {@link PmAttrStringImpl}.
   *
   * @return The PM class for entering the filter value.
   */
  Class<?> valueAttrPm() default PmAttr.class;

}

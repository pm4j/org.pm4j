package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.PmTable;

/**
 * Annotation configuration for {@link PmTable} instances.
 * 
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface PmTableCfg {

  /**
   * An optional default setting for column sortability.  
   * 
   * @return <code>TRUE</code> if the column may be sorted by default.<br>
   *         <code>FALSE</code> or <code>UNDEFINED</code> if the column is not sortable by default.
   */
  PmBoolean sortable() default PmBoolean.UNDEFINED;
  
}

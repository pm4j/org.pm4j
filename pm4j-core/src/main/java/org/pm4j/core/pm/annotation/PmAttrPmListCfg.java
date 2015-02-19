package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * PM list attribute constraints.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmAttrPmListCfg {

  /**
   * Defines if the list attribute should provide invisible PM items.
   * <p>
   * The default setting is <code>false</code>. - The list provides only visible
   * PM items.
   *
   * @return <code>true</code> to provide a mixed list of visible and invisible
   *         items.
   */
  boolean provideInvisibleItems() default false;

}

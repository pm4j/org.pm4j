package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.impl.converter.PmConverterOptionBased;


/**
 * PM list attribute constraints.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmAttrPmListCfg {

  /**
   * You may specify here an specific converter that translates between PMs and
   * string-IDs, used for option lists.
   * <p>
   * If no converter is specified, by default a {@link PmConverterOptionBased}
   * will be used that uses the method {@link PmElement#getPmKey()} to identify
   * the option id.
   *
   * @return The converter class for items of the list value.
   */
  Class<?> itemConverter() default Void.class;

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

  /**
   * Lazy load scenario support:<br>
   * Defines whether the {@link PmElement#pmTouchAll()} method should be called
   * for each loaded item.
   * <p>
   * Please take care: The lazy load initialization may take some time to run.
   * Use this setting only when required.<br>
   * You may customize the PM specific lazy load initialization by overriding
   * {@link PmElement#pmTouchAll()}.
   *
   * @return <code>true</code> for lazy load support.
   */
  boolean touchItems() default false;
}

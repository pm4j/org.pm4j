package org.pm4j.deprecated.core.sample.admin.remote_sample.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * May be used to define a remote PM configruation.
 * <p>
 * TODOC olaf:
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmServerCfg {

  /**
   * Defines the aspects provided from the server.<br>
   * These aspects will be transferred in case of a client PM request.
   * <p>
   * The default setting just transfers the {@link PmAspect#VALUE}.
   * <p>
   * In case of an empty set configuration (aspect={}) the PM item will not be
   * considered in client requests. Thus, no corresponding PM item needs to
   * exist on client side.
   *
   * @return The set of PM aspects to transfer.
   */
//  PmAspect[] providedAspects() default PmAspect.VALUE;

}

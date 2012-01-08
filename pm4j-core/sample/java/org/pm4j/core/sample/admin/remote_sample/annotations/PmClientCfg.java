package org.pm4j.core.sample.admin.remote_sample.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;

/**
 * May be used to define a remote PM configruation.
 * <p>
 * TODOC olaf:
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmClientCfg {

  /**
   * Defines the PM on the server that provides content aspects for this PM.
   * <p>
   * Examples:
   * <ul>
   *  <li>@PmClientCfg(serverPm="#userSession.someDialogPm")<br>
   *      Points to a fix address for a server managed PM.</li>
   *  <li>@PmClientCfg(serverPm="#userSession.getSomeDataPm('123')")<br>
   *      Addresses a server PM based on a function call.</li>
   * </ul> 
   *
   * @return A path expression addressing the corresponding server object.
   */
  String serverPm() default "";
  
//  /**
//   * Defines the server provided PM aspects.
//   *  
//   * @return The set of aspects provided by the server. ???
//   */
//  PmAspect[] serverProvided() default PmAspect.ALL;
  
  /**
   * Defines if a PM interacts with the corresponding server PM.
   * <p>
   * If this is set to <code>false</code> for a {@link PmCommand}, it will be
   * executed on client side only.<br>
   * A corresponding server command will not be addressed and called.
   * <p>
   * If this is set to <code>false</code> for a {@link PmAttr} or some other content
   * providing PM, its content will not be send to the server on command execution.  
   * 
   * @return <code>false</code> for client side only PMs.
   */
  boolean isServerProvided() default true;
}

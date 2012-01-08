package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.PmCommand.CmdKind;
import org.pm4j.core.pm.api.PmCacheApi;

/**
 * Static configuration data for commands.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmCommandCfg {

  /**
   * @return <code>true</code> when the command should fail when there are invalid
   *         values within the current session.
   */
  boolean requiresValidValues() default true;

  /**
   * @return The {@link CmdKind} of this command class.
   */
  CmdKind cmdKind() default CmdKind.COMMAND;

  /**
   * @return <code>true</code> when the command should be hidden when not applicable.
   */
  boolean hideWhenNotEnabled() default false;

  /**
   * Defines the caches to clear within the element context of this command.
   *
   * @return The cache kinds to clear.
   */
  PmCacheApi.CacheKind[] clearCaches() default {};

}

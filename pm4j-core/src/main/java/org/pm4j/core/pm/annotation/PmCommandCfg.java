package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.PmCommand.CmdKind;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmDefaults;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.impl.PmCommandImpl;

/**
 * Static configuration data for commands.
 * 
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface PmCommandCfg {

  public enum BEFORE_DO {
    /**
     * {@link PmCommandImpl#validate()} will be executed before the
     * <code>doItImpl()</code> method gets executed.<br>
     * This call validates the entered data of the first parent not
     * being a {@link org.pm4j.core.pm.PmCommand}.
     * <p>
     * The <code>doItImpl()</code> method of the command will only be executed
     * if the validation was successful.
     */
    VALIDATE,

    /**
     * Clears all messages within the current {@link PmConversation}.
     * <p>
     * Additionally entered string values that have failed to convert to the
     * corresponding attribute value type will also be cleared.
     * <p>
     * The <code>doItImpl()</code> method of the command will be executed
     * regardless if there are errors within the related {@link PmConversation}
     * or not.
     */
    CLEAR,

    /**
     * Does not care about existing (error-) messages and does not clear any of
     * these messages before it gets executed.
     */
    DO_NOTHING,

    /**
     * The definition, defined in the project specific {@link PmDefaults} will
     * be used.
     */
    DEFAULT
  }
  
  public enum AFTER_DO {

    /**
	 * Resets the value change state of the pm that is used as validation root.
	 */
    RESET_VALUE_CHANGED_STATE,
	    
    /**
     * Clears the caches of all pm's along the path to the root pm. 
     */
    CLEAR_CACHES,
	    
    /**
     * Does not executes any special operation in the after do method of the command.
     * Has the same effect as if {@link PmCommandCfg#afterDo()} returns an empty array.
     */
    DO_NOTHING,

    /**
     * Default is 
     * <ul>
     * <li>{@link #CLEAR_CACHES} and</li>
     * <li>{@link #RESET_VALUE_CHANGED_STATE} if and only if {@link BEFORE_DO#VALIDATE} is set
     * for {@link PmCommandCfg#beforeDo()}.</li>
     * </ul>
     */
     DEFAULT
  }

  /**
   * @return Defines what should be done before executing the command logic.
   * @see BEFORE_DO
   */
  BEFORE_DO[] beforeDo() default BEFORE_DO.DEFAULT;

  /**
   * @return Defines what should be done after executing the command logic.
   * @see AFTER_DO
   */
  AFTER_DO[] afterDo() default AFTER_DO.DEFAULT;

  /**
   * @return The {@link CmdKind} of this command class.
   */
  CmdKind cmdKind() default CmdKind.COMMAND;

  /**
   * Defines the caches to clear within the element context of this command.
   * 
   * @return The cache kinds to clear.
   */
  PmCacheApi.CacheKind[] clearCaches() default {};

}

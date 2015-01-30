package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.impl.PmObjectBase;

/**
 * Allows defining fix settings for some common PM aspects.
 *
 * @author Olaf Boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmObjectCfg {

  /**
   * Configurable rules for PM enablement. May be used for a fix annotation
   * based definition.
   *
   * @author Olaf Boede
   */
  public enum Enable {
    /**
     * By default PMs are enabled.
     * <p>
     * A specific rules applies for {@link PmAttr}: It is not enabled if it's PM
     * tree is read-only.
     */
    DEFAULT,
    /**
     * Disables this PM always.
     */
    NO,
    /**
     * Enables this PM only if this PM is in an editable context.<br>
     * Technically expressed: Its method {@link PmObject#isPmEnabled()} returns
     * <code>true</code> if its method {@link PmObject#isPmReadonly()} returns
     * <code>false</code>. <br>
     * {@link PmAttr}s do that by default. That's why attempts to apply this
     * option to {@link PmAttr}s are redundant, thus unnecessary and will result
     * in {@link PmRuntimeException}.
     */
    IN_EDITABLE_CTXT
  }

  /**
   * Configurable rules for PM visibility. May be used for a fix annotation
   * based definition.
   * <p>
   * If you need a more complex visibility definition, consider overriding
   * {@link PmObjectBase#isPmVisibleImpl()}.
   */
  public enum Visible {
    /**
     * By default PMs are visible.
     */
    DEFAULT,
    /**
     * Makes this PM always invisible.
     */
    NO,
    /**
     * Is visible only if this PM is in an editable context.<br>
     * Technically expressed: Its method {@link PmObject#isPmReadonly()} returns
     * <code>false</code>.
     */
    IN_EDITABLE_CTXT,
    /**
     * Is only visible if it is enabled.<br>
     * Technically expressed: Its method {@link PmObject#isPmVisble()} returns
     * <code>true</code> if {@link PmObject#isPmEnabled()} returns
     * <code>true</code>.
     */
    IF_ENABLED,
    /**
     * Is only visible if there is a value to show.
     * <p>
     * Applies only for PMs having values, such as {@link PmAttr}, {@link PmTable} and {@link PmBean}.
     */
    IF_NOT_EMPTY
  }

  /**
   * Enablement rule for this PM.
   * <p>
   * If you need a more complex enablement definition, consider overriding
   * {@link PmObjectBase#isPmEnabledImpl()} or
   * {@link PmObjectBase#isPmReadonlyImpl()} (to control PM trees).
   *
   * @return the configured enablement definition.
   */
  Enable enabled() default Enable.DEFAULT;

  /**
   * Visibility rule for this PM.
   * <p>
   * If you need a more complex visibility definition, consider overriding
   * {@link PmObjectBase#isPmVisibleImpl()}.
   *
   * @return the configured visibility definition.
   */
  Visible visible() default Visible.DEFAULT;
}

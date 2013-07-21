package org.pm4j.core.pm;

import org.pm4j.core.pm.annotation.PmFactoryCfg;

/**
 * Presentation model for references to other bean that is represented by a PM.
 * <p>
 * The PM for the referenced bean will be created according to the
 * {@link PmFactoryCfg}. That configuration may also be inherited by its parent
 * context.
 *
 * @author olaf boede
 */
public interface PmAttrPmRef<T_REFED_PM extends PmBean<?>> extends PmAttr<T_REFED_PM> {

  /**
   * Sets the reference using a bean.
   *
   * @param bean A bean to reference.
   * @return The presentation model for the given bean.
   */
  T_REFED_PM setValueAsBean(Object bean);

  /**
   * @return The bean behind the referenced model.
   */
  Object getValueAsBean();

  /**
   * Repeated base class signature. Allows reflection based frameworks (such as
   * EL) to identify the referenced type.
   */
  @Override
  T_REFED_PM getValue();
}

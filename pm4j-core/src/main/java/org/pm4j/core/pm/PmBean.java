package org.pm4j.core.pm;

import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.impl.PmBeanBase;

/**
 * Presentation model for data beans.
 *
 * @param <T_BEAN> The type of the supported data bean.
 */
public interface PmBean<T_BEAN> extends PmObject {

  /**
   * @return The data bean behind this presentation model element.<br>
   *         May provide <code>null</code> if the backing bean is
   *         <code>null</code> and:
   *         <ul>
   *         <li>{@link PmBeanCfg#autoCreateBean()} is not <code>true</code> and
   *         </li>
   *         <li>there is no definition of {@link PmBeanBase#findPmBeanImpl()}
   *         that provides an instance.</li>
   *         </ul>
   */
  T_BEAN getPmBean();

  /**
   * Re-associates the PM to another bean instance and fires all change events
   * for this instance and all children.
   *
   * @param bean The new bean behind this PM.
   */
  void setPmBean(T_BEAN bean);

  /**
   * Re-associates the PM to a reloaded bean instance and fires all change events
   * for this instance and all children.<br>
   * The fired change event also has the flag {@link PmEvent#RELOAD}.
   *
   * @param bean The new bean behind this PM.
   */
  void reloadPmBean(T_BEAN reloadedBean);

  /**
   * @return The class of the bean behind this PM.
   */
  Class<?> getPmBeanClass();

}

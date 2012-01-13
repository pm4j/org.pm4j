package org.pm4j.core.pm;

import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.impl.PmBeanBase;

/**
 * Presentation model for data beans.
 *
 * @param <T_BEAN> The type of the supported data bean.
 */
public interface PmBean<T_BEAN> extends PmElement {

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
   * @return The class of the bean behind this PM.
   */
  Class<?> getPmBeanClass();

}

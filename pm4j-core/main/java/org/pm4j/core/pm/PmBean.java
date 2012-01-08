package org.pm4j.core.pm;

import org.pm4j.core.exception.PmRuntimeException;




/**
 * Presentation model for data beans.
 *
 * @param <T_BEAN> The type of the supported data bean.
 */
public interface PmBean<T_BEAN> extends PmElement {

  /**
   * @return The data bean behind this presentation model element.
   * @throws PmRuntimeException if the backing bean is <code>null</code>.
   */
  T_BEAN getPmBean();

  /**
   * @return The data bean behind this presentation model element.<br>
   * May provide <code>null</code> if the backing bean is <code>null</code>.
   */
  T_BEAN findPmBean();

  /**
   * @return The class of the bean behind this PM.
   */
  Class<?> getPmBeanClass();

}

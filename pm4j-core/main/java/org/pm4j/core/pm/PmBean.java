package org.pm4j.core.pm;




/**
 * Presentation model for data beans.
 *
 * @param <T_BEAN> The type of the supported data bean.
 */
public interface PmBean<T_BEAN> extends PmElement {

  /**
   * @return The data bean behind this presentation model element.
   */
  T_BEAN getPmBean();

  /**
   * @return The class of the bean behind this PM.
   */
  Class<?> getPmBeanClass();

}

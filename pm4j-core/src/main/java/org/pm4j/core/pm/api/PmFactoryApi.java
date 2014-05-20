package org.pm4j.core.pm.api;

import java.util.Collection;
import java.util.List;

import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmFactoryApiHandler;

public class PmFactoryApi {

  private static PmFactoryApiHandler apiHandler = new PmFactoryApiHandler();

  /**
   * Searches an existing presentation model for the given bean. Will create a
   * new model when no one exists yet.
   * <p>
   * The method call will return <code>null</code> when the given bean is
   * <code>null</code>.
   *
   * @param bean
   *          The bean to get the presentation model for. May be <code>null</code>.
   * @return The presentation model for the given bean.
   */
  public static <T, T_PM extends PmBean<T>> T_PM getPmForBean(PmObject pmCtxt, T bean) {
    return apiHandler.<T, T_PM>getPmForBean(pmCtxt, bean);
  }


  /**
   * Searches an existing presentation model for the given bean.
   * Will <b>not</b> create a new model when none found.
   *
   * @param bean The bean to get the presentation model for.
   * @return The presentation model for the given bean or <code>null</code>.
   */
  public static <T extends PmBean<?>> T findPmForBean(PmObject pmCtxt, Object bean) {
    return apiHandler.<T>findPmForBean(pmCtxt, bean);
  }

  /**
   * Convenience method that calls {@link #getPmForBean(Object)} for each item
   * within the given list.
   *
   * @param pmParent
   *          The PM context for the PMs to create.
   * @param beanList
   *          The objects to get PMs for. Can be <code>null</code> or empty.
   * @param excludeInvisible
   *          <code>true</code> adds only visible {@link PmBean}s to the list.
   * @return The matching list of PMs, sorted in the same order as the given
   *         collection.<br>
   *         Is never <code>null</code>.<br>
   *         In case of an empty set it provides an
   *         unmodifiable list.
   */
  public static <T> List<? extends PmBean<T>> getPmListForBeans(PmObject pmParent, Collection<T> beanList, boolean excludeInvisible) {
    return apiHandler.getPmListForBeans(pmParent, beanList, excludeInvisible);
  }

}

package org.pm4j.core.pm.impl;

import java.util.Collection;
import java.util.Collections;

import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;

/**
 * Provides INTERNAL methods for bean factory cache handling.
 *
 * @author olaf boede
 */
public final class BeanPmCacheUtil {

  public static void clearBeanPmCache(PmObject factoryOwningPm) {
    if (((PmObjectBase)factoryOwningPm).pmBeanFactoryCache != null) {
      ((PmObjectBase)factoryOwningPm).pmBeanFactoryCache.clear();
    }
  }

  /**
   * Clears all bean PM caches within the given PM tree.
   *
   * @param rootPm The root of the PM tree to handle.
   */
  public static void clearBeanPmCachesOfSubtree(PmObject rootPm) {
    rootPm.accept(new PmVisitorAdapter() {
      @Override
      protected void onVisit(PmObject pm) {
        // There are no caches to clear if the PM is not yet initialized.
        if (PmInitApi.isPmInitialized(pm)) {
          BeanPmCacheUtil.clearBeanPmCache(pm);
          for (PmObject c : PmUtil.getPmChildren(pm)) {
            c.accept(this);
          }
        }
      }
    });
  }

  public static void removeBeanPm(PmObject factoryOwningPm, PmBean<?> pmToRemove) {
    if (((PmObjectBase)factoryOwningPm).pmBeanFactoryCache != null) {
      ((PmObjectBase)factoryOwningPm).pmBeanFactoryCache.removePm(pmToRemove);
    }
  }

  public static void removeBean(PmObject factoryOwningPm, Object beanToRemove) {
    if (((PmObjectBase)factoryOwningPm).pmBeanFactoryCache != null) {
      ((PmObjectBase)factoryOwningPm).pmBeanFactoryCache.removeBean(beanToRemove);
    }
  }

  /**
   * Provides the set of currenty cached PM instances.
   * @param factoryOwningPm
   * @return
   */
  @SuppressWarnings("unchecked")
  public static Collection<PmBean<?>> getCachedPms(PmObject factoryOwningPm) {
    return (((PmObjectBase)factoryOwningPm).pmBeanFactoryCache != null)
        ? ((PmObjectBase)factoryOwningPm).pmBeanFactoryCache.getItems()
        : Collections.EMPTY_LIST;
  }

}

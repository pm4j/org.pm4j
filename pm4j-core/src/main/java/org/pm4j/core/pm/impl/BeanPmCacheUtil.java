package org.pm4j.core.pm.impl;

import java.util.Collection;
import java.util.Collections;

import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHint;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitResult;

/**
 * Provides INTERNAL methods for bean factory cache handling.
 *
 * @author olaf boede
 */
public final class BeanPmCacheUtil {

  /**
   * Clears the PM factory cache for the given particular PM.<br>
   * Does NOT apply the call recursively to child PMs.
   *
   * @param factoryOwningPm
   */
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
    PmVisitCallBack callBack = new PmVisitCallBack() {
      @Override
      public PmVisitResult visit(PmObject pm) {
        BeanPmCacheUtil.clearBeanPmCache(pm);
        return PmVisitResult.CONTINUE;
      }
    };
    PmVisitorApi.visit(rootPm, callBack, PmVisitHint.SKIP_NOT_INITIALIZED);
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

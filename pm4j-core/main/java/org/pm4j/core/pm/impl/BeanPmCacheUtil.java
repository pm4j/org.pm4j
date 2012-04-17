package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;

class BeanPmCacheUtil {

  public static final void clearBeanPmCache(PmObject factoryOwningPm) {
    if (((PmObjectBase)factoryOwningPm).pmBeanFactoryCache != null) {
      ((PmObjectBase)factoryOwningPm).pmBeanFactoryCache.clear();
    }
  }

  public static final void removeBeanPm(PmObject factoryOwningPm, PmBean<?> pmToRemove) {
    if (((PmObjectBase)factoryOwningPm).pmBeanFactoryCache != null) {
      ((PmObjectBase)factoryOwningPm).pmBeanFactoryCache.removePm(pmToRemove);
    }
  }

  public static final void removeBean(PmObject factoryOwningPm, Object beanToRemove) {
    if (((PmObjectBase)factoryOwningPm).pmBeanFactoryCache != null) {
      ((PmObjectBase)factoryOwningPm).pmBeanFactoryCache.removeBean(beanToRemove);
    }
  }

}

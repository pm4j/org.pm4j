package org.pm4j.core.pm.impl;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmBean;

class BeanPmCache {

  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(PmConversationImpl.class);

  private Map<BeanIdentity, WeakReference<PmBean<?>>> beanIdentityToPmMap = new WeakHashMap<BeanIdentity, WeakReference<PmBean<?>>>();

  /**
   * Keeps the bean identity alive as long as the presentation model instance
   * is in use.
   */
  private Map<PmBean<?>, BeanIdentity> pmToBeanIdentityMap = new WeakHashMap<PmBean<?>, BeanIdentity>();

  public void add(PmBean<?> pmElement) {
    Object bean = pmElement.getPmBean();
    BeanIdentity beanIdentity = new BeanIdentity(bean);
    WeakReference<PmBean<?>> pmRef = new WeakReference<PmBean<?>>(pmElement);
    WeakReference<PmBean<?>> oldPmRef = beanIdentityToPmMap.put(beanIdentity, pmRef);
    if (oldPmRef != null && oldPmRef.get() != null) {
      throw new PmRuntimeException(pmElement, "Bean identity already added to the PM bean cache: " + beanIdentity);
    }
    if (pmToBeanIdentityMap.put(pmElement, beanIdentity) != null) {
      throw new PmRuntimeException(pmElement, "Bean presentation model already added to the PM bean cache: " + pmElement);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends PmBean<?>> T findByBean(Object bean) {
    WeakReference<PmBean<?>> ref = beanIdentityToPmMap.get(new BeanIdentity(bean));
    return (T) (ref != null
                ? ref.get()
                : null);
  }

  /**
   * Holds only a weak reference to the referenced bean.
   */
  static class BeanIdentity {
    private final WeakReference<Object> beanRef;
    private final int hashCode;

    public BeanIdentity(Object bean) {
      this.beanRef = new WeakReference<Object>(bean);
      this.hashCode = bean.hashCode();
    }

    public boolean isActiveBeanRef() {
      return beanRef.get() != null;
    }

    @Override
    public boolean equals(Object obj) {
      return (obj != null) &&
             (beanRef.get() == ((BeanIdentity)obj).beanRef.get());
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public String toString() {
      return ObjectUtils.identityToString(beanRef.get());
    }
  }
}

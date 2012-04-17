package org.pm4j.core.pm.impl;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmBean;

class BeanPmCacheImpl implements BeanPmCache {

  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(BeanPmCacheImpl.class);

  private Map<Object, WeakReference<PmBean<?>>> beanEqualToPmMap = new WeakHashMap<Object, WeakReference<PmBean<?>>>();
  private Map<BeanIdentity, WeakReference<PmBean<?>>> beanIdentityToPmMap = new WeakHashMap<BeanIdentity, WeakReference<PmBean<?>>>();

  /**
   * Keeps the bean identity alive as long as the presentation model instance
   * is in use.
   */
  private Map<PmBean<?>, BeanIdentity> pmToBeanIdentityMap = new WeakHashMap<PmBean<?>, BeanIdentity>();

  /**
   * Registers the bean-to-PM mapping(s).
   *
   * @param pmElement A new PM for a bean.
   */
  @Override
  public void add(PmBean<?> pmElement) {
    Object bean = pmElement.getPmBean();
    BeanIdentity beanIdentity = new BeanIdentity(bean);
    WeakReference<PmBean<?>> pmRef = new WeakReference<PmBean<?>>(pmElement);

    // Add it to the identity map. The bean should not yet be registered there.
    WeakReference<PmBean<?>> oldPmRef = beanIdentityToPmMap.put(beanIdentity, pmRef);
    if (oldPmRef != null && oldPmRef.get() != null) {
      throw new PmRuntimeException(pmElement, "Bean identity already added to the PM bean cache: " + beanIdentity);
    }

    // Add it to the equals map.
    WeakReference<PmBean<?>> oldPmEqualRef = beanEqualToPmMap.put(bean, pmRef);
    if (oldPmEqualRef != null && oldPmEqualRef.get() != null) {
      throw new PmRuntimeException(pmElement, "An equal bean is already added to the PM bean cache: " + beanIdentity);
    }

    // Check if the set of beans for duplicates. That may indicate a bug:
    // TODO olaf: This check adds an equals condition for the beans. Check if that is
    //            correct for all use cases.
    //            If not: Add a switch.
    if (pmElement.getPmConversation().getPmDefaults().debugHints) {
      HashSet<Object> beanSet = new HashSet<Object>();
      for (BeanIdentity i : beanIdentityToPmMap.keySet()) {
        Object o = i.beanRef.get();
        if (o != null) {
          if (!beanSet.add(o)) {
            throw new PmRuntimeException(pmElement, "Bean already added to the PM bean cache: " + PmUtil.getPmLogString(pmElement));
          }
        }
      }
    }

    if (pmToBeanIdentityMap.put(pmElement, beanIdentity) != null) {
      throw new PmRuntimeException(pmElement, "Bean presentation model already added to the PM bean cache: " + PmUtil.getPmLogString(pmElement));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends PmBean<?>> T findByBean(Object bean) {
    T pm = findByBeanIdentity(bean);
    return (T) (pm == null
        ? findByBeanEquals(bean)
        : pm);
  }

  @SuppressWarnings("unchecked")
  private <T extends PmBean<?>> T findByBeanIdentity(Object bean) {
    WeakReference<PmBean<?>> ref = beanIdentityToPmMap.get(new BeanIdentity(bean));
    return (T) (ref != null
        ? ref.get()
        : null);
  }

  @SuppressWarnings("unchecked")
  private <T extends PmBean<?>> T findByBeanEquals(Object bean) {
    WeakReference<PmBean<?>> ref = beanEqualToPmMap.get(bean);
    return (T) (ref != null
        ? ref.get()
        : null);
  }

  @Override
  public void removePm(PmBean<?> pmBean) {
    Object bean = pmBean.getPmBean();
    pmToBeanIdentityMap.remove(pmBean);
    if (bean != null) {
      beanEqualToPmMap.remove(bean);
      beanIdentityToPmMap.remove(bean);
    }
  }

  @Override
  public void removeBean(Object bean) {
    WeakReference<PmBean<?>> pmRef = beanIdentityToPmMap.remove(bean);
    if (pmRef != null && pmRef.get() != null) {
      pmToBeanIdentityMap.remove(pmRef.get());
    }
    pmRef = beanEqualToPmMap.remove(bean);
    if (pmRef != null && pmRef.get() != null) {
      pmToBeanIdentityMap.remove(pmRef.get());
    }
  }

  @Override
  public void clear() {
    pmToBeanIdentityMap.clear();
    beanEqualToPmMap.clear();
    beanIdentityToPmMap.clear();
  }


  /**
   * Holds only a weak reference to the referenced bean.
   */
  static class BeanIdentity {
    private final WeakReference<Object> beanRef;
    private final int hashCode;

    public BeanIdentity(Object bean) {
      this.beanRef = new WeakReference<Object>(bean);
      this.hashCode = System.identityHashCode(bean);
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

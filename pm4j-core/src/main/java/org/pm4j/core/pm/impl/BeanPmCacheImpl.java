package org.pm4j.core.pm.impl;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;

class BeanPmCacheImpl implements BeanPmCache {

  private static final Logger LOG = LoggerFactory.getLogger(BeanPmCacheImpl.class);

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

    if (LOG.isTraceEnabled()) {
      LOG.trace(this + ": added PM '" + logString(pmElement) + "' for bean: " + bean);
    }

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

    if (LOG.isTraceEnabled()) {
      LOG.trace(this + ": removed PM '" + logString(pmBean) + "' for bean: " + bean);
    }

    pmToBeanIdentityMap.remove(pmBean);
    if (bean != null) {
      beanEqualToPmMap.remove(bean);
      beanIdentityToPmMap.remove(bean);
    }
  }

  @Override
  public void removeBean(Object bean) {
    WeakReference<PmBean<?>> pmRef = beanIdentityToPmMap.remove(bean);

    if (LOG.isTraceEnabled()) {
      LOG.trace(this + ": removed bean '" + bean + "' cached PM was: " + (pmRef != null ? logString(pmRef.get()) : null));
    }

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
    int size = pmToBeanIdentityMap.size();
    if (size > 0 && LOG.isTraceEnabled()) {
      LOG.trace(this + ": clear called. Removed " + size + " entries.");
      for (Object o : beanEqualToPmMap.keySet()) {
        LOG.trace("  cleared bean reference: " + o);
      }
    }

    pmToBeanIdentityMap.clear();
    beanEqualToPmMap.clear();
    beanIdentityToPmMap.clear();
  }

  @Override
  public boolean isEmpty() {
    return pmToBeanIdentityMap.isEmpty();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<PmBean<?>> getItems() {
    return (Collection<PmBean<?>>)(Object)pmToBeanIdentityMap.keySet();
  }

  private String logString(PmObject pm) {
    return pm != null
        ? (PmInitApi.isPmInitialized(pm) ? pm.getPmRelativeName() : pm.getClass().getSimpleName()) + "(" + Integer.toHexString(pm.hashCode()) + ")"
        : null;
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

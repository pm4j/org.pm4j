package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmFactoryCfg;

public class PmFactoryApiHandler {

  /**
   * Searches an existing presentation model for the given bean. Will create a
   * new model when no one exists yet.
   * <p>
   * The method call will return <code>null</code> when the given bean is
   * <code>null</code>.
   *
   * @param bean
   *          The bean to get the presentation model for.
   * @return The presentation model for the given bean.
   */
  public <T, T_PM extends PmBean<T>> T_PM getPmForBean(PmObject pmCtxt, T bean) {
    // no bean - no pm.
    if (bean == null) {
      return null;
    }

    PmConversation pmConversation = pmCtxt.getPmConversation();
    boolean supportFactoryHierarchy = pmConversation.getPmDefaults().supportFactoryHierarchy;
    synchronized (pmConversation) {
      T_PM pm = this.<T_PM>findPmForBean(pmCtxt, bean);
      if (pm != null) {
        return pm;
      }

      BeanPmFactory factory = null;
      if (supportFactoryHierarchy) {
        factory = ((PmObjectBase)pmCtxt).getOwnPmElementFactory();
      }
      else {
        factory = findPmFactory(pmCtxt);
        if (factory == null) {
          throw new PmRuntimeException(pmCtxt, "Please add a @PmFactoryCfg configuration to be able to create a PM for a bean of type " + bean.getClass());
        }
      }

      if (factory != null &&
          factory.canMakePmFor(bean)) {
        pm = factory.<T_PM>makePm(pmCtxt, bean);
      }
      else if (supportFactoryHierarchy) {
        PmObject pmParent = pmCtxt.getPmParent();
        if (pmParent != null) {
          pm = this.<T, T_PM>getPmForBean(pmParent, bean);
        }
      }

      if (pm == null) {
        // FIXME olaf: only provides context information for the highest factory container (the session)
        //             does not really provide a hint for the attribute/element location, the factory may be
        //             placed in too...
        throw new PmRuntimeException(pmCtxt, "Can't create presentation model for bean of class '" +
            bean.getClass() +
            "'.\nPlease check if the intended presentation model is registered in a PM-Factory." +
            "\nYou may use the annotation '" +
            PmFactoryCfg.class.getSimpleName() + ".beanPmClasses()' to specify a presentation model factory.");
      }

      return pm;
    }
  }

  /**
   * Searches an existing presentation model for the given bean.
   * Will <b>not</b> create a new model when none found.
   *
   * @param bean The bean to get the presentation model for.
   * @return The presentation model for the given bean or <code>null</code>.
   */
  public <T extends PmBean<?>> T findPmForBean(PmObject pmCtxt, Object bean) {
    PmObjectBase pmCtxtImpl = (PmObjectBase)pmCtxt;
    T pmBean = null;

    if (pmCtxtImpl.pmBeanFactoryCache != null) {
      synchronized(pmCtxt) {
        pmBean = pmCtxtImpl.pmBeanFactoryCache.<T>findByBean(bean);
      }
    }

    if (pmBean == null) {
      BeanPmFactory factory = pmCtxtImpl.getOwnPmElementFactory();

      // check in hierarchy only if the own factory (and cache) does not
      // manage objects of the given type.
      if (pmCtxt.getPmConversation().getPmDefaults().supportFactoryHierarchy &&
          (factory == null ||
           ! factory.canMakePmFor(bean))) {
        PmObject pmParent = pmCtxtImpl.getPmParent();
        if (pmParent != null) {
          pmBean = this.<T>findPmForBean(pmParent, bean);
        }
      }
    }

    return pmBean;
  }



  /**
   * Searches an existing presentation model for a bean that equals the
   * given bean instance.
   *
   * @param bean The bean to find a similar presentation model for.
   * @return The presentation model for an 'equal' bean or <code>null</code>.
   */
  // TODO olaf: Check with first project context how to get rid of this...
//  public <T extends PmBean<?>> T findPmForEqualBean(PmObject pmCtxt, Object bean) {
//    PmObjectBase pmCtxtImpl = (PmObjectBase)pmCtxt;
//    T pmBean = null;
//
//    if (pmCtxtImpl.pmBeanFactoryCache != null) {
//      synchronized(pmCtxtImpl) {
//        pmBean = pmCtxtImpl.pmBeanFactoryCache.<T>findPmForEqualBean(bean);
//      }
//    }
//
//    if (pmBean == null) {
//      BeanPmFactory factory = pmCtxtImpl.getOwnPmElementFactory();
//
//      // check in hierarchy only when the own factory (and cache) does not
//      // manage objects of the given type.
//      if (factory == null ||
//          ! factory.canMakePmFor(bean)) {
//        PmObject pmParent = pmCtxtImpl.getPmParent();
//        if (pmParent != null) {
//          pmBean = this.<T>findPmForEqualBean(pmParent, bean);
//        }
//      }
//    }
//
//    return pmBean;
//  }


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
  public <T> List<? extends PmBean<T>> getPmListForBeans(PmObject pmParent, Collection<T> beanList, boolean excludeInvisible) {
    if ((beanList != null) && (beanList.size() > 0)) {
      List<PmBean<T>> list = new ArrayList<PmBean<T>>(beanList.size());
      for (T o : beanList) {
        PmBean<T> pm = getPmForBean(pmParent, o);
        if (!excludeInvisible || pm.isPmVisible()) {
          list.add(pm);
        }
        else {
          @SuppressWarnings("unused")
          String pointToBreak = "";
        }
      }
      return list;
    }
    else {
      return Collections.emptyList();
    }
  }

  private static BeanPmFactory findPmFactory(PmObject pm) {
    BeanPmFactory factory = ((PmObjectBase)pm).getOwnPmElementFactory();

    if ((factory == null) && (pm.getPmParent() != null)) {
        factory = findPmFactory(pm.getPmParent());
    }

    return factory;
  }

}

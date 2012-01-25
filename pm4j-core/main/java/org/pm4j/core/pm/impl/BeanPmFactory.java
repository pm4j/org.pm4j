package org.pm4j.core.pm.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmBeanCfg;

/**
 * A factory that generates PMs for a fix set of registered
 * {@link PmBean} types.
 * <p>
 * It reads from the registered {@link PmBean} classes the annotation
 * {@link PmBeanCfg} to get knowledge about the associated bean class.
 *
 * @author olaf boede
 */
class BeanPmFactory {

  private static final Log LOG = LogFactory.getLog(BeanPmFactory.class);

  /** A cache that prevents repeated reflection analysis loops. */
  private Map<Class<?>, Constructor<PmBean<?>>> beanClassToPmConstructorMap = new HashMap<Class<?>, Constructor<PmBean<?>>>();

  /** A cache that prevents repeated reflection analysis loops. */
  private Set<Class<?>> classesNotHandledHere = new HashSet<Class<?>>();

  /**
   * @param beanPmClasses The set of handled {@link PmBean} classes.
   */
  public BeanPmFactory(Class<?>... beanPmClasses) {
    for (Class<?> beanPmClass : beanPmClasses) {

      PmBeanCfg annotation = AnnotationUtil.findAnnotationInClassTree(beanPmClass, PmBeanCfg.class);
      if (annotation == null) {
        throw new PmRuntimeException("Annotation '" +
            PmBeanCfg.class.getSimpleName() +
            "' needed for class '" + beanPmClass.getSimpleName() +
            "' to identify the bean class that can be handled by this presentation model.");
      }
      addClassToPmConstructorMapItem(annotation.beanClass(), beanPmClass);

// TODO: enhance class metadata inspection to prevent the (in most cases) redundant
//       beanClass attribute specification.
//      Class<?> beanClass = ClassUtil.findFirstGenericParameterOfInterface(beanPmClass, PmBean.class);
//      if (beanClass == null) {
//        throw new PmConfigurationException("Unable to identify bean class for PM class '" +
//            beanPmClass.getSimpleName() +
//            "'. Please check if the class really implementes the interface " + PmBean.class.getSimpleName());
//      }
//      addClassToPmConstructorMapItem(beanClass, beanPmClass);
    }
  }

  /**
   * Generates a PM for the given bean.
   * <p>
   * Please ensure that you call makePm only when {@link #canMakePmFor(Object)} returns <code>true</code>.
   *
   * @param pmParent
   *          The presentation model context for the new PM to generate.
   * @param object
   *          The bean to generate a PM for.
   * @return The generated PM. Never <code>null</code>.
   * @throws PmRuntimeException when {@link #canMakePmFor(Object)} would return <code>false</code> for the given object.
   */
  @SuppressWarnings("unchecked")
  public <T extends PmBean<?>> T makePm(PmObject pmParent, Object bean) {
    assert bean != null;

    Constructor<PmBean<?>> pmCtor = getCtorForPm(bean);
    if (pmCtor == null) {
      throw new PmRuntimeException(pmParent, "The factory is not responsible for class '" + bean.getClass() +
          "'. Please ensure that you call makePm only when canMakePmFor returns 'true'.");
    }

    try {
      T pm;
      if (pmCtor.getParameterTypes().length == 2) {
        // Explicit initializing ctor is defined:
        pm = (T) pmCtor.newInstance(pmParent, bean);
      }
      else {
        pm = (T) pmCtor.newInstance();
        ((PmBeanBase<Object>)pm).initPmBean(pmParent, bean);
      }

      return pm;
    } catch (Exception e) {
      String msg = "Can't create model for bean: " + bean;

      if (e instanceof InstantiationException)
        msg += " Reason: Class is abstract.";
      if (e instanceof IllegalAccessException)
        msg += " Reason: Constructor not accessible.";
      if (e instanceof IllegalArgumentException)
        msg += " Reason: Presentation model constructur with arguments (" + PmObject.class + ", " + Object.class + ") required.";
      if (e instanceof InvocationTargetException)
        msg += " Reason: Exception within the presentation model constructor.";

      throw new PmRuntimeException(pmParent, msg, e);
    }
  }

  /**
   * @param object
   *          The object to generate a PM for.
   * @return <code>true</code> when this factory can create a PM for the given
   *         object.
   */
  public boolean canMakePmFor(Object object) {
    return getCtorForPm(object) != null;
  }

  // -- Internal helper --

  private static final class LongestSuperPathComp implements Comparator<Class<?>> {
    @Override
    public int compare(Class<?> c1, Class<?> c2) {
      Integer c1Supers = ClassUtils.getAllSuperclasses(c1).size();
      Integer c2Supers = ClassUtils.getAllSuperclasses(c2).size();
      return c2Supers.compareTo(c1Supers);
    }
  }

  private Constructor<PmBean<?>> getCtorForPm(Object bean) {
    Class<?> beanClass = bean.getClass();
    Constructor<PmBean<?>> pmCtor = beanClassToPmConstructorMap.get(beanClass);

    // extra loop for proxies and super classes:
    // TODO: add support for Pms mapped to interfaces.
    if ((pmCtor == null) && !classesNotHandledHere.contains(beanClass)) {
      Set<Class<?>> foundMatches = new TreeSet<Class<?>>(new LongestSuperPathComp());
      for (Map.Entry<Class<?>, Constructor<PmBean<?>>> e : beanClassToPmConstructorMap.entrySet()) {
        if (e.getKey().isAssignableFrom(beanClass)) {
          if (LOG.isTraceEnabled()) {
            LOG.trace("Generating a PM for a base class or proxy. Bean class: " + e.getKey() + " PM-bean class: " + beanClass);
          }

          foundMatches.add(e.getKey());
        }
      }

      if (foundMatches.size() > 0) {
        Class<?> foundMappedBeanClass = foundMatches.iterator().next();
        pmCtor = beanClassToPmConstructorMap.get(foundMappedBeanClass);

        // Remember that mapping to prevent permanent re-evaluations.
        beanClassToPmConstructorMap.put(beanClass, pmCtor);
      }
      else {
        // Remember that not mapped class to prevent permanent re-evaluations.
        classesNotHandledHere.add(beanClass);
      }
    }

    return pmCtor;
  }

  @SuppressWarnings("unchecked")
  private void addClassToPmConstructorMapItem(Class<?> beanClass, Class<?> pmClass) {
    Constructor<PmBean<?>> ctor = null;
    // try two - parameter ctors first.
    for (Constructor<?> c : pmClass.getConstructors()) {
      Class<?>[] pTypes = c.getParameterTypes();
      if ((pTypes.length == 2) && (PmObject.class.isAssignableFrom(pTypes[0]))) {
        ctor = (Constructor<PmBean<?>>) c;
        break;
      }
    }

    if (ctor == null) {
      try {
        ctor = (Constructor<PmBean<?>>) pmClass.getConstructor();
      } catch (SecurityException e) {
        throw new PmRuntimeException("Can't initialize " + pmClass.getName() +
            ". Default constructor of bean class is not accessible.", e);
      } catch (NoSuchMethodException e) {
        throw new PmRuntimeException("Can't initialize " + pmClass.getName() +
            ". Missing default constructor for bean class.", e);
      }
    }

    beanClassToPmConstructorMap.put(beanClass, ctor);
  }

}

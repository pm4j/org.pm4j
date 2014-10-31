package org.pm4j.core.pm.impl;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.annotation.PmCacheCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg2;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Aspect;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Cache;

class PmCacheCfgUtil {
  
  /**
   * Finds all {@link PmCacheCfg} and {@link PmCacheCfg2} annotations in the
   * hierarchy of the given pm. Asserts that either the first or the latter
   * annotation type is used but never both.
   * 
   * @param pm defines the start point in the hierarchy
   * @param foundAnnotations
   * @return either a list of {@link PmCacheCfg} or {@link PmCacheCfg2}.
   */
  @SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
  static List findCacheCfgsInPmHierarchy(PmObjectBase pm, List foundAnnotations) {
    PmCacheCfg2 cfg = AnnotationUtil.findAnnotation(pm, PmCacheCfg2.class);
    if (cfg != null) {
      assertNoMixedMode(pm, foundAnnotations, cfg);
      foundAnnotations.add(cfg);
    } 
    PmCacheCfg cfgOld = AnnotationUtil.findAnnotation(pm, PmCacheCfg.class);
    if (cfgOld != null) {
      assertNoMixedMode(pm, foundAnnotations, cfgOld);
      foundAnnotations.add(cfg);
    }

    PmObjectBase pmParent = (PmObjectBase) pm.getPmParent();
    if (pmParent != null &&
        ! (pm instanceof PmConversation)) {
      findCacheCfgsInPmHierarchy(pmParent, foundAnnotations);
    }

    return foundAnnotations;
  }
  
  /**
   * Evaluates the cache strategy for a particular cache aspect of the
   * given PM. Therefore the {@link PmCacheCfg2} annotations of the PM
   * and all parent PMs will be evaluated.
   *
   * @param pm the pm in question
   * @param aspect the cache aspect to find a cache strategy for
   * @param cacheAnnotations the cache annotations of the PM hierarchy, 
   *        @see #findCacheCfgsInPmHierarchy(PmObjectBase, List)
   * @return a cache strategy for the aspect in question
   */
  static Cache findCacheForAspectInPmHierarchy(
      PmObjectBase pm,
      Aspect aspect,
      Collection<PmCacheCfg2> cacheAnnotations)
  {
    PmCacheCfg2 localCfg =  AnnotationUtil.findAnnotation(pm, PmCacheCfg2.class);
    if (localCfg != null) {
      return getCacheByAspect(localCfg, aspect);
    } 
      
    for (PmCacheCfg2 cfg : cacheAnnotations) {
      // consider all parent annotations that are
      // flagged as cascaded
      Cache parentCache = getCacheByAspect(cfg, aspect);
      if (parentCache != null && parentCache.cascade()) {
        return parentCache;
      }
    }
    
    return null;
  }
  
  @SuppressWarnings({"rawtypes"})
  private static void assertNoMixedMode(PmObjectBase currentPm, List foundAnnotations, Annotation nextFoundAnnotation) {
    if (!foundAnnotations.isEmpty()) {
      Annotation a = (Annotation) foundAnnotations.get(0);
      if (a.getClass() != nextFoundAnnotation.getClass()) {
        throw new IllegalStateException("Mixed cache annotations not supported. We are currently searching for '"+a.getClass().getSimpleName()+"' annoations but pm '"+currentPm.getPmRelativeName()+"' is annotated with '"+nextFoundAnnotation.getClass().getSimpleName()+"'. Please make sure that you either use the old cache cfg annotation or the new one, but not both!");        
      }
    }
  }
  
  private static Cache getCacheByAspect(PmCacheCfg2 cfg, Aspect aspect) {
    for (Cache cache : cfg.value()) {
      List<Aspect> aspectList = Arrays.asList(cache.aspect());
      if (aspectList.contains(aspect) || aspectList.contains(Aspect.ALL)) {
        return cache;
      }
    }
    
    return null;
  }
  
}

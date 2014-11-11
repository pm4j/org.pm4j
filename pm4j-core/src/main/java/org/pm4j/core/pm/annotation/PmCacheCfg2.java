package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.api.PmCacheApi.CacheKind;

/**
 * Annotation to define a cache configuration for cacheable PM properties.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmCacheCfg2 {
  
  /**
   * Defines a set of cache definitions for the cacheable properties of a
   * PM object.
   */
  Cache[] value() default {};
  
  /** Defines the cache configuration for a particular cacheable property of a PM object */
  public @interface Cache {
    
    /**
     * Defines the property this cache definition is for.
     */
    CacheKind[] property() default CacheKind.ALL; 
    
    /**
     * Defines the cache mode.
     */
    CacheMode mode() default CacheMode.ON;
   
    /**
     * A cache definition is by default only applied for the PM that you annotate.
     * <p>
     * In some cases it's useful to define a cache mode for a whole sub tree of PM's.
     * If you define {@link #cascade()} as <code>true</code> the cache definition
     * will also be applied for all child PM's.<br>
     * But: Each recursively defined parent cache definition can be overridden
     * by a cache definition on child level.
     *
     * @return <code>true</code> if the cache definition should also be applied on child PMs.
     */
    boolean cascade() default false;

    /** 
     * Configuration of clear behavior, when clear the method is called.
     * Default behavior is that the cache is cleared, but optionally it can be
     * configured, to be never cleared.
     */
    Clear clear() default Clear.DEFAULT;
    
    /**
     * Defines a set of PMs that will trigger a cache clear if it's value changes.
     */
    Observe[] clearOn() default {};
  }
  
  
  /** Used to define a set of PMs that will be observed for value changes. */
  public @interface Observe {
    
    /**
     * @return an array of path expressions pointing to PMs that will be observed
     *         for value changes.
     */
    String[] pm() default {};
    
    /**
     * @return <code>true</code> if child PMs shall be observer too,
     *         <code>false</code> otherwise
     */
    boolean observePmTree() default false;
  }

  public enum CacheMode {
    /** No caching. */
    OFF,
    /** The value will be cached locally within the PM instance. */
    ON,
    /**
     * The value will be cached for the life time of a request.
     * <p>
     * This option will currently only be considered in the JSF environment. In
     * other cases (rich client) it has the effect of the option {@link #OFF}.
     */
    REQUEST
  }
  
  /** Configuration values for behavior how to clear cache, when clear method is called. */
  public enum Clear {
    /** When clear method is called, cache is cleared. */
    DEFAULT,
    /** When clear method is called, cache is never cleared. */
    NEVER;
  }

}

package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cache definition annotation.
 * <p>
 * TODOC olaf:
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmCacheCfg2 {
  
  /**
   * TODO
   * @return
   */
  Cache[] value() default {};
  
  /**
   * @author sdolke
   *
   */
  public @interface Cache {
    
    /**
     * TODO
     * @return
     */
    Aspect[] aspect() default Aspect.ALL; 
    
    /**
     * TODO
     * @return
     */
    CacheMode mode() default CacheMode.ON;
   
    /**
     * A cache definition is by default only applied for the PM that you annotate.
     * <p>
     * In some cases it's useful to define a cache mode for a whole sub tree of PM's.
     * If you define {@link #cascade()} as <code>true</code> the cache definition
     * will also be applied for all child PM's.<br>
     * But: Each recursively defined parent cache aspect definition can be overridden
     * by a cache aspect definition on child level.
     *
     * @return <code>true</code> if the cache definition should also be applied on child PMs.
     */
    boolean cascade() default false;

    /** 
     * Configuration of clear behavior, when clear method is called.
     * Default behavior is that cache is cleared, but optionally it can be configured,
     * that cache is never cleared.
     */
    Clear clear() default Clear.DEFAULT;
    
    /**
     * TODO
     * @return
     */
    Observe[] clearOn() default {};
    
  }
  
  
  public @interface Observe {
    
    /**
     * TODO
     * @return
     */
    String[] pm() default {};
    
    /**
     * @return
     */
    boolean cascade() default false;
  }
  
  public enum Aspect {
    ALL,
    NONE,
    ENABLEMENT,
    OPTIONS,
    TITLE,
    VALUE,
    VISIBILITY
  }
  

  public enum CacheMode {
    /** Not specified. */
    NOT_SPECIFIED,
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

  
  // Name constants for attributes that are found by reflection:
  public static final String ATTR_VISIBILITY = "visibility";
  public static final String ATTR_ENABLEMENT = "enablement";
  public static final String ATTR_TITLE = "title";
  public static final String ATTR_VALUE = "value";
  public static final String ATTR_OPTIONS = "options";

}

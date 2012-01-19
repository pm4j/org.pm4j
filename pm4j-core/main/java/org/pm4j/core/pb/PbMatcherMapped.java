package org.pm4j.core.pb;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.pm4j.common.util.collection.MapUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * A set of {@link PresentationModel} to {@link PbMatcher} associations.
 *
 * @author olaf boede
 */
public class PbMatcherMapped extends PbMatcher {

  private Deque<PbMatcher> matcherSet = new LinkedList<PbMatcher>();
  private PbFactory<?> defaultFactory;

  /** Cache for the already found matches.  */
  private BinderCache binderCache = new NoBinderCache();

  /**
   * An optional reference to a parent that may be configured in case of a matcher cascade.
   */
  private PbMatcher parentMatcher;

  public PbMatcherMapped(PbMatcher... matchers) {
    addMatcher(matchers);
  }

  public static PbMatcherMapped makeBinderMap(PbMatcher... matchers) {
    return new PbMatcherMapped(matchers);
  }

  public static PbMatcherMapped makeCascadedBinderMap(PbMatcherMapped parentMap, PbMatcher... matchers) {
    PbMatcherMapped b = new PbMatcherMapped(matchers);
    b.setParentMatcher(parentMap);
    return b;
  }

  /**
   * @param parentMatcher An optional reference to a parent that may be configured in case of a matcher cascade.
   */
  public void setParentMatcher(PbMatcher parentMatcher) {
    this.parentMatcher = parentMatcher;
  }

  public PbFactory<?> getPbFactory(PmObject pm) {
    PbFactory<?> f = findPbFactory(pm);
    if (f == null) {
      if (defaultFactory == null) {
        throw new PmRuntimeException(pm, "No matching presentation binding factory found for PM and no default factory was found.");
      }
      f = defaultFactory;
    }
    return f;
  }

  public PbFactory<?> findPbFactory(PmObject pm) {
    if (pm == null) {
      return null;
    }

    String pmName = PmUtil.getAbsoluteName(pm);
    PbFactory<?> b = binderCache.find(pmName);

    // Check if the PM has a subclass of a mapped class or interface.
    if ((b == null) &&
        (! binderCache.isUnmatched(pmName))) {
      synchronized (this) {
        // synchronization double check:
        if (! binderCache.isUnmatched(pmName)) {
          for (PbMatcher m : matcherSet) {
            b = m.findPbFactory(pm);
            if (b != null) {
              binderCache.registerMatch(pmName, b);
              break;
            }
          }

          if (b == null) {
            binderCache.registerUnmatched(pmName);
          }
        }
        // synchronized double check looser: should find the binding mapped by
        // the winner...
        else {
          b = binderCache.find(pmName);
        }
      }
    }

    if (b == null && parentMatcher != null) {
      b = parentMatcher.findPbFactory(pm);
    }

    return b;
  }

  public PbMatcherMapped addMatcher(PbMatcher... matchers) {
    for (PbMatcher m : matchers) {
      matcherSet.addLast(m);
    }

    return this;
  }

  public PbMatcherMapped addMatcher(Class<? extends PmObject> pmClass, PbFactory<?> builder) {
    return addMatcher(new PbMatcherByClass(builder, pmClass));
  }

  /**
   * Adds a set of {@link PbMatcher} instances.
   * <p>
   * The parameter set consists of key-value pairs.
   * <p>
   * If the key is a {@link Class}, a {@link PbMatcherByClass} will be
   * added for the pair.<br>
   * If the key is a {@link String}, a {@link PbMatcherByName} will be
   * added for the pair.
   * <p>
   * The pair-value needs to be a {@link PbFactory}.
   *
   * @param keyToBinderArray
   *          The set of key-value pairs to add {@link PbMatcher}s for.
   */
  public PbMatcherMapped addKeyToViewMatches(Object... keyToBinderArray) {
    Map<Object, PbFactory<?>> map = MapUtil.makeLinkedMap(keyToBinderArray);
    for (Map.Entry<Object, PbFactory<?>> e : map.entrySet()) {
      Object key = e.getKey();
      if (key instanceof Class<?>) {
        addMatcher(new PbMatcherByClass((Class<?>)key, e.getValue()));
      }
      else
      if (key instanceof String) {
        addMatcher(new PbMatcherByName((String)key, e.getValue()));
      }
      else {
        throw new IllegalArgumentException("Invalid key argument: " + key);
      }
    }

    return this;
	}

	/**
	 * Interface for internal binder association cache stragegies.
	 * <p>
	 * These caches should prevent permanent iteration over all binder mappings
	 * for each new PM display operation.
	 */
  interface BinderCache {
    PbFactory<?> find(String name);
    boolean isUnmatched(String name);
    void registerMatch(String name, PbFactory<?> binder);
    void registerUnmatched(String name);
	}

	static class NoBinderCache implements BinderCache {
    @Override public PbFactory<?> find(String name) {
      return null;
    }
    @Override public boolean isUnmatched(String name) {
      return false;
    }
    @Override public void registerMatch(String name, PbFactory<?> binder) {}
    @Override public void registerUnmatched(String name) {}
	}

  /**
   * Caches the binder instances using the full name of the PM as key.
   * <p>
   * <b>TODO olaf: Does not yet work reliable:</b>
   * <ul>
   *   <li>It has to consider matcher instances that do not rely on fix metadata only.
   *       In this case the same PM may be associated with different binders, depending
   *       on runtime-data...<br>
   *       Such PMs have to be excluded from the cache....
   *       </li>
   *   <li>The cache has to ensure the the same evaluation order as the uncached implementation.
   *       To ensure this, the cache has to know about the evaluation order position of each
   *       mapping.</li>
   * </ul>
   */
	static class BinderCacheByName implements BinderCache {
    private Map<String, PbFactory<?>> pmNameToBinderMap = new HashMap<String, PbFactory<?>>();
    private Set<String> notMappedNames = new HashSet<String>();

    @Override public PbFactory<?> find(String name) {
      return pmNameToBinderMap.get(name);
    }

    @Override public boolean isUnmatched(String name) {
      return notMappedNames.contains(name);
    }

    @Override public void registerMatch(String name, PbFactory<?> binder) {
      pmNameToBinderMap.put(name, binder);
    }

    @Override public void registerUnmatched(String name) {
      notMappedNames.add(name);
    }
  }

  public void setDefaultFactory(PbFactory<?> defaultFactory) {
    this.defaultFactory = defaultFactory;
  }

}

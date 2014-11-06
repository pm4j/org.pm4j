package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi.PmMatcher;

/**
 * A builder for {@link PmMatcher} instances.
 * <p>
 * Is useful to support immutable matcher instances without having too much
 * matcher constructor variants.
 *
 * @author Olaf Boede
 **/
public class PmVisitorMatcherBuilder {
  private PmMatcher parentMatcher;
  private Class<?> pmClass;
  private String namePattern;

  /**
   * Adds a match condition:<br>
   * PMs that extend or implement the given class or interface do match.
   *
   * @param pmClass The class or interface condition.
   * @return The builder reference for fluent programming style support.
   */
  public PmVisitorMatcherBuilder pmClass(Class<?> pmClass) {
    this.pmClass = pmClass;
    return this;
  }

  /**
   * Adds a match condition:<br>
   * The result of {@link PmObject#getPmName()} should match the given pattern.
   *
   * @see {@link java.util.regex.Pattern}
   *
   * @param namePattern The name condition. May be the name string or a pattern like 'cmd.*'.
   * @return The builder reference for fluent programming style support.
   */
  public PmVisitorMatcherBuilder name(String namePattern) {
    this.namePattern = namePattern;
    return this;
  }

  /**
   * Adds a match condition:<br>
   * The PM should have a parent that matches given matcher logic.
   *
   * @param parentMatcher The parent PM condition.
   * @return The builder reference for fluent programming style support.
   */
  public PmVisitorMatcherBuilder parent(PmMatcher parentMatcher) {
    this.parentMatcher = parentMatcher;
    return this;
  }

  /**
   * Adds a match condition:<br>
   * The PM should have a parent that implement or extends the given interface or class.
   *
   * @param parentMatcher The parent type condition.
   * @return The builder reference for fluent programming style support.
   */
  public PmVisitorMatcherBuilder parent(Class<?> parentClass) {
    return parent(new PmClassMatcher(parentClass));
  }

  /**
   * Builds the matcher and clear this builder instance.
   * The builder is ready for building the next matcher.
   *
   * @return the matcher.
   */
  public PmMatcher build() {
    PmMatcherImpl m = new PmMatcherImpl(parentMatcher, pmClass, namePattern);
    clear();
    return m;
  }

  private void clear() {
    parentMatcher = null;
    pmClass = null;
    namePattern = null;
  }


  /**
   * A matcher implementation that may match by
   * <ul>
   *  <li>parent (using a parent matcher)</li>
   *  <li>the PM class (is-assignable check)</li>
   *  <li>a PM name pattern</li>
   * </ul>
   */
  class PmMatcherImpl implements PmMatcher {

    private PmMatcher parentMatcher;
    private Class<?> pmClass;
    private String pmNamePattern;

    /**
     * @param parentClass
     * @param pmNamePattern See {@link java.util.regex.Pattern}
     */
    public PmMatcherImpl(PmMatcher parentMatcher, Class<?> pmClass, String pmNamePattern) {
      this.parentMatcher = parentMatcher;
      this.pmClass = pmClass;
      this.pmNamePattern = pmNamePattern;
    }

    @Override
    public boolean doesMatch(PmObject pm) {
      return pm != null &&
             doesParentMatch(pm) &&
             doesPmClassMatch(pm) &&
             doesNameMatch(pm);
    }

    private boolean doesPmClassMatch(PmObject pm) {
      return pmClass == null ||
             pmClass.isAssignableFrom(pm.getClass());
    }

    private boolean doesNameMatch(PmObject pm) {
      return pmNamePattern == null ||
             pm.getPmName().matches(pmNamePattern);
    }

    private boolean doesParentMatch(PmObject pm) {
      return parentMatcher == null ||
             parentMatcher.doesMatch(pm.getPmParent());
    }


  }

  static class PmClassMatcher implements PmMatcher {
    private Class<?> pmClass;

    public PmClassMatcher(Class<?> pmClass) {
      assert pmClass != null;
      this.pmClass = pmClass;
    }

    @Override
    public boolean doesMatch(PmObject pm) {
      return pm != null &&
             pmClass.isAssignableFrom(pm.getClass());
    }
  }
}

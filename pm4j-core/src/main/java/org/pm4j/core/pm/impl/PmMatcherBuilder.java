package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmObject.PmMatcher;

/**
 * A builder for {@link PmMatcher} instances.
 * <p>
 * Creates {@link PmMatcher} instances without having too much matcher
 * constructor variants.
 *
 * @author Olaf Boede
 **/
public class PmMatcherBuilder {

  private PmMatcherImpl matcher = new PmMatcherImpl();

  /**
   * Adds a match condition:<br>
   * PMs that extend or implement the given class or interface do match.
   *
   * @param pmClass The class or interface condition.
   * @return The builder reference for fluent programming style support.
   */
  public PmMatcherBuilder pmClass(Class<?> pmClass) {
    matcher.classOrInstanceMatcher = new PmClassMatcher(pmClass);
    return this;
  }

  /**
   * Adds a match condition:<br>
   * PM should be one of the given <code>pms</code>.
   *
   * @param pm The PMs the match.
   * @return The builder reference for fluent programming style support.
   */
  public PmMatcherBuilder pm(PmObject... pms) {
    matcher.classOrInstanceMatcher = (pms.length == 1)
        ? new PmInstanceMatcher(pms[0])
        : new PmSetMatcher(pms);
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
  public PmMatcherBuilder name(String namePattern) {
    matcher.pmNamePattern = namePattern;
    return this;
  }

  /**
   * Adds a match condition:<br>
   * The result of {@link PmObject#isPmEnabled()} should match the given enabled state.
   *
   * @param matchingState The enabled state that matches.
   * @return The builder reference for fluent programming style support.
   */
  public PmMatcherBuilder enabled(boolean matchingState) {
    matcher.enabled = matchingState;
    return this;
  }

  /**
   * Adds a match condition:<br>
   * The result of {@link PmObject#isPmVisible()} should match the given visible state.
   *
   * @param matchingState The visible state that matches.
   * @return The builder reference for fluent programming style support.
   */
  public PmMatcherBuilder visible(boolean matchingState) {
    matcher.visible = matchingState;
    return this;
  }



  /**
   * Adds a match condition:<br>
   * The PM should have a parent that matches given matcher logic.
   *
   * @param parentMatcher The parent PM condition.
   * @return The builder reference for fluent programming style support.
   */
  public PmMatcherBuilder parent(PmMatcher parentMatcher) {
    matcher.parentMatcher = parentMatcher;
    return this;
  }

  /**
   * Adds a match condition:<br>
   * The PM should have a parent that implement or extends the given interface or class.
   *
   * @param parentClass The parent type condition.
   * @return The builder reference for fluent programming style support.
   */
  public PmMatcherBuilder parent(Class<?> parentClass) {
    return parent(new PmClassMatcher(parentClass));
  }

  /**
   * Adds an additional matcher to consider when this matcher gets evaluated.
   *
   * @param subMatcher The matcher
   * @return The builder reference for fluent programming style support.
   */
  public PmMatcherBuilder matcher(PmMatcher subMatcher) {
    if (matcher.subMatcher.isEmpty()) {
      matcher.subMatcher = new ArrayList<PmObject.PmMatcher>();
    }
    matcher.subMatcher.add(subMatcher);
    return this;
  }

  /**
   * Defines that the match criteria should be logically inverted.
   *
   * @return The builder reference for fluent programming style support.
   */
  public PmMatcherBuilder invert() {
    matcher.invert = true;
    return this;
  }

  /**
   * Builds the matcher and clear this builder instance.
   * The builder is ready for building the next matcher.
   *
   * @return the matcher.
   */
  public PmMatcher build() {
    PmMatcherImpl m = matcher;
    matcher = new PmMatcherImpl();
    return m;
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
    private PmMatcher classOrInstanceMatcher;
    private String pmNamePattern;
    private Boolean enabled;
    private Boolean visible;
    private List<PmMatcher> subMatcher = Collections.emptyList();
    private boolean invert = false;


    @Override
    public boolean doesMatch(PmObject pm) {
      return doesMatchNotInverted(pm) ^ invert;
    }

    private boolean doesMatchNotInverted(PmObject pm) {
      if (pm == null ||
          !doesParentMatch(pm) ||
          !doesPmClassMatch(pm) ||
          !doesNameMatch(pm) ||
          (visible != null && pm.isPmVisible() != visible) ||
          (enabled != null && pm.isPmEnabled() != enabled)) {
        return false;
      }

      for (PmMatcher m : subMatcher) {
        if (!m.doesMatch(pm)) {
          return false;
        }
      }

      return true;
    }

    private boolean doesPmClassMatch(PmObject pm) {
      return classOrInstanceMatcher == null ||
             classOrInstanceMatcher.doesMatch(pm);
    }

    private boolean doesNameMatch(PmObject pm) {
      return pmNamePattern == null ||
             pm.getPmName().matches(pmNamePattern);
    }

    private boolean doesParentMatch(PmObject pm) {
      return parentMatcher == null ||
             parentMatcher.doesMatch(pm.getPmParent());
    }

    @Override
    public String toString() {
      List<String> parts = new ArrayList<String>();
      if (parentMatcher != null) {
        parts.add("parent(" + parentMatcher + ")");
      }
      if (classOrInstanceMatcher != null) {
        parts.add(classOrInstanceMatcher.toString());
      }
      if (pmNamePattern != null) {
        parts.add("name=" + pmNamePattern);
      }
      if (enabled != null) {
        parts.add("enabled=" + enabled);
      }
      if (visible != null) {
        parts.add("visible=" + visible);
      }
      if (!subMatcher.isEmpty()) {
        parts.add(subMatcher.toString());
      }
      return StringUtils.join(parts, "&&");
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

    @Override
    public String toString() {
      return pmClass.getSimpleName();
    }
  }

  static class PmInstanceMatcher implements PmMatcher {
    private final PmObject pm;

    public PmInstanceMatcher(PmObject pm) {
      this.pm = pm;
    }

    @Override
    public boolean doesMatch(PmObject pm) {
      return this.pm == pm;
    }

    @Override
    public String toString() {
      return "pm=" + pm.getPmName();
    }
  }

  static class PmSetMatcher implements PmMatcher {
    private final PmObject[] pms;

    public PmSetMatcher(PmObject... pms) {
      this.pms = pms;
    }

    @Override
    public boolean doesMatch(PmObject pm) {
      for (PmObject p : pms) {
        if (p == pm) {
          return true;
        }
      }
      return false;
    }

    @Override
    public String toString() {
      ArrayList<String> names = new ArrayList<String>(pms.length);
      for (PmObject p : pms) {
        names.add(p.getPmName());
      }
      return "pms=" + names.toString();
    }
  }
}

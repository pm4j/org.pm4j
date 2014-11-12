package org.pm4j.core.xml.visibleState;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi.PmMatcher;

/**
 * Checks for defined PMs, whether an aspect is part of a defined property set.
 *
 * @author Olaf Boede
 */
public class VisibleStateAspectMatcher {

  private final PmMatcher pmMatcher;
  private final Set<VisibleStateAspect> properties;

  /**
   * Constructor for a matcher that will be applied for all PMs.
   *
   * @param properties
   *          The set of properties that match.
   */
  public VisibleStateAspectMatcher(VisibleStateAspect... properties) {
    this(null, properties);
  }


  private static final PmMatcher ALWAYS_TRUE_PM_MATCHER = new PmMatcher() {
    @Override
    public boolean doesMatch(PmObject pm) {
      return true;
    }
  };

  /**
   * @param pmMatcher
   *          The PM matcher. Only for PMs the match, the property condition can
   *          match (and-combined logic).<br>
   *          If it is <code>null</code> all PMs will match.
   * @param properties
   *          The set of properties that match.
   */
  public VisibleStateAspectMatcher(PmMatcher pmMatcher, VisibleStateAspect... properties) {
    this.pmMatcher = pmMatcher != null
        ? pmMatcher
        : ALWAYS_TRUE_PM_MATCHER;
    this.properties = new HashSet<VisibleStateAspect>(Arrays.asList(properties));
  }

  /**
   * Returns <code>true</code> if the {@link PmMatcher} matches and the
   * property if part of the defined property set.
   *
   * @param pm
   *          The PM that the property condition should be checked for.
   * @param property
   *          The property to check.
   * @return <code>true</code> if the PM matcher matches and the property is
   *         part of the defined property set.
   */
  public boolean doesMatch(PmObject pm, VisibleStateAspect property) {
    if (pmMatcher != null && !pmMatcher.doesMatch(pm)) {
      return false;
    }
    return properties.contains(property);
  }

  /**
   * @return the PM matcher. Never <code>null</code>.
   */
  public PmMatcher getPmMatcher() {
    return pmMatcher;
  }

  /**
   * @return the set of matching properties. Never <code>null</code>.
   */
  public Set<VisibleStateAspect> getProperties() {
    return properties;
  }

}

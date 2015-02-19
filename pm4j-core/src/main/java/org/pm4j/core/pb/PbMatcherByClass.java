package org.pm4j.core.pb;

import org.pm4j.core.pm.PmObject;

public class PbMatcherByClass extends PbMatcher {

  private Class<?>[] pmClasses;
  private PbFactory<?> builder;

  public PbMatcherByClass(Class<?> pmClass, PbFactory<?> builder) {
    assert builder != null;
    this.builder = builder;
    this.pmClasses = new Class<?>[1];
    pmClasses[0] = pmClass;
  }

  public PbMatcherByClass(PbFactory<?> builder, Class<?>... pmClasses) {
    assert builder != null;
    this.builder = builder;
    this.pmClasses = pmClasses;
  }

  @Override
  public PbFactory<?> findPbFactory(PmObject pm) {
    for (Class<?> c : pmClasses) {
      if (c.isAssignableFrom(pm.getClass())) {
        return builder;
      }
    }
    // no match
    return null;
  }

}

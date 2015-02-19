package org.pm4j.core.pb;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * Uses {@link PmUtil#getAbsoluteName(PmObject)} to identify the match.
 *
 * @author olaf boede
 */
public class PbMatcherByName extends PbMatcher {

  private Set<String> nameSet;
  private PbFactory<?> builder;

  public PbMatcherByName(String name, PbFactory<?> builder) {
    assert builder != null;
    this.builder = builder;
    this.nameSet = new HashSet<String>(Arrays.asList(name));
  }

  public PbMatcherByName(PbFactory<?> builder, String... names) {
    assert builder != null;
    this.builder = builder;
    this.nameSet = new HashSet<String>(Arrays.asList(names));
  }

  @Override
  public PbFactory<?> findPbFactory(PmObject pm) {
    return nameSet.contains(PmUtil.getAbsoluteName(pm))
            ? builder
            : null;
  }

}

package org.pm4j.core.pm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Enumerates PM content aspects.
 * <p>
 * Is used for controlling cache configuration and remote communication for
 * server based rich client applications.
 *
 * @author olaf boede
 */
@Deprecated
public enum DeprPmAspect {
  ENABLEMENT,
  OPTIONS,
  TITLE,
  TOOLTIP,
  VALUE,
  VISIBILITY,
  ALL;

  public static final Set<DeprPmAspect> ALL_SET = Collections.unmodifiableSet(
      new HashSet<DeprPmAspect>(Arrays.asList(
          ENABLEMENT,
          OPTIONS,
          TITLE,
          TOOLTIP,
          VALUE,
          VISIBILITY,
          ENABLEMENT,
          OPTIONS
          )));


}
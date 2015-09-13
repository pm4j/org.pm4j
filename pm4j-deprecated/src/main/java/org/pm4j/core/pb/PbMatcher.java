package org.pm4j.core.pb;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;

/**
 * Finds a matching {@link PbFactory} for a given PM.
 * 
 * @author olaf boede
 */
public abstract class PbMatcher {

  /**
   * Finds the {@link PbFactory} mapped for the given PM.
   *
   * @param pm The PM to get a {@link PbFactory} for.
   * @return The matching {@link PbFactory}. <code>null</code> if there is no match.
   */
  public abstract PbFactory<?> findPbFactory(PmObject pm);

  /**
   * Imperative version of {@link #findPbFactory(PmObject)}.
   *
   * @param pm The PM to get a {@link PbFactory} for.
   * @return The matching {@link PbFactory}. 
   * @throws PmRuntimeException if there is no match.
   */
  public PbFactory<?> getPbFactory(PmObject pm) {
    PbFactory<?> f = findPbFactory(pm);
    if (f == null) {
      throw new PmRuntimeException(pm, "No matching presentation binding factory found for PM.");
    }
    return f;
  }

}

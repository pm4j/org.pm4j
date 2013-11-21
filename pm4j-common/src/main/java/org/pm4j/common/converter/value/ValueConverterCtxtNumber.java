package org.pm4j.common.converter.value;

import java.math.RoundingMode;

/**
 * Provides additional context information for numeric converter tasks.
 *
 * @author Olaf Boede
 */
public interface ValueConverterCtxtNumber extends ValueConverterCtxt {

  /**
   * @return The {@link RoundingMode} to use.
   */
  RoundingMode getConverterCtxtRoundingMode();
}

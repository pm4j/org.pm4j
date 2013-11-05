package org.pm4j.core.pm.impl.converter;

import java.util.TimeZone;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmWithTimeZone;

/**
 * Convenience base class for {@link PmAttr#ValueConverter}'s that need to consider time zone information.
 *
 * @param <E> The external attribute value type.
 * @param <I> The internal attribute value type.
 *
 * @author oboede
 */
public abstract class ValueConverterWithTimeZoneBase<E, I> implements PmAttr.ValueConverter<E, I> {

  /**
   * Retrieves the {@link TimeZone} from the attributes that implement the {@link PmWithTimeZone}
   * interface. In all other cases it gets it from the related {@link PmConversation}.
   *
   * @param pmAttr The attribute PM the converter is used for.
   * @return The {@link TimeZone}. Never <code>null</code>.
   */
  protected TimeZone getPmTimeZone(PmAttr<?> pmAttr) {
    TimeZone tz = (pmAttr instanceof PmWithTimeZone)
        ? ((PmWithTimeZone) pmAttr).getPmTimeZone()
        : pmAttr.getPmConversation().getPmTimeZone();

    if (tz == null) {
      throw new PmRuntimeException(pmAttr, "Time zone information should not be null.");
    }
    return tz;
  }

}

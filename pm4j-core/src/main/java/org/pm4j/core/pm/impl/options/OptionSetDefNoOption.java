package org.pm4j.core.pm.impl.options;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmOptionSet;

/**
 * Default option set definition. Provides no options.
 *
 * @author olaf boede
 */
public class OptionSetDefNoOption implements PmOptionSetDef<PmAttr<?>> {

  /** An instance that may be used as singleton. */
  public static final PmOptionSetDef<PmAttr<?>> INSTANCE = new OptionSetDefNoOption();

  /** The empty instance, provided for all attributes without options. */
  private static final PmOptionSet EMPTY_OPTION_SET = new PmOptionSetImpl();

  public PmOptionSet makeOptions(PmAttr<?> forAttr) {
    return EMPTY_OPTION_SET;
  }

  @Override
  public String getNullOptionTitle(PmAttr<?> forAttr) {
    return "";
  }

}

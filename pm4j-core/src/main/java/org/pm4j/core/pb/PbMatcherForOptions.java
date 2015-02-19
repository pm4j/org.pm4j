package org.pm4j.core.pb;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmOptionSet;

public class PbMatcherForOptions extends PbMatcher {

  private PbFactory<?> optionSetAttrBinder;

  public PbMatcherForOptions(PbFactory<?> optionSetAttrBinder) {
    this.optionSetAttrBinder = optionSetAttrBinder;
  }

  @Override
  public PbFactory<?> findPbFactory(PmObject pm) {
    if (pm instanceof PmAttr) {
      PmOptionSet os = ((PmAttr<?>)pm).getOptionSet();
      return os.getOptions().size() > 0
        ? optionSetAttrBinder
        : null;
    }

    return null;
  }

}

package org.pm4j.common.query;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

public final class FilterUtil {

  public static FilterCompareDefinition findFilterCompareDefinitionByPathName(Collection<FilterCompareDefinition> fcds, String name) {
    for (FilterCompareDefinition fcd : fcds) {
      if (StringUtils.equals(fcd.getAttr().getPathName(), name)) {
        return fcd;
      }
    }
    // not found
    return null;
  }

}

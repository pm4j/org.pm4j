package org.pm4j.core.pm.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.annotation.PmTitleCfg;

class InternalPmTitleCfgUtil {
  
  static String getResKeyBase(List<PmTitleCfg> annotations, String defaultValue) {    
    for (PmTitleCfg annotation : annotations) {
      if (StringUtils.isNotBlank(annotation.resKeyBase())) {
          return annotation.resKeyBase();
        }
    }
    
    return defaultValue;
  }
}

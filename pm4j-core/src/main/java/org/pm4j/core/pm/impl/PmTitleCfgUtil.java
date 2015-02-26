package org.pm4j.core.pm.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.annotation.PmTitleCfg;

class PmTitleCfgUtil {

  enum PmTitleCfgParameter {
    // TODO: For GLOBE00145358
    // RES_KEY, RES_KEY_BASE
    RES_KEY_BASE
  };

  @SuppressWarnings("unchecked")
  static <T> T getPmTitleCfgValue(List<PmTitleCfg> annotations, PmTitleCfgParameter parameter, T defaultValue) {

    for (PmTitleCfg annotation : annotations) {
      switch (parameter) {
      case RES_KEY_BASE:
        if (StringUtils.isNotBlank(annotation.resKeyBase())) {
          return (T) annotation.resKeyBase();
        }

        // TODO: For GLOBE00145358
        // case RES_KEY:
        // if(StringUtils.isNotBlank(annotation.resKey())) {
        // return (T) annotation.resKey();
        // }

      default:
      }
    }

    return defaultValue;
  }
}

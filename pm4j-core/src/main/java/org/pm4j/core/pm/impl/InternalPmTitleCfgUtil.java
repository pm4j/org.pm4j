package org.pm4j.core.pm.impl;

//import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.annotation.TooltipUsesTitleEnum;
import org.pm4j.core.pm.impl.title.PmTitleProvider;

class InternalPmTitleCfgUtil {

  static String getAttrValue(List<PmTitleCfg> annotations, String defaultValue) {
    for (PmTitleCfg annotation : annotations) {
      if (StringUtils.isNotBlank(annotation.attrValue())) {
        return annotation.attrValue();
      }
    }

    return defaultValue;
  }

  static String getIcon(List<PmTitleCfg> annotations, String defaultValue) {
    for (PmTitleCfg annotation : annotations) {
      if (StringUtils.isNotBlank(annotation.icon())) {
        return annotation.icon();
      }
    }

    return defaultValue;
  }

  static String getResKey(List<PmTitleCfg> annotations, String defaultValue) {
    for (PmTitleCfg annotation : annotations) {
      if (StringUtils.isNotBlank(annotation.resKey())) {
        return annotation.resKey();
      }
    }

    return defaultValue;
  }

  static String getResKeyBase(List<PmTitleCfg> annotations, String defaultValue) {
    for (PmTitleCfg annotation : annotations) {
      if (StringUtils.isNotBlank(annotation.resKeyBase())) {
        return annotation.resKeyBase();
      }
    }

    return defaultValue;
  }

  static String getTitle(List<PmTitleCfg> annotations, String defaultValue) {
    for (PmTitleCfg annotation : annotations) {
      if (StringUtils.isNotBlank(annotation.title())) {
        return annotation.title();
      }
    }

    return defaultValue;
  }

  static String getTooltip(List<PmTitleCfg> annotations, String defaultValue) {
    for (PmTitleCfg annotation : annotations) {
      if (StringUtils.isNotBlank(annotation.tooltip())) {
        return annotation.tooltip();
      }
    }

    return defaultValue;
  }

  static TooltipUsesTitleEnum getTooltipUsesTitle(List<PmTitleCfg> annotations, TooltipUsesTitleEnum defaultValue) {
    for (PmTitleCfg annotation : annotations) {
      if (annotation.tooltipUsesTitle() != TooltipUsesTitleEnum.UNKNOWN) {
        return annotation.tooltipUsesTitle();
      }
    }

    return defaultValue;
  }
}

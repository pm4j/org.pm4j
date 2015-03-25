package org.pm4j.core.pm.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmTitleCfg;

/**
 * Internal helper class for handling of {@link PmTitleCfg} annotations.
 *
 * @author mheller
 */
class InternalPmTitleCfgUtil {

  /**
   * @param annotations
   *          A set of annotations to read {@link PmTitleCfg#icon()} from.<br>
   *          The read operation starts with the first list element.
   * @return The value of the fist found value or <code>null</code> if all values where blank.
   */
  static String readIcon(List<PmTitleCfg> annotations) {
    for (PmTitleCfg annotation : annotations) {
      if (StringUtils.isNotBlank(annotation.icon())) {
        return annotation.icon();
      }
    }
    return null;
  }

  /**
   * @param annotations
   *          A set of annotations to read {@link PmTitleCfg#resKey()} from.<br>
   *          The read operation starts with the first list element.
   * @return The value of the fist found value or <code>null</code> if all values where blank.
   */
  static String readResKey(List<PmTitleCfg> annotations) {
    for (PmTitleCfg annotation : annotations) {
      if (StringUtils.isNotBlank(annotation.resKey())) {
        return annotation.resKey();
      }
    }
    return null;
  }

  /**
   * @param annotations
   *          A set of annotations to read {@link PmTitleCfg#resKeyBase()} from.<br>
   *          The read operation starts with the first list element.
   * @return The value of the fist found value or <code>null</code> if all values where blank.
   */
  static String readResKeyBase(List<PmTitleCfg> annotations) {
    for (PmTitleCfg annotation : annotations) {
      if (StringUtils.isNotBlank(annotation.resKeyBase())) {
        return annotation.resKeyBase();
      }
    }
    return null;
  }

  /**
   * @param annotations
   *          A set of annotations to read {@link PmTitleCfg#title()} from.<br>
   *          The read operation starts with the first list element.
   * @return The value of the fist found value or <code>null</code> if all values where blank.
   */
  static String getTitle(List<PmTitleCfg> annotations) {
    for (PmTitleCfg annotation : annotations) {
      if (StringUtils.isNotBlank(annotation.title())) {
        return annotation.title();
      }
    }
    return null;
  }

  /**
   * @param annotations
   *          A set of annotations to read {@link PmTitleCfg#tooltip()} from.<br>
   *          The read operation starts with the first list element.
   * @return The value of the fist found value or <code>null</code> if all values where blank.
   */
  static String getTooltip(List<PmTitleCfg> annotations) {
    for (PmTitleCfg annotation : annotations) {
      if (StringUtils.isNotBlank(annotation.tooltip())) {
        return annotation.tooltip();
      }
    }
    return null;
  }

  /**
   * @param annotations
   *          A set of annotations to read {@link PmTitleCfg#tooltipUsesTitle()} from.<br>
   *          The read operation starts with the first list element.
   * @return The value of the fist found value or {@link PmBoolean#UNDEFINED} if all values where {@link PmBoolean#UNDEFINED}.
   */
  static PmBoolean getTooltipUsesTitle(List<PmTitleCfg> annotations) {
    for (PmTitleCfg annotation : annotations) {
      if (annotation.tooltipUsesTitle() != PmBoolean.UNDEFINED) {
        return annotation.tooltipUsesTitle();
      }
    }
    return PmBoolean.UNDEFINED;
  }
}

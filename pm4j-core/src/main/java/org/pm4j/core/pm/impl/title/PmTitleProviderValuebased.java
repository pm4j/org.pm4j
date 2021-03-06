package org.pm4j.core.pm.impl.title;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.PmObject;

/**
 * Provides title values that are defined in Java code.
 * <p>
 * Useful for single language and prototype applications.
 * <p>
 * But in parallel it also allows to find resource based strings using the findLocalization methods.
 *
 * @author Olaf Boede
 */
public class PmTitleProviderValuebased implements PmTitleProvider<PmObject> {

  private String title;
  private String shortTitle;
  private String tooltip;
  private String icon;

  public PmTitleProviderValuebased(String title, String shortTitle, String tooltip, String icon) {
    this.title = StringUtils.isBlank(title) ? null : title;
    this.shortTitle = StringUtils.isBlank(shortTitle) ? null : shortTitle;
    this.tooltip = StringUtils.isBlank(tooltip) ? null : tooltip;
    this.icon = StringUtils.isBlank(icon) ? null : icon;
  }

  @Override
  public String getIconPath(PmObject item) {
    return icon;
  }

  @Override
  public String getShortTitle(PmObject item) {
    return shortTitle;
  }

  @Override
  public String getTitle(PmObject item) {
    return title;
  }

  @Override
  public String getToolTip(PmObject item) {
    return tooltip;
  }
}

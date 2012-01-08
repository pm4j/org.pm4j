package org.pm4j.core.pm.impl.title;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.util.resource.ClassPathResourceStringUtil;
import org.pm4j.core.pm.PmObject;

/**
 * Provides title values that are defined in Java code.
 * <p>
 * Useful for single language and prototype applications.
 *
 * @author olaf boede
 */
public class PmTitleProviderValuebased implements PmTitleProvider<PmObject> {

  private String title;
  private String tooltip;
  private String icon;

  public PmTitleProviderValuebased(String value, String tooltip, String icon) {
    this.title = StringUtils.isBlank(value) ? null : value;
    this.tooltip = StringUtils.isBlank(tooltip) ? null : tooltip;
    this.icon = StringUtils.isBlank(icon) ? null : icon;
  }

  @Override
  public boolean canSetTitle(PmObject item) {
    return true;
  }

  @Override
  public String findLocalization(PmObject item, String key, Object... resStringArgs) {
    return ClassPathResourceStringUtil.findString(item.getPmConversation().getPmLocale(), item.getClass(), key, resStringArgs);
  }

  @Override
  public String findLocalization(PmObject item, Locale locale, String key, Object... resStringArgs) {
    return ClassPathResourceStringUtil.findString(locale, item.getClass(), key, resStringArgs);
  }

  @Override
  public String getIconPath(PmObject item) {
    return icon;
  }

  @Override
  public String getLocalization(PmObject item, String key, Object... resStringArgs) {
    return ClassPathResourceStringUtil.getString(item.getPmConversation().getPmLocale(), item.getClass(), key, resStringArgs);
  }

  @Override
  public String getShortTitle(PmObject item) {
    return getTitle(item);
  }

  @Override
  public String getTitle(PmObject item) {
    return title;
  }

  @Override
  public String getToolTip(PmObject item) {
    return tooltip;
  }

  @Override
  public void setTitle(PmObject item, String titleString) {
    this.title = titleString;
  }

}

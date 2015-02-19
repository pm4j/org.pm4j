package org.pm4j.navi;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a navigation rule string. <br>
 * Web frameworks often use string identifier based navigation rules.
 *
 * @author olaf boede
 *
 */
public class NaviRuleLink implements NaviLink {

  private String ruleString;

  public NaviRuleLink(String ruleString) {
    this.ruleString = ruleString;
  }

  @Override
  public String getPath() {
    return ruleString;
  }

  @Override
  public String getPosOnPage() {
    return null;
  }
  
  /**
   * Navigation rule links usually may only refer to internal pages.
   */
  @Override
  public boolean isExternalLink() {
    return false;
  }

  @Override
  public boolean isLinkToSamePage(NaviLink other) {
    return (other instanceof NaviRuleLink) &&
           StringUtils.equals(ruleString, ((NaviRuleLink)other).ruleString);
  }

  @Override
  public boolean isLinkToSamePagePos(NaviLink other) {
    return isLinkToSamePage(other);
  }

  @Override
  public String toString() {
    return ruleString;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof NaviRuleLink) &&
           isLinkToSamePage((NaviRuleLink)obj);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(3, 13).append(ruleString).toHashCode();
  }

  @Override
  public Map<String, Object> getParams() {
    // no parameters for NaviRuleLinks needed.
    return Collections.emptyMap();
  }
}

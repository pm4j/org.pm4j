package org.pm4j.navi.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pm4j.core.pm.impl.commands.PmCommandNaviBack;
import org.pm4j.navi.NaviLink;

public class NaviLinkImpl implements NaviLink, Cloneable {

  /** A navigation path that points to a page. */
  private String path;

  /** Properties that will be applied to the resulting history item. */
  private Map<String, Object> params = Collections.emptyMap();

  /** Optional position on the addressed page. */
  private String posOnPage = null;

  /** Defines if this link points to an external page. */
  private boolean externalLink = false;

  /**
   * Properties that will be transferred to the history navigation scope.
   * <p>
   * They do not have any impact on the link identity:
   * The method {@link #isLinkToSamePage(NaviLink)} for links to the same
   * page will always return <code>true</code> even if their
   * naviScopeParams are different.
   */
  private Map<String, Object> naviScopeParams = Collections.emptyMap();

  /**
   * @param path The link target path.
   */
  public NaviLinkImpl(String path) {
    this(path, null);
  }

  /**
   * @param path The link target path.
   * @param posOnPage Position on the addressed page.
   */
  public NaviLinkImpl(String path, String posOnPage) {
    this.path = path;
    this.posOnPage = posOnPage;
  }

  /**
   * Convenience constructor that adds a PM parameter to the link.
   *
   * @param path The link target.
   * @param pName The property name to add.
   * @param pValue The property value.
   */
  public NaviLinkImpl(String path, String pName, Object pValue) {
    this(path, null);
    addParam(pName, pValue);
  }

  /**
   * Creates a new link that points to the same page with the same parameter
   * set. It has only a changed {@link #posOnPage} attribute.
   *
   * @param ori
   *          The original link.
   * @param newPos
   *          The position of the new link.
   */
  @SuppressWarnings("unchecked")
  public NaviLinkImpl(NaviLinkImpl ori, String newPos) {
    this.path = ori.path;
    this.params = ori.params.size() > 0
                        ? new HashMap<String, Object>(ori.params)
                        : Collections.EMPTY_MAP;
    this.posOnPage = newPos;
    this.naviScopeParams = ori.naviScopeParams.size() > 0
                        ? new HashMap<String, Object>(ori.naviScopeParams)
                        : Collections.EMPTY_MAP;
  }

  /**
   * Creates a navigation link with a back navigation position parameter.
   *
   * @param path
   *          Path of the page to go to.
   * @param backNaviPos
   *          The position on the current page to return to in case of
   *          back-navigation.
   * @return The generated link.
   */
  public static NaviLinkImpl makeBackNaviLink(String path, String backNaviPos) {
    return new NaviLinkImpl(path, BACK_POS_PARAM, backNaviPos);
  }

  /**
   * Creates a navigation link which transfers a navigation scoped object.
   *
   * @param path
   *          The navigation path to generate the link for.
   * @param pName
   *          Key for the navigation scoped object.
   * @param pValue
   *          The new navigation scoped object.
   * @return The generated link.
   */
  public static NaviLinkImpl makeNaviScopeParamLink(String path, String pName, Object pValue) {
    NaviLinkImpl l = new NaviLinkImpl(path);
    l.addNaviScopeParam(pName, pValue);
    return l;
  }

  // TODO olaf: find some good factory signatures.
  /**
   * Creates a navigation link which transfers a navigation scoped objects.
   *
   * @param path
   *          The navigation path to generate the link for.
   * @param keyValueSet
   *          The navigation scoped objects to pass, organized in pairs. The
   *          first item should be the key of the first item. The second one,
   *          the value of the first item. After that other items might be
   *          passed in the same manner.
   *
   * @return The generated link.
   */
//  public static NaviLinkImpl makeNaviScopeParamsLink(String path, Object... keyValueSet) {
//    NaviLinkImpl l = new NaviLinkImpl(path);
//    if (keyValueSet.length > 0) {
//      l.params = MapUtil.makeHashMap(keyValueSet);
//    }
//    return l;
//  }

  public static NaviLinkImpl makeNaviScopeParamLink(NaviLink oriLink, String pName, Object pValue) {
    NaviLinkImpl l = new NaviLinkImpl(oriLink.getPath(), oriLink.getPosOnPage());
    l.addNaviScopeParam(pName, pValue);
    return l;
  }
//
//  public static NaviLinkImpl makeNaviScopeParamsLink(NaviLink oriLink, Object... keyValueSet) {
//    NaviLinkImpl l = new NaviLinkImpl(oriLink.getPath(), oriLink.getPosOnPage());
//    if (keyValueSet.length > 0) {
//      l.params = MapUtil.makeHashMap(keyValueSet);
//    }
//    return l;
//  }

  @Override
  public NaviLinkImpl clone() {
    try {
      NaviLinkImpl clone = (NaviLinkImpl) super.clone();
      // TODO: check if that is really necessary...
      if (params.size() > 0) {
        clone.params = new HashMap<String, Object>(params);
      }
      if (naviScopeParams.size() > 0) {
        clone.naviScopeParams = new HashMap<String, Object>(naviScopeParams);
      }
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new NaviRuntimeException(e);
    }
  }

  /**
   * In case of http applications it returns the URL of the target page (without
   * url parameters).
   *
   * @return The external parge name.
   */
  @Override
  public String getPath() {
    return path;
  }

  @Override
  public Map<String, Object> getParams() {
    return params;
  }

  @Override
  public String getPosOnPage() {
    return posOnPage;
  }

  /**
   * Defines a position on the page to navigate to.
   *
   * @param newPos The position. <code>null</code> specifies the default position (top of the page).
   */
  public void setPosOnPage(String newPos) {
    this.posOnPage = newPos;
  }

  @Override
  public boolean isExternalLink() {
    return externalLink;
  }

  /**
   * @param externalLink Defines if this link points to an external page.
   */
  public void setExternalLink(boolean externalLink) {
    this.externalLink = externalLink;
  }

  /**
   * Returns <code>true</code> if all navigation relevant information of both
   * links are equal.<br>
   * Navigation relevant attributes:
   * <ul>
   * <li>{@link #getPath()}</li>
   * <li>{@link #getParams()}</li>
   * <li>{@link #getPosOnPage()}</li>
   * </ul>
   * All other attributes, such as version and last visit time are not
   * considered here.
   *
   * @return <code>true</code> if all navigation relevant information of both
   *         links are equal.
   */
  @Override
  public boolean isLinkToSamePagePos(NaviLink other) {
    return StringUtils.equals(this.posOnPage, other.getPosOnPage()) &&
    isLinkToSamePage(other);
  }

  /**
   * Returns <code>true</code> if all navigation page relevant information of
   * both links are equal.<br>
   * Navigation relevant attributes:
   * <ul>
   * <li>{@link #getPath()}</li>
   * <li>{@link #getParams()}</li>
   * </ul>
   * All other attributes, such as pos-on-page, version and last visit time are
   * not considered here.
   *
   * @return <code>true</code> if all navigation page relevant information of
   *         both links are equal.
   */
  @Override
  public boolean isLinkToSamePage(NaviLink other) {
    if (this == other)
      return true;

    if (!StringUtils.equals(this.path, other.getPath()))
      return false;

    if (other instanceof NaviLinkImpl) {
      NaviLinkImpl otherPmLink = (NaviLinkImpl)other;

      if (this.params.size() != otherPmLink.getParams().size())
        return false;

      for (Map.Entry<String, Object> e : otherPmLink.getParams().entrySet()) {
        Object ownValue = this.params.get(e.getKey());
        Object otherValue = e.getValue();
        if (!ObjectUtils.equals(ownValue, otherValue)) {
          return false;
        }
      }
    }

    // all checks successfully passed
    return true;
  }

  public void addParam(String pName, Object pValue) {
    if (params.size() == 0) {
      params = new HashMap<String, Object>();
    }
    params.put(pName, pValue);
  }

  /**
   * Deprecated, please use addParam.
   * @param attrName
   * @param pValue
   */
  @Deprecated
  public void addAttrValueParamIfNotNull(String attrName, Object pValue) {
    if (pValue != null) {
      addAttrValueParam(attrName, pValue);
    }
  }

  /**
   * Deprecated, please use addParam.
   * @param attrName
   * @param pValue
   * @param compareVal
   */
  @Deprecated
  public void addAttrValueParamIfNotEq(String attrName, Object pValue, Object compareVal) {
    if (pValue != null && (! ObjectUtils.equals(pValue, compareVal)) ) {
      addAttrValueParam(attrName, pValue);
    }
  }

  /**
   * Deprecated, please use addParam.
   * @param attrName
   * @param pValue
   */
  @Deprecated
  public void addAttrValueParam(String attrName, Object pValue) {
    addParam(attrName, pValue);
  }

  /**
   * Adds a parameter that may be used for back navigation.
   * <p>
   * @see PmCommandNaviBack
   *
   * @param naviBackPos
   */
  public void addBackPosParam(String naviBackPos) {
    if (naviBackPos != null)
      addParam(BACK_POS_PARAM, naviBackPos);
  }

  /**
   * Adds a property that will be transferred to the next history navigation
   * scope.
   *
   * @param pName
   *          Name of the navigation scope parameter to add.
   * @param pValue
   *          The parameter value.
   */
  public void addNaviScopeParam(String pName, Object pValue) {
    NaviUtil.checkNaviScopeParam(pName, pValue);

    if (naviScopeParams.size() == 0) {
      naviScopeParams = new TreeMap<String, Object>();
    }
    naviScopeParams.put(pName, pValue);
  }

  /**
   * @return Properties that will be transferred to the history navigation scope.
   */
  public Map<String, Object> getNaviScopeParams() {
    return naviScopeParams;
  }

  @Override
  public String toString() {
    return "" + path +
      (params.size() > 0 ? params : "") +
      (posOnPage != null ? ("#" + posOnPage) : "");
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof NaviLinkImpl) &&
           isLinkToSamePage((NaviLink)obj);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(29, 31).append(path).append(params).toHashCode();
  }

}

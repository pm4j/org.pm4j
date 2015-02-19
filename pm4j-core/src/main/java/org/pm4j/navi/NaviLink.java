package org.pm4j.navi;

import java.util.Map;


/**
 * A link to a target 'page'.
 *
 * @author olaf boede
 */
public interface NaviLink {

  /**
   * A pm4j-request parameter that may be added to a navigation link.<br>
   * It will be used by this class to determine the correct back navigation position.
   */
  public static final String BACK_POS_PARAM = "navi.backPos";

  /**
	 * In case of http applications it returns the URL of the target page (without
	 * url parameters).
	 *
	 * @return The external parge name.
	 */
  String getPath();

	/**
	 * @return An optional definition of a position of the target page.<br>
	 *         For HTML URLs that is the fragment that may appear behind the '#' character.
	 */
  String getPosOnPage();

  /**
   * Some application generate different links for internal and external pages.
   * This method provides a hint about that.
   * <p>
   * Example use case: A web application may adds some internal information
   * to each internal URL. E.g. the current navigation history position.<br>
   * A link to an application external page should not get this additional
   * information parameter.
   *
   * @return <code>true</code> if the link refers to an external URL.
   */
  boolean isExternalLink();

  /**
   * Returns <code>true</code> if all navigation relevant information of both
   * links are equal.<br>
   * Navigation relevant attributes:
   * <ul>
   * <li>{@link #getPath()}</li>
   * <li>{@link #getPosOnPage()}</li>
   * </ul>
   *
   * @return <code>true</code> if all navigation relevant information of both
   *         links are equal.
   */
  boolean isLinkToSamePagePos(NaviLink other);

  /**
   * Returns <code>true</code> if all navigation page relevant information of
   * both links are equal.<br>
   * Navigation relevant attributes:
   * <ul>
   * <li>{@link #getPath()}</li>
   * </ul>
   * Pos-on-page, is not considered here.
   *
   * @return <code>true</code> if all navigation page relevant information of
   *         both links are equal.
   */
  boolean isLinkToSamePage(NaviLink other);
  

  
  /**
   * @return The set of parameters for the page call.
   */
  Map<String, Object> getParams();
}

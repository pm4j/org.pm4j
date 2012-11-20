package org.pm4j.web;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;

/**
 * A simple URL structure information object.
 *
 * @author olaf boede
 */
public class UrlInfo {

  /** The URL page path. */
  private String path;
  /** The URL Parameter set. */
  private Map<String, String> params;
  /** The fragment. */
  private String fragment;

  /**
   * Adds an extra empty dummy parameter in case of an existing fragment.
   * <p>
   * The IE6 adds the fragment to the last parameter value. Which
   * leads to invalid parameter values...
   */
  private boolean ie6FragmentHandling = true;

  public UrlInfo(HttpServletRequest r) {
    this(UrlUtils.buildRequestUrl(r));
  }

  /**
   * @param urlString
   *          The URL path to use.
   */
  public UrlInfo(String urlString) {
    this(urlString, null);
  }

  /**
   * @param urlString
   *          The URL path to use.
   * @param fragment
   *          A fragment to use. May replace the optional fragment provided
   *          within the urlString parameter.
   */
  public UrlInfo(String urlString, String fragment) {
    if (urlString == null) {
      throw new PmRuntimeException("Can't handle a 'null' as urlString.");
    }

    String paramString = null;
    int questMarkPos = urlString.indexOf('?');
    int hashPos = urlString.indexOf('#');

    if (fragment != null) {
      this.fragment = fragment;
    }
    else {
      this.fragment = ((hashPos > 0) && (hashPos < urlString.length()-1))
                  ? urlString.substring(hashPos+1)
                  : null;
    }

    if (questMarkPos >= 0) {
      path = urlString.substring(0, questMarkPos);
      paramString = (hashPos > 0)
          ? urlString.substring(questMarkPos+1, hashPos)
          : urlString.substring(questMarkPos+1);
    }
    else if (hashPos > 0) {
      path = urlString.substring(0, hashPos);
    }
    else {
      path = urlString;
    }

    // Sorted set is used to ensure that query string generation will provide always
    // the same result.
    params = new TreeMap<String, String>();
    if (paramString != null) {
      for (String s : paramString.split("&")) {
        int eqPos = s.indexOf('=');
        params.put(StringUtils.substring(s, 0, eqPos), StringUtils.substring(s, eqPos+1));
      }
    }
  }

  /**
   * @return An URL string that includes the query parameter set and the fragment.
   */
  public String buildUrl() {
    StringBuilder sb = new StringBuilder(120);
    buildUrlWithoutFragment(sb);

    if (fragment != null) {
      if (ie6FragmentHandling && params.size() > 0) {
        sb.append('&');
      }
      sb.append('#').append(fragment);
    }

    return sb.toString();
  }

  /**
   * Builds an URL that contains the path and all parameters, but
   * not the fragment.
   *
   * @return The build URL.
   */
  public String buildUrlWithoutFragment() {
    return buildUrlWithoutFragment(new StringBuilder(100)).toString();
  }

  private StringBuilder buildUrlWithoutFragment(StringBuilder sb) {
    sb.append(path);

    String qs = buildQueryString();
    if (qs != null) {
      char queryStartChar = StringUtils.indexOf(path, '?') == -1 ? '?' : '&';
      sb.append(queryStartChar)
        .append(qs);
    }

    return sb;
  }

  /**
   * Builds the URL that contains all parameters.
   * <p>
   * Example: 'p1=1a&p2=3445'.
   *
   * @return The query string.<br>
   *         <code>null</code> in case of an empty parameter set.
   */
  private String buildQueryString() {
    if (params.size() > 0) {
      StringBuilder sb = new StringBuilder(50);
      for (Map.Entry<String, String> e : params.entrySet()) {
        if (sb.length() > 0)
          sb.append('&');

        sb.append(e.getKey()).append('=').append(e.getValue());
      }
      return sb.toString();
    }
    else {
      return null;
    }
  }

  public String addParam(String key, String value) {
    return params.put(key, value);
  }

  public String removeParam(String key) {
    return params.remove(key);
  }

  public String getParam(String key) {
    return params.get(key);
  }

  public Long getParamAsLong(String key) {
    String s = getParam(key);
    return (s != null)
              ? Long.parseLong(s)
              : null;
  }

  public String getPath() {
    return path;
  }

  public Map<String, String> getParams() {
    return params;
  }

  public String getFragment() {
    return fragment;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setFragment(String fragment) {
    this.fragment = fragment;
  }

  /** @see #ie6FragmentHandling */
  public void setIe6FragmentHandling(boolean ie6FragmentHandling) {
    this.ie6FragmentHandling = ie6FragmentHandling;
  }

  @Override
  public String toString() {
    return buildUrl();
  }

}

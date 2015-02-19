package org.pm4j.jsf;

import java.util.Map;

/**
 * Interface for URL parameter encoder/decoder implementations.
 *
 * @author olaf boede
 */
public interface UrlParamCoder {

  /**
   * Converts the given pm4j request parameter to a name-value map.
   *
   * @param urlParam
   *          The parameter value. May also be <code>null</code> or empty.
   * @return The corresponding map. Is empty when the provides string was
   *         <code>null</code> or empty.
   */
  Map<String, Object> paramValueToMap(String urlParam);

  /**
   * Converts the given name-value map to a pm4j url request parameter value.
   *
   * @param paramMap
   *          The name-value map. May also be <code>null</code> or empty.
   * @return The corresponding parameter value. Is <code>null</code> when the
   *         given map was <code>null</code> or empty.
   */
  String mapToParamValue(Map<String, Object> paramMap);

}

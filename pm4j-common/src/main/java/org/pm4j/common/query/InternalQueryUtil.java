package org.pm4j.common.query;

/**
 * For framework internal usage only!
 * 
 * Provides internal functionality that should not appear in the public API.
 * 
 * @author Olaf Boede
 */
public class InternalQueryUtil {

  /**
   * Copies all parameter values from the given source to this instance.
   * <p>
   * Fires no events.
   *
   * @param src The instance to copy the query parameter values from.
   */
  public static void copyParamValues(QueryParams target, QueryParams src) {
    target.copyParamValues(src);
  }


}

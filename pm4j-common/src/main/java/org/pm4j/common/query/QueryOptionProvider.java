package org.pm4j.common.query;


/**
 * Interface for query services that define {@link QueryOptions} too.
 */
public interface QueryOptionProvider {

  /**
   * Provides the set of filter definitions and attribute sort orders that can be
   * processed by this service.
   *
   * @return
   */
  QueryOptions getQueryOptions();

}

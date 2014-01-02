package org.pm4j.common.query.filter;

import org.pm4j.common.query.QueryAttr;

/**
 * Interface for filter compare definitions.
 * <p>
 * Applications may have their specific filter compare options offered to the
 * users.
 *
 * @author olaf boede
 */
public interface FilterDefinitionFactory {
  /**
   * Creates an attribute value type specific filter definition.
   *
   * @param attr
   *          the attribute to create a filter definition for.
   * @return the filter definition.
   */
  FilterDefinition createCompareDefinition(QueryAttr attr);
}

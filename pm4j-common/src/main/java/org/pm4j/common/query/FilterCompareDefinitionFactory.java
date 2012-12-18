package org.pm4j.common.query;


/**
 * Interface for filter compare definitions.
 * <p>
 * Applications may have their specific filter compare options offered
 * to the users.
 *
 * @author olaf boede
 */
public interface FilterCompareDefinitionFactory {
    /**
     * Creates an attribute value type specific filter definition.
     *
     * @param attr
     *            the attribute to create a filter definition for.
     * @return the filter definition.
     */
    FilterCompareDefinition createCompareDefinition(AttrDefinition attr);
}

package org.pm4j.common.converter.value;


public interface ValueConverter2 <T_EXTERNAL, T_INTERNAL> {

    /**
     * Converts a backing attribute value to an external attribute value.
     *
     * @param pmAttr The attribute instance the conversation is done for.
     * @param i The backing value to convert.
     * @return The corresponding external value.
     */
    T_EXTERNAL toExternalValue(ValueConverterCtxt ctxt, T_INTERNAL i);

    /**
     * Converts an external attribute value to a backing attribute value.
     *
     * @param pmAttr The attribute instance the conversation is done for.
     * @param e The external value to convert.
     * @return The corresponding backing value.
     */
    T_INTERNAL toInternalValue(ValueConverterCtxt ctxt, T_EXTERNAL e);

}

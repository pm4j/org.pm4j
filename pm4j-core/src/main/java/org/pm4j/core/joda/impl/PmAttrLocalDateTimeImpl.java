package org.pm4j.core.joda.impl;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.joda.PmAttrLocalDateTime;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmObjectBase;

/**
 * Pm Attribute for a {@link LocalDateTime}.
 *
 * This field can handle multiple input formats to be defined ;-separated in the resources with
 * key suffix "_format" appended to the fields resource key.
 *
 * @author olaf boede
 */
public class PmAttrLocalDateTimeImpl extends PmAttrBase<LocalDateTime, LocalDateTime> implements PmAttrLocalDateTime {

    /**
     * @param pmParent
     *            The PM parent.
     */
    public PmAttrLocalDateTimeImpl(PmObject pmParent) {
        super(pmParent);
    }

    /**
     * Custom implementation to compare {@link LocalDate} objects
     */
    @Override
    public int compareTo(PmObject otherPm) {
        // TODO oboede: should have a default implementation for all attributes that handles all
        // cases...
        return CompareUtil.compare(getValue(), ((PmAttrLocalDateTime) otherPm).getValue());
    }

    @Override
    protected String getFormatDefaultResKey() {
        return FORMAT_DEFAULT_RES_KEY;
    }

    @Override
    protected PmObjectBase.MetaData makeMetaData() {
        /** Sets the default max length is the length of the date format pattern. */
        return new MetaData(11);
    }

    /** Adjusts the default converter. */
    @Override
    protected void initMetaData(PmObjectBase.MetaData metaData) {
        super.initMetaData(metaData);
        ((PmAttrBase.MetaData) metaData).setConverterDefault(PmConverterLocalDateTime.INSTANCE);
    }

}

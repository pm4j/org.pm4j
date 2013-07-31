package org.pm4j.core.joda.impl;

import org.joda.time.LocalDate;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.joda.PmAttrLocalDate;
import org.pm4j.core.pm.PmAttrDate;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmObjectBase;

/**
 * PM attribute for a {@link LocalDate}.
 * 
 * This field can handle multiple input formats to be defined ;-separated in the resources with
 * key suffix "_format" appended to the fields resource key.
 * 
 * @author Harm Gnoyke, Olaf Boede
 */
public class PmAttrLocalDateImpl extends PmAttrBase<LocalDate, LocalDate> implements PmAttrLocalDate {

    /**
     * @param pmParent
     *            The PM parent.
     */
    public PmAttrLocalDateImpl(PmObject pmParent) {
        super(pmParent);
    }

    /**
     * Custom implementation to compare {@link LocalDate} objects
     */
    @Override
    public int compareTo(PmObject otherPm) {
        // TODO oboede: should have a default implementation for all attributes that handles all
        // cases...
        return CompareUtil.compare(getValue(), ((PmAttrLocalDate) otherPm).getValue());
    }

    @Override
    protected PmObjectBase.MetaData makeMetaData() {
        /** Sets the default max length is the length of the date format pattern. */
        // TODO oboede: needs to be derived from the format.
        return new MetaData(11);
    }

    /** Adjusts the default converter. */
    @Override
    protected void initMetaData(PmObjectBase.MetaData metaData) {
        super.initMetaData(metaData);
        ((PmAttrBase.MetaData) metaData).setConverterDefault(PmConverterLocalDate.INSTANCE);
    }

    /** Uses {@link PmAttrDate#RESKEY_DEFAULT_FORMAT_PATTERN}. */
    @Override
    protected String getFormatDefaultResKey() {
        return PmAttrDate.RESKEY_DEFAULT_FORMAT_PATTERN;
    }
}

package org.pm4j.core.joda;

import org.joda.time.LocalDateTime;
import org.pm4j.core.pm.PmAttr;

/**
 * A PM attribute that handles {@link LocalDateTime} values.
 * 
 * @author olaf boede
 */
public interface PmAttrLocalDateTime extends PmAttr<LocalDateTime> {

    /**
     * The default format resource key that is used of no attribute specific format is defined.
     */
    public static final String FORMAT_DEFAULT_RES_KEY = "pmAttrDateTime_defaultFormat";

}

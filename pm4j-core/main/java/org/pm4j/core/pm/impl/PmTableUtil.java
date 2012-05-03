package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmPager.PagerVisibility;

/**
 * Some table related helper functions.
 *
 * @author olaf boede
 */
public final class PmTableUtil {

    /**
     * Defines the properties of the given table as needed for scrollable (not pageable) tables.
     *
     * @param table
     *            The table to adjust.
     */
    public static void setScrollableTableProperties(PmTableImpl<?> table) {
        table.setNumOfPageRows(Integer.MAX_VALUE);
        if (table.getPager() != null) {
          table.getPager().setPagerVisibility(PagerVisibility.WHEN_SECOND_PAGE_EXISTS);
        }
    }

}

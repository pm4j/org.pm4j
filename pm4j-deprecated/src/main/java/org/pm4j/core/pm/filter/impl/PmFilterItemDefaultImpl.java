package org.pm4j.core.pm.filter.impl;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.filter.FilterByDefinition;

/**
 * Default implementation for a filter item PM.
 * <p>
 * Applications that need a different UI logic for their filters may use this class as a
 * template and add extend that copy with specific logic.
 *
 * @author olaf boede
 */
public class PmFilterItemDefaultImpl extends PmFilterItemBase {

  public final PmAttr<FilterByDefinition> filterByImpl  = new FilterByAttrPm();
  public final PmAttr<?>                  compOpImpl    = new CompOpAttrPm();

}

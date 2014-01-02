package org.pm4j.core.pm.filter.impl;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.filter.DeprFilterByDefinition;

/**
 * Default implementation for a filter item PM.
 * <p>
 * Applications that need a different UI logic for their filters may use this class as a
 * template and add extend that copy with specific logic.
 *
 * @author olaf boede
 */
@Deprecated
public class DeprPmFilterItemDefaultImpl extends DeprPmFilterItemBase {

  public final PmAttr<DeprFilterByDefinition> filterByImpl  = new FilterByAttrPm();
  public final PmAttr<?>                  compOpImpl    = new CompOpAttrPm();

}

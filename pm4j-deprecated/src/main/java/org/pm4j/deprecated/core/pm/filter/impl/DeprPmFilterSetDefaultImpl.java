package org.pm4j.deprecated.core.pm.filter.impl;

import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.deprecated.core.pm.filter.DeprCombinedBy;
import org.pm4j.deprecated.core.pm.filter.DeprFilterByDefinitionProvider;
import org.pm4j.deprecated.core.pm.filter.DeprFilterable;
import org.pm4j.deprecated.core.pm.filter.DeprPmFilterItem;

/**
 * Default implementation for a filter set PM.
 * <p>
 * Applications that need a different UI logic for their filters may use this class as a
 * template and add extend that copy with specific logic.
 *
 * @author olaf boede
 */
@Deprecated
public class DeprPmFilterSetDefaultImpl extends DeprPmFilterSetBase {

  public final PmAttrPmList<DeprPmFilterItem> filterItemsImpl = new FilterItemsAttrPm();

  public final PmAttrEnum<DeprCombinedBy> combinedByImpl = new CombinedByAttrPm();

  public final PmCommand cmdApplyImpl = new CmdApply();

  public final PmCommand cmdClearImpl = new CmdClear();

  public DeprPmFilterSetDefaultImpl(PmObject pmParent, DeprFilterByDefinitionProvider filterByDefinitionProvider, DeprFilterable filterable) {
    super(pmParent, filterByDefinitionProvider, filterable);
  }

  public DeprPmFilterSetDefaultImpl(PmObject pmParent) {
    super(pmParent);
  }



}

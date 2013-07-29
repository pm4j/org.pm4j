package org.pm4j.core.pm.filter.impl;

import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.filter.CombinedBy;
import org.pm4j.core.pm.filter.FilterByDefinitionProvider;
import org.pm4j.core.pm.filter.Filterable;
import org.pm4j.core.pm.filter.PmFilterItem;

/**
 * Default implementation for a filter set PM.
 * <p>
 * Applications that need a different UI logic for their filters may use this class as a
 * template and add extend that copy with specific logic.
 *
 * @author olaf boede
 */
public class PmFilterSetDefaultImpl extends PmFilterSetBase {

  public final PmAttrPmList<PmFilterItem> filterItemsImpl = new FilterItemsAttrPm();

  public final PmAttrEnum<CombinedBy> combinedByImpl = new CombinedByAttrPm();

  public final PmCommand cmdApplyImpl = new CmdApply();

  public final PmCommand cmdClearImpl = new CmdClear();

  public PmFilterSetDefaultImpl(PmObject pmParent, FilterByDefinitionProvider filterByDefinitionProvider, Filterable filterable) {
    super(pmParent, filterByDefinitionProvider, filterable);
  }

  public PmFilterSetDefaultImpl(PmObject pmParent) {
    super(pmParent);
  }



}

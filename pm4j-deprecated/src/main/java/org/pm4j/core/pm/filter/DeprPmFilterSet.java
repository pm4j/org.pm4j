package org.pm4j.core.pm.filter;

import java.util.List;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommand;

@Deprecated
public interface DeprPmFilterSet extends PmBean<DeprFilterSet> {

  /** Default identifier for user defined filter sets. */
  static final String USER_FILTER_SET_ID = "userFilterSet";

  PmAttr<List<DeprPmFilterItem>> getFilterItems();

  PmAttr<DeprCombinedBy> getCombinedBy();

  PmCommand getCmdApply();

  PmCommand getCmdClear();
}

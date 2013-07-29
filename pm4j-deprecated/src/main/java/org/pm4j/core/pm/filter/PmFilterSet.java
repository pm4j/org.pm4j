package org.pm4j.core.pm.filter;

import java.util.List;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommand;

public interface PmFilterSet extends PmBean<FilterSet> {

  /** Default identifier for user defined filter sets. */
  static final String USER_FILTER_SET_ID = "userFilterSet";

  PmAttr<List<PmFilterItem>> getFilterItems();

  PmAttr<CombinedBy> getCombinedBy();

  PmCommand getCmdApply();

  PmCommand getCmdClear();
}

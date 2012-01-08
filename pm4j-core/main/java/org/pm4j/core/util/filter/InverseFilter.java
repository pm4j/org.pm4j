package org.pm4j.core.util.filter;

import org.apache.commons.lang.Validate;

/**
 * Inverts the filter condition of another given filter.
 */
public class InverseFilter implements Filter {

  private Filter baseFilter;
  
  public InverseFilter(Filter baseFilter) {
    Validate.notNull(baseFilter);
    
    this.baseFilter = baseFilter;
  }
  
  public boolean accept(Object item) {
    return !baseFilter.accept(item);
  }

}

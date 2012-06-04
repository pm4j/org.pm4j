package org.pm4j.core.pm.filter;

import java.util.List;


/**
 * Interface for classes that may provide filter-by definitions.<br>
 * E.g. a table column may provide its specific filter by definitions.
 *
 * @author olaf boede
 */
public interface FilterByDefinitionProvider {

  /**
   * Provides the set of current filter definitions that can be
   * specified/modified for this object.<br>
   * Provides an empty collection if there is no filter definition. Never
   * <code>null</code>.
   *
   * @return The set of filter by definitions.
   */
  List<FilterByDefinition> getFilterByDefinitions();

}

package org.pm4j.core.util.filter;

import java.util.HashSet;

import org.pm4j.core.pm.PmAttr;

/**
 * A filter that considers a set of specified item names.
 * <p>
 * It can <i>include</i> or <i>exclude</i> the specified names. That behavior
 * can be configured using the attribute {@link #excludeFilter}.
 */
public class ItemNameFilter extends ItemFilter<PmAttr<?>> {

  private HashSet<String> nameSet = new HashSet<String>();

  private boolean excludeFilter;

  /**
   * @param excludeFilter
   *          Defines if the specified item names should be included or
   *          excluded.
   * @param names
   *          The item names to consider.
   */
  public ItemNameFilter(boolean excludeFilter, String... names) {
    super();

    this.excludeFilter = excludeFilter;

    for (String s : names) {
      nameSet.add(s);
    }
  }

  /**
   * Initializes an include filter that considers only the specified names.
   * 
   * @param names
   *          The item names to consider.
   */
  public ItemNameFilter(String... names) {
    this(false, names);
  }

  /**
   * @param excludeFilter
   *          Defines if the specified item names should be included or
   *          excluded.
   */
  public ItemNameFilter(boolean excludeFilter) {
    this(excludeFilter, new String[0]);
  }

  /**
   * Initializes a empty filter that includes the specified item names.
   */
  public ItemNameFilter() {
    this(false);
  }

  /**
   * @param name Another name to be include in the filter condition.
   */
  public void addName(String name) {
    this.nameSet.add(name);
  }
  
  @Override
  public boolean acceptItem(PmAttr<?> item) {
    boolean isInSet = nameSet.contains(item.getPmName());
    return (isInSet ^ excludeFilter);
  }

  public boolean isExcludeFilter() {
    return excludeFilter;
  }

  public void setExcludeFilter(boolean excludeFilter) {
    this.excludeFilter = excludeFilter;
  }
  
}

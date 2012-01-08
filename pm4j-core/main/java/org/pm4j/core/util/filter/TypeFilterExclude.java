package org.pm4j.core.util.filter;

/**
 * TOCOMMENT:
 * 
 * @param <T_ITEM>
 *          The item fieldClass to filter.
 */
public class TypeFilterExclude extends TypeFilterInclude {

  public TypeFilterExclude() {
    super();
  }

  public TypeFilterExclude(Class<?>... typeClasses) {
    super(typeClasses);
  }

  /**
   * @param item
   * @return <code>false</code> for all items that are derived from a
   *         registered class or interface.
   */
  public boolean acceptItem(Object item) {
    return !super.acceptItem(item);
  }

}

package org.pm4j.common.query;

import java.io.Serializable;
import java.util.List;

/**
 * A sort order specification.
 *
 * @author olaf boede
 */
public interface SortOrder extends Serializable {

  /**
   * Provides a copy of this sort order that sorts in the reverse order.
   *
   * @return A reverse sort order definition.
   */
  SortOrder getReverseSortOrder();

  /**
   * The set of attributes to be sorted by.
   *
   * @return the attributes to sort by.
   */
  List<AttrSortSpec> getAttrSortSpecs();

  public class AttrSortSpec implements Serializable {
    private static final long serialVersionUID = 1L;

    private final AttrDefinition attribute;
    private final boolean ascending;

    public AttrSortSpec(AttrDefinition attribute, boolean ascending) {
      assert attribute != null;
      this.attribute = attribute;
      this.ascending = ascending;
    }

    public AttrDefinition getAttribute() {
      return attribute;
    }

    public boolean isAscending() {
      return ascending;
    }
  }

}
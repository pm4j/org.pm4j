package org.pm4j.common.selection;

public interface TwoPhasePropertyChangeListener<T_ITEM> {

  boolean beforeChange(Selection<T_ITEM> oldSelection, Selection<T_ITEM> newSelection);

  void afterChange(Selection<T_ITEM> oldSelection, Selection<T_ITEM> newSelection);
}

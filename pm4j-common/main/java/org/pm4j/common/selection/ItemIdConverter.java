package org.pm4j.common.selection;

public interface ItemIdConverter<T_ITEM, T_ID> {

  T_ID getIdForItem(T_ITEM item);
  T_ITEM getItemForId(T_ID id);
}

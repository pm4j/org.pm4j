package org.pm4j.common.selection;

/**
 * Converts between query or selection items and their corresponding identifiers.
 *
 * @param <T_ITEM> the item type.
 * @param <T_ID> the item id type.
 *
 * @author olaf boede
 */
public interface ItemIdConverter<T_ITEM, T_ID> {

  /**
   * @param item the item to get the identifier for.
   * @return the item identifier.
   */
  T_ID getIdForItem(T_ITEM item);

  /**
   * @param id the identifier to get the item for.
   * @return the item.
   */
  T_ITEM getItemForId(T_ID id);
}

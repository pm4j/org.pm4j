package org.pm4j.common.modifications;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pm4j.common.selection.ItemSetSelection;
import org.pm4j.common.util.collection.IterableUtil;
import org.pm4j.common.util.collection.ListUtil;

/**
 * Helper methods for {@link Modifications}.
 * 
 * @author Olaf Boede
 */
public final class ModificationsUtil {

  /**
   * Converter used for
   * {@link ModificationsUtil#convertModifications(Modifications, Converter)}.
   * 
   * Converts T1 to T2.
   */
  public interface Converter<T1, T2> {

    /**
     * @param src
     * @return The converted instance or <code>null</code>.<br>
     *         <code>null</code> means that the item should not be reported as a
     *         modification.
     */
    T2 convert(T1 src);
  }

  /**
   * Converts {@link Modifications} reported for type T1 to
   * {@link Modifications} of T2 instances.
   * 
   * @param src
   *          {@link Modifications} reported for T1
   * @param converter
   *          knows how to convert T1 to T2.<br>
   *          Each convert() call that returns a <code>null</code> will not be
   *          added to the result.
   * @return corresponding {@link Modifications} reported for T2
   */
  public static <T1, T2> Modifications<T2> convertModifications(Modifications<T1> src, Converter<T1, T2> converter) {
    ModificationsImpl<T2> result = new ModificationsImpl<>();
    for (T1 t1 : src.getAddedItems()) {
      result.registerAddedItem(converter.convert(t1));
    }
    for (T1 t1 : src.getUpdatedItems()) {
      result.registerUpdatedItem(converter.convert(t1), true);
    }
    Set<T2> removedItems = new HashSet<>();
    for (T1 t1 : src.getRemovedItems()) {
      removedItems.add(converter.convert(t1));
    }
    result.setRemovedItems(new ItemSetSelection<T2>(removedItems));
    return result;
  }

  /**
   * Creates a {@link Modifications} instance using the given item sets.
   * 
   * @param added the set of added items. May be <code>null</code>.
   * @param updated the set of updated items. May be <code>null</code>.
   * @param removed the set of removed items. May be <code>null</code>.
   * @return a {@link Modifications} instance.
   */
  public static <T> Modifications<T> createModfications(Collection<T> added, Collection<T> updated,
      Collection<T> removed) {
    ModificationsImpl<T> m = new ModificationsImpl<>();
    registerAddedItems(m, added);
    registerUpdatedItems(m, updated);
    registerRemovedItems(m, removed);
    return m;
  }

  /**
   * @param modifications
   *          the {@link Modifications} to check
   * @param item
   *          the item to check
   * @return <code>true</code> if the item is registered as added, updated or
   *         removed item.
   */
  public static <T> boolean isModified(Modifications<T> modifications, T item) {
    return modifications.getAddedItems().contains(item) 
        || modifications.getUpdatedItems().contains(item)
        || modifications.getRemovedItems().contains(item);
  }

  /**
   * @param modifications
   *          the modifications to change
   * @param items
   *          the items to register. May also be <code>null</code>.
   */
  public static <T> void registerAddedItems(ModificationsImpl<T> modifications, Iterable<T> items) {
    if (items != null) {
      for (T item : items) {
        modifications.registerAddedItem(item);
      }
    }
  }

  /**
   * @param modifications
   *          the modifications to change
   * @param items
   *          the items to register. May also be <code>null</code>.
   */
  public static <T> void registerUpdatedItems(ModificationsImpl<T> modifications, Iterable<T> items) {
    if (items != null) {
      for (T item : items) {
        modifications.registerUpdatedItem(item, true);
      }
    }
  }

  /**
   * @param modifications
   *          the modifications to change
   * @param items
   *          the items to register. May also be <code>null</code>.
   */
  public static <T> void registerRemovedItems(ModificationsImpl<T> modifications, Iterable<T> items) {
    if (items != null) {
      // XXX oboede: full iteration not really ok for very large selections.
      // not very likely but may get a problem when deleting thousands of items.
      List<T> removedItems = new ArrayList<T>(IterableUtil.asCollection(modifications.getRemovedItems()));
      removedItems.addAll(ListUtil.toList(items));
      modifications.setRemovedItems(new ItemSetSelection<>(new HashSet<T>(removedItems)));
    }
  }

  /**
   * Aggregates a set of {@link Modifications} to a single {@link Modifications} instance.
   * 
   * @param modificationSet
   *          the modifications to aggregate
   * @return a single instance containing all modification items
   */
  public static <T> Modifications<T> joinModifications(Collection<? extends Modifications<T>> modificationSet) {
    if (modificationSet.size() == 1) {
      return modificationSet.iterator().next();
    }

    ModificationsImpl<T> result = new ModificationsImpl<>();
    for (Modifications<T> m : modificationSet) {
      registerAddedItems(result, m.getAddedItems());
      registerUpdatedItems(result, m.getUpdatedItems());
      registerRemovedItems(result, m.getRemovedItems());
    }
    return result;
  }
  
  private ModificationsUtil() {}

}

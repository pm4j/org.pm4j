package org.pm4j.common.selection;

import java.util.Collection;

/**
 * @deprecated Exists only to keep TablePmUtil up and running.
 */
public interface ItemIdBasedSelection<T_ITEM, T_ID> extends Selection<T_ITEM> {

  Collection<T_ID> getIds();
}

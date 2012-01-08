package org.pm4j.core.util.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * TOCOMMENT: 
 * @param <T_ITEM> 
 */
public abstract class ItemFilter<T_ITEM> implements Filter {

  @SuppressWarnings("unchecked")
  public boolean accept(Object o) {
    return acceptItem((T_ITEM) o);
  }

  public abstract boolean acceptItem(T_ITEM item);
  
  public List<T_ITEM> filter(Iterable<T_ITEM> unfilteredSet) {
    if (unfilteredSet == null) {
      return Collections.emptyList();
    }
    
    List<T_ITEM> filteredList = new ArrayList<T_ITEM>();
    for (T_ITEM i : unfilteredSet) {
      if (acceptItem(i)) {
        filteredList.add(i);
      }
    }

    return filteredList;
  }
}

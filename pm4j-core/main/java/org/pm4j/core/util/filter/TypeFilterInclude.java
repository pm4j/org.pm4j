package org.pm4j.core.util.filter;

import java.util.HashSet;

import org.apache.commons.lang.ClassUtils;

/**
 * TOCOMMENT: 
 * @param <T_ITEM> The item class to filter.
 */
public class TypeFilterInclude extends ItemFilter<Object> {

  private HashSet<Class<?>> typeClassSet = new HashSet<Class<?>>();
  private HashSet<Class<?>> typeInterfaceSet = new HashSet<Class<?>>();
  
  public TypeFilterInclude() {
    super();
  }
  
  public TypeFilterInclude(Class<?>... typeClasses) {
    for (Class<?> c : typeClasses) {
      addType(c);
    }
  }
  
  public void addType(Class<?> type) {
    assert type != null;
    
    if (type.isInterface()) {
      typeInterfaceSet.add(type);
    }
    else {
      typeClassSet.add(type);
    }
  }
  
  /**
   * @param item 
   * @return <code>true</code> for all items that have a registered fieldClass definition. 
   */
  public boolean acceptItem(Object item) {
    Class<?> itemClass = item.getClass();
    
    if (itemClass.isInterface()) {
      if (typeInterfaceSet.contains(itemClass)) {
        return true;
      }
    }
    else {
      Class<?> c = itemClass;
      while (c != null) {
        if (typeClassSet.contains(c)) {
          return true;
        }
        c = c.getSuperclass();
      }
    }
    
    for (Object i : ClassUtils.getAllInterfaces(itemClass)) {
      if (typeInterfaceSet.contains(i)) {
        return true;
      }
    };

    return false;
  }
}

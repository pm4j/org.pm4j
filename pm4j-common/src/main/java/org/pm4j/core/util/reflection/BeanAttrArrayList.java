package org.pm4j.core.util.reflection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.ObjectUtils;

public class BeanAttrArrayList<T> implements List<T> {

  private final Object bean;
  private final BeanAttrAccessor[] accessorArray;

  public BeanAttrArrayList(Object bean, BeanAttrAccessor[] accessorArray) {
    this.bean = bean;
    this.accessorArray = accessorArray;
  }

  /**
   * Creates a list of PMs representing the items, addressed by an accessor
   * array and a list of dynamic child instances.
   *
   * @param bean
   *          The bean to apply the accessors on.
   * @param accessorArray
   *          The accessors used to create the {@link BeanAttrArrayList}.
   * @param extensionList
   *          An optional list of additional items to include.
   * @return A list over the instances, referenced by the accessors and the
   *         extension.
   */
  public static <T> List<T> makeList(Object bean, BeanAttrAccessor[] accessorArray, List<T> extensionList) {
    if (accessorArray.length == 0 &&
        extensionList.isEmpty()) {
      return Collections.emptyList();
    }
    else
      return extensionList.isEmpty()
          ? new BeanAttrArrayList<T>(bean, accessorArray)
          : new BeanAttrArrayList.WithArrayExtension<T>(bean, accessorArray, extensionList);
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> makeList(Object bean, BeanAttrAccessor[] accessorArray) {
    return accessorArray.length == 0
          ? Collections.EMPTY_LIST
          : new BeanAttrArrayList<T>(bean, accessorArray);
  }

  @Override public boolean add(T e) { throw new RuntimeException("not implemented."); }
  @Override public void add(int index, T element) { throw new RuntimeException("not implemented."); }
  @Override public boolean addAll(Collection<? extends T> c) { throw new RuntimeException("not implemented."); }
  @Override public boolean addAll(int index, Collection<? extends T> c) { throw new RuntimeException("not implemented."); }
  @Override public void clear() { throw new RuntimeException("not implemented."); }

  @Override
  public boolean contains(Object o) {
    for (int i=0; i<accessorArray.length; ++i) {
      if (ObjectUtils.equals(o, get(i))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (Iterator<?> iter = c.iterator(); iter.hasNext(); ) {
      if (!contains(iter.next())) {
        return false;
      }
    }
    return true;
  }

  @Override
  public T get(int index) {
    return accessorArray[index].<T>getBeanAttrValue(bean);
  }

  @Override
  public int indexOf(Object o) {
    for (int i=0; i<accessorArray.length; ++i) {
      if (ObjectUtils.equals(o, get(i))) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public boolean isEmpty() {
    return accessorArray.length == 0;
  }

  @Override
  public Iterator<T> iterator() {
    return listIterator();
  }

  @Override
  public int lastIndexOf(Object o) {
    for (int i=accessorArray.length-1; i>=0; --i) {
      if (ObjectUtils.equals(o, get(i))) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public ListIterator<T> listIterator() {
    return listIterator(0);
  }

  @Override
  public ListIterator<T> listIterator(final int index) {
    return new ListIterator<T>() {
      int idx = index-1;

      @Override
      public boolean hasNext() {
        return idx < size() - 1;
      }

      @Override
      public T next() {
        return get(++idx);
      }

      @Override public void remove() { throw new UnsupportedOperationException(); }
      @Override public void add(T e) { throw new UnsupportedOperationException(); }

      @Override
      public boolean hasPrevious() {
        return idx > 0;
      }

      @Override
      public int nextIndex() {
        return idx+1;
      }

      @Override
      public T previous() {
        return null;
      }

      @Override
      public int previousIndex() {
        return idx-1;
      }

      @Override
      public void set(T e) { throw new UnsupportedOperationException(); }
    };
  }

  @Override public boolean remove(Object o) { throw new RuntimeException("not implemented."); }
  @Override public T remove(int index) { throw new RuntimeException("not implemented."); }
  @Override public boolean removeAll(Collection<?> c) { throw new RuntimeException("not implemented."); }
  @Override public boolean retainAll(Collection<?> c) { throw new RuntimeException("not implemented."); }
  @Override public T set(int index, T element) { throw new RuntimeException("not implemented."); }

  @Override
  public int size() {
    return accessorArray.length;
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    List<T> list = new ArrayList<T>(toIndex-fromIndex);
    for (int i=fromIndex; i<toIndex; ++i) {
      list.add(get(i));
    }
    return list;
  }

  @Override
  public Object[] toArray() {
    return toArray(new Object[size()]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T2> T2[] toArray(T2[] a) {
    int size = size();
    T2[] arr = (a.length == size)
        ? a
        : (T2[])new Object[size];

    for (int i=0; i<size; ++i) {
      arr[i] = (T2)get(i);
    }
    return arr;
  }

  public static class WithArrayExtension<T> extends BeanAttrArrayList<T> {
    private final List<T> extensionList;

    public WithArrayExtension(Object bean, BeanAttrAccessor[] accessorArray, List<T> extensionList) {
      super(bean, accessorArray);
      this.extensionList = extensionList;
    }

    @Override
    public boolean contains(Object o) {
      return super.contains(o) ||
             extensionList.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      return super.containsAll(c) ||
             extensionList.containsAll(c);
    }

    @Override
    public T get(int index) {
      return index < super.size()
          ? super.get(index)
          : extensionList.get(index - super.size());
    }

    @Override
    public int indexOf(Object o) {
      int i = super.indexOf(o);
      if (i != -1) {
        return i;
      }
      i = extensionList.indexOf(o);
      return i != -1
          ? i + super.size()
          : -1;
    }

    @Override
    public boolean isEmpty() {
      return super.isEmpty() && extensionList.isEmpty();
    }

    @Override
    public int lastIndexOf(Object o) {
      int i = extensionList.lastIndexOf(o);
      if (i != -1) {
        return i + super.size();
      }
      i = super.lastIndexOf(o);
      return i != -1
          ? i
          : -1;
    }

    @Override
    public int size() {
      return super.size() + extensionList.size();
    }

  };

}

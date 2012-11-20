package org.pm4j.core.pm;

import java.util.List;

import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.pm.annotation.PmAttrListCfg;

public interface PmAttrList<T> extends PmAttr<List<T>> {

  /**
   * An explicte list value signature. This signature is required for reflection
   * based frameworks that analyze the method signature for their data binding
   * (e.g. JSF). Such analysis for {@link PmAttr#getValue()} will just find out
   * that the method returns an object. The same analysis perfomed on this
   * method will provide the result that the returned value is a list...
   *
   * @return The list value.
   */
  List<T> getValueAsList();

  /**
   * @see #getValueAsList()
   * @param value
   *          The new list value.
   */
  void setValueAsList(List<T> value);

  /**
   * A string list interface for UI controls that only support string lists.<br>
   * Example: rich:pickList
   * <p>
   * Attention: Will only work if an {@link PmAttrListCfg#itemConverter()} is
   * defined.
   *
   * @return The items as a list of strings.
   */
  List<String> getValueAsStringList();

  /**
   * A string list interface for UI controls that only support string lists.<br>
   * Example: rich:pickList
   * <p>
   * Attention: Will only work if an {@link PmAttrListCfg#itemConverter()} is
   * defined.
   *
   * @param value The items as a list of strings.
   */
  void setValueAsStringList(List<String> value) throws PmConverterException;

  /**
   * Provides a subset of the whole item set.
   *
   * @param fromIdx
   *          Index of the first item to get.
   * @param numItems
   *          The maximal number of items to get.
   * @return The subset. May be empty but never <code>null</code>
   */
  List<T> getValueSubset(int fromIdx, int numItems);

  /**
   * @return number of list items provided by #{link {@link #getValue()}.
   */
  int getSize();

}

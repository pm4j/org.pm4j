package org.pm4j.common.query.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.pm4j.common.query.CompOp;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.util.reflection.ClassUtil;

/**
 * Provides the meta data set for the UI of a filter item.<br>
 * It provides the options the user can define for a filter item.
 *
 * @author olaf boede
 */
public class FilterDefinition {

  private QueryAttr          attr;
  private Collection<CompOp> compOps;
  private CompOp             defaultCompOp;
  private Object             defaultFilterByValue;

  @SuppressWarnings("unchecked")
  public FilterDefinition(QueryAttr attr, Collection<CompOp> compOps) {
    assert attr != null;

    this.attr = attr;
    this.compOps = new ArrayList<CompOp>(compOps != null ? compOps : Collections.EMPTY_LIST);
  }

  public FilterDefinition(QueryAttr attr, CompOp... compOps) {
    assert attr != null;

    this.attr = attr;
    this.compOps = new ArrayList<CompOp>(Arrays.asList(compOps));
  }

  public FilterDefinition(QueryAttr attr, Class<? extends CompOp>... compOps) {
    assert attr != null;

    this.attr = attr;
    this.compOps = new ArrayList<CompOp>(compOps.length);

    for (Class<? extends CompOp> c : compOps) {
      CompOp co = ClassUtil.newInstance(c);
      this.compOps.add(co);
    }
  }


  /**
   * @return the attribute to compare.
   */
  public QueryAttr getAttr() {
    return attr;
  }

  // TODO: olaf resource based name resulution needs to be implemented.
  public String getAttrTitle() {
    return attr.getTitle() != null ? attr.getTitle() : attr.getName();
  }

  /**
   * @return the set of compare operators that can be applied.
   */
  public Collection<CompOp> getCompOps() {
    return compOps;
  }

  /**
   * @return the default compare operator.
   */
  public CompOp getDefaultCompOp() {
    return defaultCompOp;
  }

  /**
   * @return The default filter-by value.
   */
  public Object getDefaultFilterByValue() {
    return defaultFilterByValue;
  }

  public void setCompOps(Collection<CompOp> compOps) {
    this.compOps = compOps;
  }

  public void setDefaultCompOp(CompOp defaultCompOp) {
    this.defaultCompOp = defaultCompOp;
  }

  public void setDefaultFilterByValue(Object defaultFilterByValue) {
    this.defaultFilterByValue = defaultFilterByValue;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(this.getClass().getName());
    sb.append("[QueryAttr: ").append(attr.getName()).append(" - CompOps:");
    if (compOps != null) {
      sb.append(compOps.toString());      
    } else {
      sb.append("null");
    }
    sb.append(" - DefaultCompOp: ");
    if (defaultCompOp != null) {
      sb.append(defaultCompOp.toString());      
    } else {
      sb.append("null");
    }
    sb.append(" - DefaultFilterByValue: ");
    if (defaultFilterByValue != null) {
      sb.append(defaultFilterByValue.toString());      
    } else {
      sb.append("null");
    }
    sb.append("]");
    return sb.toString();
  }
}

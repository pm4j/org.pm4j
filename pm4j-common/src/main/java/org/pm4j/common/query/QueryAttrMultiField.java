package org.pm4j.common.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.Expression;
import org.pm4j.common.expr.PathExpressionChain;
import org.pm4j.common.util.collection.MultiObjectValue;
import org.pm4j.core.util.lang.CloneUtil;


/**
 * Descriptor for an attribute that is defined by a set for individual fields.
 * <p>
 * A typical use case is a business key comprising multiple fields.
 *
 * @author olaf boede
 */
public class QueryAttrMultiField implements QueryAttr.MultiPart {

  private static final long serialVersionUID = 1L;

  private final String   name;
  private final String   title;

  /** The set of attribute fields. */
  private List<QueryAttr.WithPath> parts = new ArrayList<QueryAttr.WithPath>();


  public QueryAttrMultiField(String name) {
    this(name, name);
  }

  public QueryAttrMultiField(String name, String title) {
    assert StringUtils.isNotBlank(name);
    this.name = name;
    this.title = title;
  }

  /**
   * Adds an attribute field set part.
   * <p>
   * Please notice that the pathName of the part is <b>not</b> relative to the main attribute.
   *
   * @param part the field set part to add.
   * @return the whole multi field attribute for inline usage.
   */
  public QueryAttrMultiField addPart(QueryAttr.WithPath part) {
    assert part != null;
    parts.add(part);
    return this;
  }

  /**
   * Adds an attribute field set part.
   * <p>
   * Please notice that the pathName of the part is <b>not</b> relative to the main attribute.
   *
   * @param path the relative path of the multi field part to add.
   * @param type the field part type.
   * @return the whole multi field attribute for inline usage.
   */
  public QueryAttrMultiField addPart(String path, Class<?> type) {
    return addPart(new AttrDefinition(path, type));
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public List<WithPath> getParts() {
    if (parts.isEmpty()) {
      throw new IllegalStateException("Multi field attribute '" + this +
          "'has no parts. Please add all parts before using the method getParts()");
    }
    return parts;
  }

  /**
   * Each attribute part contains already the full path identifier.
   */
  @Override
  public List<QueryAttr.WithPath> getPartsWithFullPath() {
    return getParts();
  }

  @Override
  public QueryAttrMultiField clone() {
    try {
      QueryAttrMultiField clone = (QueryAttrMultiField) super.clone();
      clone.parts = CloneUtil.cloneList(this.getParts(), true);
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || getClass() != obj.getClass()) return false;

    QueryAttrMultiField other = (QueryAttrMultiField) obj;
    return new EqualsBuilder().append(name, other.name).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 41).append(name).toHashCode();
  }

  @Override
  public String toString() {
    return parts.toString();
  }

  /**
   * A reflection based helper method to generate a value object from a multi field attribute
   * definition.
   * <p>
   * The path strings of the attribute parts are used to get the values by reflection.
   *
   * @param item the object to retrieve the attribute values from.
   * @param fd the multi field attribute descriptor.
   * @return a value object that represents all the attribute part values.
   */
  // XXX olaf: check how the algorithm of InMemQueryEvaluator can be used directly.
  static MultiObjectValue makeValueObject(Object item, QueryAttrMultiField fd) {
    List<QueryAttr.WithPath> parts = fd.getParts();
    Object[] values = new Object[parts.size()];
    for (int i=0; i< parts.size(); ++i) {
      QueryAttr.WithPath d = parts.get(i);
      Expression ex = PathExpressionChain.parse(d.getPathName(), true);
      values[i] = ex.exec(new ExprExecCtxt(item));
    }
    return new MultiObjectValue(values);
  }

}


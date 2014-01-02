package org.pm4j.common.query;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.Expression;
import org.pm4j.common.expr.PathExpressionChain;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.common.util.CloneUtil;
import org.pm4j.common.util.collection.MultiObjectValue;


/**
 * Descriptor for an attribute that is defined by a set for individual fields.
 * <p>
 * A typical use case is a business key comprising multiple fields.
 * <p>
 * The path of this kind of attribute is <code>null</code> because the multi field attribute - in
 * difference to a composite attribute - usually has not common path for all
 * parts.
 *
 * @author olaf boede
 */
public class QueryAttrMulti extends QueryAttr {

  private static final long serialVersionUID = 1L;

  /** The set of attribute fields. */
  private List<QueryAttr> parts = new ArrayList<QueryAttr>();


  public QueryAttrMulti(String name) {
    this(name, name);
  }

  public QueryAttrMulti(String name, String title) {
    super(name, null, MultiObjectValue.class, title);
  }

  /**
   * Adds an attribute field set part.
   * <p>
   * Please notice that the pathName of the part is <b>not</b> relative to the main attribute.
   *
   * @param part the field set part to add.
   * @return the whole multi field attribute for inline usage.
   */
  public QueryAttrMulti addPart(QueryAttr part) {
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
  public QueryAttrMulti addPart(String path, Class<?> type) {
    return addPart(new QueryAttr(path, type));
  }

  //@Override
  public List<QueryAttr> getParts() {
    if (parts.isEmpty()) {
      throw new IllegalStateException("Multi field attribute '" + this +
          "'has no parts. Please add all parts before using the method getParts()");
    }
    return parts;
  }

  @Override
  public QueryAttrMulti clone() {
    QueryAttrMulti clone = (QueryAttrMulti) super.clone();
    clone.parts = CloneUtil.cloneList(this.getParts(), true);
    return clone;
  }

  @Override
  public String toString() {
    return super.toString() + parts.toString();
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
  public static MultiObjectValue makeValueObject(Object item, QueryAttrMulti fd) {
    List<QueryAttr> parts = fd.getParts();
    Object[] values = new Object[parts.size()];
    for (int i=0; i< parts.size(); ++i) {
      QueryAttr d = parts.get(i);
      Expression ex = PathExpressionChain.parse(new ParseCtxt(d.getPath()));
      values[i] = ex.exec(new ExprExecCtxt(item));
    }
    return new MultiObjectValue(values);
  }

}


package org.pm4j.common.query;

import java.util.Arrays;
import java.util.List;


/**
 * Descriptor for a virtual {@link QueryAttr} that is defined by a set for
 * individual fields.
 * <p>
 * Typical use case:<br>
 * You have to check for a set of fields if any of these matches a query
 * condition.
 * <p>
 * Example:<br>
 * We search for all records having the string 'hello' either in the name or in
 * the description field:
 *
 * <pre>
 * multiAttr = new QueryAttrMulti(QA_NAME, QA_DESCR);
 * expr = new QueryExprCompare(multiAttr, CompOpLike.class, &quot;%hello%&quot;);
 * found = QueryServiceUtil.findItems(queryService, expr, 100);
 * </pre>
 *
 * <p>
 * Please notice a difference to the base class {@link QueryAttr}:<br>
 * {@link QueryAttrMulti#getPath()} returns always <code>null</code> because the
 * multi-field attribute - in difference to a composite attribute - usually does
 * not have a common path part for its items.
 *
 * @author Olaf Boede
 */
public class QueryAttrMulti extends QueryAttr {

  private static final long serialVersionUID = 1L;

  /** The set of attribute fields. */
  private final List<QueryAttr> parts;

  /** Conditions for the attribute parts a by default or-combined. */
  private boolean orCombined = true;

  /**
   * @param name The name (and title) of the attribute.
   * @param parts The set of attributes represented by this virtual attribute.
   */
  public QueryAttrMulti(String name, QueryAttr... parts) {
    this(name, null, parts);
  }

  public QueryAttrMulti orCombined() {
    orCombined = true;
    return this;
  }

  public QueryAttrMulti andCombined() {
    orCombined = false;
    return this;
  }

  /**
   * @param name The name (and title) of the attribute.
   * @param title An optional kind of title string for this attribute.
   * @param parts The set of attributes represented by this virtual attribute.
   */
  public QueryAttrMulti(String name, String title, QueryAttr... parts) {
    super(name, null, QueryAttrMulti.class, title);
    if (parts.length == 0) {
      throw new IllegalStateException("Multi field attribute '" + this +
          "'has no parts. Please add all parts before using the method getParts()");
    }
    this.parts = Arrays.asList(parts);
  }

  public List<QueryAttr> getParts() {
    return parts;
  }

  /**
   * @return the orCombined
   */
  public boolean isOrCombined() {
    return orCombined;
  }

  @Override
  public String toString() {
    return getName() + parts.toString();
  }

}


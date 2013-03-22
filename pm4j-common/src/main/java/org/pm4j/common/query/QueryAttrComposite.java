package org.pm4j.common.query;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.util.lang.CloneUtil;


/**
 * Descriptor for an attribute that is composed of multiple fields (or attributes).
 * <p>
 * A typical use case is a business key comprising multiple fields.
 *
 * @author olaf boede
 */
public class QueryAttrComposite extends AttrDefinition implements QueryAttr.WithPath, QueryAttr.MultiPart {

  private static final long serialVersionUID = 1L;

  /** The set of attribute fields. */
  private List<QueryAttr.WithPath> parts = new ArrayList<QueryAttr.WithPath>();

  public QueryAttrComposite(String pathName, Class<?> type) {
    super(pathName, type);
  }

  public QueryAttrComposite(String name, String pathName, Class<?> type, String title) {
    super(name, pathName, type, title);
  }

  public QueryAttrComposite(String name, String pathName, Class<?> type) {
    super(name, pathName, type);
  }

  /**
   * Adds an attribute composition part.
   * <p>
   * Please notice that the pathName of the part is relative to the composite.
   *
   * @param part the composite part to add.
   * @return the whole composite for inline usage.
   */
  public QueryAttrComposite addPart(QueryAttr.WithPath part) {
    parts.add(part);
    return this;
  }

  /**
   * Adds an attribute composition part.
   * <p>
   * Please notice that the pathName of the part is relative to the composite.
   *
   * @param path the relative path of the composite part to add.
   * @param type the composite part type.
   * @return the whole composite for inline usage.
   */
  public QueryAttrComposite addPart(String path, Class<?> type) {
    return addPart(new AttrDefinition(path, type));
  }

  @Override
  public List<WithPath> getParts() {
    if (parts.isEmpty()) {
      throw new IllegalStateException("Composite attribute '" + this +
          "' has no parts. Please add all parts before using the method getParts()");
    }
    return parts;
  }

  /**
   * Provides the attribute parts. Each of them has a complete path that used the main attribute path as prefix.
   *
   * @return the set of of 'full path' attribute parts.
   */
  @Override
  public List<QueryAttr.WithPath> getPartsWithFullPath() {
    // uses getParts() to get a checked collection.
    List<QueryAttr.WithPath> parts = getParts();
    List<QueryAttr.WithPath> list = new ArrayList<QueryAttr.WithPath>(parts.size());

    String prefix = this.getPathName() + ".";
    for (QueryAttr.WithPath ad : parts) {
      list.add(cloneChildWithPathPrefix(prefix, ad));
    }

    return list;
  }

  @Override
  public QueryAttrComposite clone() {
    QueryAttrComposite clone = (QueryAttrComposite) super.clone();
    clone.parts = CloneUtil.cloneList(this.getParts(), true);
    return clone;
  }

  private QueryAttr.WithPath cloneChildWithPathPrefix(String prefix, QueryAttr.WithPath srcAttr) {
    QueryAttr a = srcAttr.clone();
    if (a instanceof AttrDefinition) {
      return ((AttrDefinition) srcAttr).cloneWithPathPrefix(prefix);
    } else if (a instanceof QueryAttrComposite) {
      return ((QueryAttrComposite) srcAttr).cloneWithPathPrefix(prefix);
    } else {
      throw new IllegalArgumentException("Usage as sub-attribute is only supported for single and composite attribues. Found type: " + srcAttr.getClass());
    }
  }

}


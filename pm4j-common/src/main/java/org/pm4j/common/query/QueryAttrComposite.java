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
public class QueryAttrComposite extends QueryAttr {

  private static final long serialVersionUID = 1L;

  /** The set of attribute fields. */
  private List<QueryAttr>     parts = new ArrayList<QueryAttr>();

  /**
   * @param pathName
   *          a path expression string that provides access to the attribute
   *          value.
   * @param type
   *          type of the attribute value.
   */
  public QueryAttrComposite(String pathName, Class<?> type) {
    this(pathName, pathName, type, null);
  }

  /**
   * @param name
   *          a path name that is unique within a set of filter/sort order
   *          definitions.
   * @param pathName
   *          a path expression string that provides access to the attribute
   *          value.
   * @param type
   *          type of the attribute value.
   * @param title
   *          the title string to display for this attribute. E.g. in filter
   *          dialogs.<br>
   *          If <code>null</code> is provided here, a table filter will try to
   *          get the title from a table column having the same name.
   */
  public QueryAttrComposite(String name, String pathName, Class<?> type, String title) {
    super(name, pathName, type, title);
  }

  /**
   * @param name
   *          a path name that is unique within a set of filter/sort order
   *          definitions.
   * @param pathName
   *          a path expression string that provides access to the attribute
   *          value.
   * @param type
   *          type of the attribute value.
   */
  public QueryAttrComposite(String name, String pathName, Class<?> type) {
    this(name, pathName, type, null);
  }

  /**
   * Adds an attribute composition part.
   * <p>
   * Please notice that the pathName of the part is relative to the composite.
   *
   * @param part the composite part to add.
   * @return the whole composite for inline usage.
   */
  public QueryAttrComposite addPart(QueryAttr part) {
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
    return addPart(new QueryAttr(path, type));
  }

  public List<QueryAttr> getParts() {
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
  public List<QueryAttr> getPartsWithFullPath() {
    // uses getParts() to get a checked collection.
    List<QueryAttr> parts = getParts();
    List<QueryAttr> list = new ArrayList<QueryAttr>(parts.size());

    String prefix = this.getPath() + ".";
    for (QueryAttr part : parts) {
      list.add(part.cloneWithPathPrefix(prefix));
    }

    return list;
  }

  @Override
  public QueryAttrComposite clone() {
    QueryAttrComposite clone = (QueryAttrComposite) super.clone();
    clone.parts = CloneUtil.cloneList(this.getParts(), true);
    return clone;
  }

}


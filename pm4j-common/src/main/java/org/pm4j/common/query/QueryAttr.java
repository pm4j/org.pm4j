package org.pm4j.common.query;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pm4j.common.exception.CheckedExceptionWrapper;

/**
 * Descriptor for a query attribute to filter or to sort by.
 *
 * @author olaf boede
 */
public class QueryAttr implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  private final String   name;
  private /*final*/ String path; // not final because of clone implementation. See cloneWithPathPrefix().
  private final Class<?> type;
  private String         title;
  private int            hashCode;

  /**
   * @param path
   *          a path name that is unique within a set of filter/sort order
   *          definitions.
   * @param type
   *          type of the attribute value.
   */
  public QueryAttr(String path, Class<?> type) {
    this(path, path, type, null);
  }

  /**
   * @param name
   *          a path name that is unique within a set of filter/sort order
   *          definitions.
   * @param path
   *          a path expression string that provides access to the attribute value.
   * @param type
   *          type of the attribute value.
   */
  public QueryAttr(String name, String path, Class<?> type) {
    this(name, path, type, null);
  }

  /**
   * @param name
   *          a path name that is unique within a set of filter/sort order
   *          definitions.
   * @param path
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
  public QueryAttr(String name, String path, Class<?> type, String title) {
    assert StringUtils.isNotBlank(name);
    assert type != null;

    this.name = name;
    this.path = path;
    this.type = type;
    this.title = title;
  }

  /**
   * A unique attribute name identifer. <br>
   * It usually corresponds to the name of the column displaying that attribute.
   *
   * @return the attribute name identifier.
   */
  public String getName() {
    return name;
  }

  /**
   * A convenient title string that may be used in generic dialogs that have to
   * display a title for this attribute. E.g. a generic filter-by dialog.
   * <p>
   * Applications that support internationalization may provide here a resource
   * key for the language specific title to display.
   *
   * @return the title string.
   */
  public String getTitle() {
    return title;
  }

  /**
   * A (dot separated) attribute path expression identifying the location of the
   * attribute within its bean.
   * <p>
   * Examples: <code>'id'</code> for a typical identifier attribute or
   * <code>subObject.name</code> for a path to an attribute within a sub-entity.
   *
   * @return the attribute path expression string. Is never <code>null</code>.
   */
  public String getPath() {
    return path;
  }

  /**
   * The type of values behind this attributes. Instances of that type can be
   * used in compare operations for this attribute.
   *
   * @return the attribute value type.
   */
  public Class<?> getType() {
    return type;
  }

  /**
   * @param title a title string used to display the attribute to the user.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the clone.
   */
  @Override
  public QueryAttr clone() {
    try {
      return (QueryAttr) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

  /** For internal usage: Clones this attribute. The clone gets the given path prefix. */
  /* package */ QueryAttr cloneWithPathPrefix(String prefix) {
    QueryAttr a = clone();
    a.path = prefix + this.path;
    return a;
  }

  @Override
  public String toString() {
    return path;
  }

  /**
   * The compare mechanism just compares the query relevant fields. The
   * {@link #title} is not considered.
   * <p>
   * It is done this way to support identification of identical query requests.<br>
   * The UI-title is currently not relevant or this aspect.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (! (obj instanceof QueryAttr)) {
      return false;
    }
    QueryAttr other = (QueryAttr)obj;
    return new EqualsBuilder().append(name, other.name).append(path, other.path).append(type, other.type).isEquals();
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode = new HashCodeBuilder(11, 47).append(name).append(path).append(type).toHashCode();
    }
    return hashCode;
  }

}

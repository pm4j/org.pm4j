package org.pm4j.common.query;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A data object that describes an attribute to filter or to sort by.
 *
 * @author olaf boede
 */
public class AttrDefinition implements Serializable {

  private static final long serialVersionUID = 1L;

  private String   title;
  private String   name;
  private String   pathName;
  private Class<?> type;

  /**
   * @param pathName
   *          a path name that is unique within a set of filter/sort order
   *          definitions.
   * @param type
   *          type of the attribute value.
   */
  public AttrDefinition(String pathName, Class<?> type) {
    this(pathName, pathName, type, null);
  }

  /**
   * @param name
   *          a path name that is unique within a set of filter/sort order
   *          definitions.
   * @param pathName
   *          a path expression string that provides access to the attribute value.
   * @param type
   *          type of the attribute value.
   */
  public AttrDefinition(String name, String pathName, Class<?> type) {
	  this(name, pathName, type, null);
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
  public AttrDefinition(String name, String pathName, Class<?> type, String title) {
    assert StringUtils.isNotBlank(name);
    assert StringUtils.isNotBlank(pathName);
    assert type != null;

    this.name = name;
    this.pathName = pathName;
    this.type = type;
    this.title = title;
  }

  /**
   * @return a path name that is unique within a set of filter/sort order
   *         definitions.
   */
  public String getName() {
    return name;
  }

  /**
   * @return a path name that is unique within a set of filter/sort order
   *         definitions.
   */
  public String getPathName() {
    return pathName;
  }

  /**
   * @return type of the attribute value.
   */
  public Class<?> getType() {
    return type;
  }

  /**
   * @return a title string used to display the attribute to to the user.
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title a title string used to display the attribute to to the user.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return pathName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (! (obj instanceof AttrDefinition)) {
      return false;
    }
    AttrDefinition other = (AttrDefinition)obj;
    return new EqualsBuilder().append(name, other.name).append(pathName, other.pathName).append(type, other.type).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(29, 17).append(name).append(pathName).append(type).toHashCode();
  }

}

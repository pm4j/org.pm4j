package org.pm4j.common.query;

import java.io.Serializable;
import java.util.List;

/**
 * Descriptor for a query attribute to filter or to sort by.
 *
 * @author olaf boede
 */
public interface QueryAttr extends Serializable, Cloneable {

  /**
   * A unique attribute name identifer. <br>
   * It usually corresponds to the name of the column displaying that attribute.<br>
   * In case
   *
   * @return the attribute name identifier.
   */
  String getName();

  /**
   * A convenient title string that may be used in generic dialogs that have to display
   * a title for this attribute. E.g. a generic filter-by dialog.
   * <p>
   * Applications that support internationalization may provide here a resource key
   * for the language specific title to display.
   *
   * @return the title string.
   */
  String getTitle();

  /**
   * @return the clone.
   */
  QueryAttr clone();

  /**
   * A conventional attribute having a path that referrs to the attribute
   * instance within its bean.<br>
   * It may be a simple attribute like an integer or a complex attribute (an
   * object containing other attributes).<br>
   * An attribute of type {@link QueryAttrMultiField} is not an attribute
   * {@link WithPath}, because it does not referr to a single attribute
   * instance.
   */
  public interface WithPath extends QueryAttr {

    /**
     * A (dot separated) attribute path expression identifying the location of the attribute
     * within its bean.
     * <p>
     * Examples: <code>'id'</code> for a typical identifier attribute or <code>subObject.name</code> for a path to an attribute
     * within a sub-entity.
     *
     * @return the attribute path expression string. Is never <code>null</code>.
     */
    String getPathName();

    /**
     *
     *
     * @return
     */
    Class<?> getType();

  }

  /**
   * An attribute containing multiple fields.<br>
   * Typical examples are business keys or unique constraints for views.
   */
  public interface MultiPart extends QueryAttr {

    /**
     * Provides the composite parts as a set of (relative) attributes.
     *
     * @return the composite sub-attribute set. The provided list contains at least a single item.
     */
    List<QueryAttr.WithPath> getParts();

    /**
     * Provides the composite parts as a set of attributes with full path identifiers containing
     * the path of the parent attribute too.
     *
     * @return the composite sub-attribute set. The provided list contains at least a single item.
     */
    List<QueryAttr.WithPath> getPartsWithFullPath();

  }

}

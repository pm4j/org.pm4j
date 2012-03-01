package org.pm4j.core.pm;

import java.util.Set;


/**
 * Basic interface for common presentation model functionality.
 *
 * @author olaf boede
 */
public interface PmObject extends Comparable<PmObject> {

  static final String STYLE_CLASS_REQUIRED = "required";
  static final String STYLE_CLASS_DISABLED = "disabled";
  static final String STYLE_CLASS_ERROR = "error";
  static final String STYLE_CLASS_WARN = "warn";
  static final String STYLE_CLASS_INFO = "info";

  /**
   * @return A title string for this item.
   */
  String getPmTitle();

  /**
   * Some applications decorate titles for specific items with some extra characters.
   * <p>
   * Example: A required field or a modified item may be decorated with an asterisk.
   * So that the attribute 'name' appears with the title 'Name *' in an data entry form.
   * <p>
   * That decorated title is provided by {@link #getPmTitle()}.
   * This method provides the undecorated title that may be used for generation of
   * messages such as: 'Please enter a value for field Name.'.
   * <p>
   * In addition the simple title may be specified separately in the resource file
   * with the postfix '.shortTitle'.
   * <p>
   * Example resource definition for explicit short title definition:
   *  <pre>
   *    myPm.oriSelection=Specify the Original
   *    myPm.oriSelection.shortTitle=Original
   *  </pre>
   *
   * @return The undecorated title string for this item.
   */
  String getPmShortTitle();

  /**
   * @return The tool tip string of this item or <code>null</code> when there
   *         is no tool tip.
   */
  String getPmTooltip();

  /**
   * @return Resource path of the icon to show for this PM or <code>null</code>
   *         if no icon should be displayed.
   */
  String getPmIconPath();

  /**
   * Indicates if it is allowed to call the {@link #setPmTitle(Object, String)}
   * method for the given item.
   *
   * @return <code>true</code> when it is allowed to call
   *         {@link #setTitle(Object, Object, String)}.
   */
  boolean canSetPmTitle();

  /**
   * An interface for 'in place editing' of node titles.
   * <p>
   * That might be a useful feature for title string editors of tree views where
   * a user can simply click on a title and change it.
   *
   * @param eventSource
   * @param titleString
   */
  void setPmTitle(String titleString);

  /**
   * @return The conversation context of this model element. Never <code>null</code>.
   */
  PmConversation getPmConversation();

  /**
   * @return The parent object of this PM.<br>
   *         Is <code>null</code> for the root session only.
   */
  PmObject getPmParent();

  /**
   * Setter for dependency injection framework support.
   * <p>
   * Should only be used if the instance has not yet an
   * assigned <code>pmParent</code>.
   *
   * @param pmParent The context, this PM was created in.
   */
  void setPmParent(PmObject pmParent);

  /**
   * @return <code>true</code> when the element may be displayed.
   */
  boolean isPmVisible();

  /**
   * The visibility state is usually defined by some UI logic code of the
   * implementing class.
   * <p>
   * In some cases it is useful to control the visibility of an UI element by
   * calling this setter.
   *
   * @param visible
   *          The new local visibility state. If set to <code>null</code>,
   *          the visibility provided by <code>isPmVisibleImpl()</code>
   *          will be applied.
   */
  void setPmVisible(Boolean visible);

  /**
   * @return <code>true</code> when the element should be rendered as enabled
   *         UI element.
   */
  boolean isPmEnabled();

  /**
   * @param enabled The new local enabled state.
   */
  void setPmEnabled(Boolean enabled);

  /**
   * @return <code>true</code> when the values of this PM may not be
   *         changed.
   */
  boolean isPmReadonly();

  /**
   * A unique short name of this item within its parent scope.
   *
   * @return The attribute/command/session name.<br>
   *         Is <code>null</code> for models that are created dynamically
   *         (e.g. elements of a list).
   */
  String getPmName();

  /**
   * The unique relative name of the pm within its PM-composite.
   * <ul>
   *   <li>The composite root element provides an empty string.<li>
   *   <li>The composite children of the root element provide their names.<br>
   *       E.g. 'firstName', 'lastName', 'address'.</li>
   *   <li>Composite children of composite children provide composed names.<br>
   *       E.g. 'address_street', 'alternateAddress_street'.</li>
   * </ul>
   * This kind of name may be used in html forms to identify the fields to enter.
   *
   * @return The unique name within the PM-composite.<br>
   *         An empty string for non-PM-composite children. FIXME: Derzeit nicht!
   */
  String getPmRelativeName();


  /**
   * Checks if there is an error message related to this PM.
   * <p>
   * Is often used to check if the UI should show a not valid attribute with a
   * special style.
   *
   * @return <code>true</code> if there is an error (may be a validation error)
   *         related this PM.
   */
  boolean isPmValid();

  /**
   * A PM may provide CSS style classes to provide layout hints.
   * <p>
   * The set of style classes may change whenever the state of the PM changes.
   * It usually contains an 'error' class if there is an error message related
   * to this PM.
   *
   * @return The set of CSS style classes for this PM.
   */
  Set<String> getPmStyleClasses();

  /**
   * Returns a application specific property that may have been defined using
   * {@link #setPmProperty(String, Object)}.
   *
   * @return The property value or <code>null</code> if the property is not defined.
   */
  Object getPmProperty(String propName);

  /**
   * TODO
   * @param propName
   * @param value
   */
  void setPmProperty(String propName, Object value);

  /**
   * @param visitor A visitor to call back.
   */
  void accept(PmVisitor visitor);

  /**
   * PMs may exist in sortable contexts. (For example a cell in a table.)<br>
   * This method allows to perform the required sort operations in these
   * contexts.
   *
   * @param otherPm
   *          The PM to compare.
   * @return The usual {@link Comparable} result: a negative integer, zero, or a
   *         positive integer as this object is less than, equal to, or greater
   *         than the specified object.
   */
  @Override
  int compareTo(PmObject otherPm);

}

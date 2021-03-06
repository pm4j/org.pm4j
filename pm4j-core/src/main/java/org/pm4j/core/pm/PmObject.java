package org.pm4j.core.pm;

import java.util.List;
import java.util.Set;

import org.pm4j.core.pm.impl.PmAttrUtil;


/**
 * Basic interface for common presentation model functionality.
 *
 * @author olaf boede
 */
public interface PmObject extends Comparable<PmObject> {

  static final String STYLE_CLASS_REQUIRED = "required";
  static final String STYLE_CLASS_ERROR = "error";
  static final String STYLE_CLASS_WARN = "warn";
  static final String STYLE_CLASS_INFO = "info";

  /**
   * @return A title string for this item.
   */
  String getPmTitle();

  /**
   * Some applications short titles for specific usages. E.g. in table headers.
   * <p>
   * The short title may be specified in a resource file by using the postfix
   * '_shortTitle'.
   * <p>
   * Example resource definition for explicit short title definition:
   * 
   * <pre>
   *    myPm.oriSelection=Specify the Original
   *    myPm.oriSelection_shortTitle=Original
   * </pre>
   *
   * @return The short title string for this item. <br>
   *         It no specific short title is defined, the result of
   *         {@link #getPmTitle()}.
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
   *          The new local visibility state.
   * @deprecated Please define the logic in isPmVisibleImpl().
   */
  @Deprecated
  void setPmVisible(boolean visible);

  /**
   * @return <code>true</code> when the element should be rendered as enabled
   *         UI element.
   */
  boolean isPmEnabled();

  /**
   * @param enabled The new local enabled state.
   * @deprecated Please define the logic in isPmEnabledImpl().
   */
  @Deprecated
  void setPmEnabled(boolean enabled);

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
   * @return <code>false</code> if there is an error (may be a validation error)
   *         related this PM.
   */
  boolean isPmValid();

  /**
   * Indicates a value change. E.g. a value was entered, but not yet saved by
   * the user.
   * <p>
   * The changed state usually gets cleared on execution of a {@link PmCommand}
   * that required valid values.
   *
   * @return <code>true</code> if the value of this PM or one of its composite
   *         children was changed.
   */
  boolean isPmValueChanged();

  /**
   * Marks the PM manually a changed or unchanged.
   * <p>
   * Setting the PM to unchanged will be propagated recursively to all child PMs.
   *
   * @param changed the new changed state.
   */
  void setPmValueChanged(boolean changed);

  /**
   * Resets the values of editable attributes to their default value.
   * <p>
   * Switches the changed state to <code>false</code>.
   * <p>
   * This method has some limitations. In more complex scenarios the value of
   * one field may control the enabled state of a second field.<br>
   * In that case it may happen that the value of the second field will be reset.
   * <p>
   * Please consider using {@link PmAttrUtil#resetBackingValuesToDefault(PmObject)}.
   * It does not have that editable state limitation.
   */
  void resetPmValues();

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
   * PM functionality to support tree views.
   *
   * @return The set of sub-nodes to display for this item.
   */
  List<? extends PmObject> getPmChildNodes();

  /**
   * @return <code>true</code> if this instance should be presented as a tree
   *         leaf node.
   */
  boolean isPmTreeLeaf();

  /**
   * Returns an application specific property that may have been defined using
   * {@link #setPmProperty(String, Object)}.
   *
   * @param propName the property identifier.
   * @return the property value or <code>null</code> if the property is not defined.
   */
  Object getPmProperty(String propName);

  /**
   * Assigns an application specific property to this PM.
   *
   * @param propName the property identifier.
   * @param value the property value.
   */
  void setPmProperty(String propName, Object value);

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

  /**
   * Validates this PM.<br>
   * Generates error messages in case of validation problems.<br>
   * Fires {@link PmEvent#VALIDATION_STATE_CHANGE} events in case of a change
   * of the valid-state.
   */
  // TODO olaf: move public interface to validation API. Change to protected implementation method.
  void pmValidate();

  /**
   * A predicate interface used for filtering PMs in visitor contexts.
   */
  public interface PmMatcher {
    /**
     * @param pm The PM to check.
     * @return <code>true</code> if the given PM matches the condition.
     */
    boolean doesMatch(PmObject pm);
  }

}

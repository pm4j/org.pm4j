package org.pm4j.core.pm;

import java.io.Serializable;

import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.pm.annotation.PmAttrCfg;




/**
 * Presentation model for attributes.
 *
 * @param <T> The attribute value type.
 *
 * @author olaf boede
 */
public interface PmAttr<T> extends PmObject, PmDataInput {

  /**
   * @return The attribute value.
   */
  T getValue();

  /**
   * Setter without explicit event source.
   *
   * @param value
   *          The new value.
   */
  void setValue(T value);

  /**
   * Sets the value with a string without explicit event source.
   *
   * @param text
   *          The new value as string.
   */
  void setValueAsString(String text);

  /**
   * @return The string for the current value.
   */
  String getValueAsString();

  /**
   * Some attribute types, such as enums, may provide localized values.
   * <p>
   * It is sometimes useful to display in the UI rather 'Yes' and 'No' instead
   * of 'TRUE' and 'FALSE'. For such situations implementations this method may
   * provide the expected human readable string.<br>
   * Another use case: An attribute that references a 'User' object might provide here
   * the name of the referenced user.
   *
   * @return The localized string value for the current value.
   */
  String getValueLocalized();

  /**
   * @return The maximum string representation length.
   */
  int getMaxLen();

  /**
   * @return The minimal string representation length.
   */
  int getMinLen();

  /**
   * Reset the value to <code>null</code> or the optional default value definition.
   */
  @Override
  void resetPmValues();

  /**
   * Indicates an attribute value change.
   * <p>
   * Will be cleared when
   * <ul>
   *   <li>a command that 'requiresValidValues' (e.g. a cmdSave command) gets
   *       successfully executed on the element the contains this attribute.</li>
   *   <li>{@link #resetPmValues(Object)} gets called.</li>
   *   <li>{@link #setPmValueChanged(Object, boolean)} gets called.</li>
   * </ul>
   *
   * @return <code>true</code> if the attribute value was changed in the live
   *         time of the PM (or since the previous value or changed state
   *         reset).
   */
  boolean isPmValueChanged();

  /**
   * Sets an attribute explicitly to a change or unchanged state.
   * <p>
   * Is usually used for the following scenarios:
   * <ul>
   * <li>To accept the current value as the new 'original' attribute value.</li>
   * <li>To mark the PM explicitly as changed. This is usually done by
   * {@link #setValue(Object)}. But in special cases {@link #setValue(Object)}
   * is not used to modify the presented content. (Example: A command changes
   * the domain objects behind the PM directly.) For that situation an explicit
   * call to this method may inform the UI and the PM logic about the new
   * changed state.</li>
   * </ul>
   * An event with the type-flag {@link PmEvent#VALUE_CHANGED_STATE_CHANGE}
   * will be fired if the call changed this state.
   *
   * @param <code>true</code> marks the attribute as changed.<br>
   *        <code>false</code> marks the attribute as unchanged.
   */
  void setPmValueChanged(boolean newChangedState);

  /**
   * Provides the PM for the attribute value options the user may choose from.
   * <p>
   * Attributes that represent enums or references to other objects usually
   * provide an option set.
   * <p>
   * Most PM view components use the string value of the provided option IDs to
   * set the value of the attribute using {@link #setValueAsString(String)}.<br>
   * (For multiple value attribute types the PM view components usually use the
   * 'setListValueAsString' method signatures to set the selected option values.)
   *
   * @return The attribute options.<br>
   *         In case of no options an empty option set.
   */
  PmOptionSet getOptionSet();

  /**
   * @return <code>true</code> if the attribute value needs to be set to a value
   *         that is not <code>null</code>.
   */
  boolean isRequired();

  /**
   * @return <code>true</code> if the attribute value may not be changed.
   */
  boolean isPmReadonly();

  /**
   * Returns a localized format string for attribute values provided as strings.<br>
   * The localized format string is based on a resource key which may be defined
   * as follows:
   * <ol>
   *   <li>You may specify it using the annotation
   *       {@link PmAttrCfg#formatResKey()}.</li>
   *   <li>You may define it within the resource file, using a resource key with a
   *       '_format' postfix.<br>
   *       Example: <code>myPm.myNumber_format=#,##0.##</code></li>
   *   <li>For some types such as {@link PmAttrDate} and {@link PmAttrDouble} you
   *       may specify default formats.<br>
   *       (Attribute classes may specify this by implementing
   *       <code>getFormatDefaultResKey()</code>.)<br>
   *       See: {@link PmAttrDate#RESKEY_DEFAULT_FORMAT_PATTERN} and
   *       {@link PmAttrDouble#RESKEY_DEFAULT_FORMAT_PATTERN}.</li>
   * </ol>
   * The resource key gets evaluated in the sequence specified above. The first
   * resource key match wins.
   *
   * @return A localized format string or <code>null</code> if there is no
   *         format definition.
   */
  String getFormatString();

  /**
   * Converts single values between its attribute type representation and {@link String}
   * or {@link Serializable} representation.
   *
   * @param <T>
   *          The type of the items to convert.
   */
  public interface Converter<T> {

    T stringToValue(PmAttr<?> pmAttr, String s) throws PmConverterException;

    String valueToString(PmAttr<?> pmAttr, T v);

    T serializeableToValue(PmAttr<?> pmAttr, Serializable s) throws PmConverterException;

    Serializable valueToSerializable(PmAttr<?> pmAttr, T v);

  }

}

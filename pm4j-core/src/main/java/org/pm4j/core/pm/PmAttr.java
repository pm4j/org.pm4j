package org.pm4j.core.pm;

import java.io.Serializable;

import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.impl.PmAttrImpl;




/**
 * Presentation model for attributes. It adds presentation logic aspects for
 * attribute value handling to the base interface {@link PmObject}.
 * <p>
 * An attribute represents a data item that is usually represented as a data
 * entry field on a form.
 * <p>
 * Details on how to implement application specific attribute logic see
 * {@link PmAttrImpl}.
 *
 * @param <T> The attribute value type provided for the view.
 *
 * @author olaf boede
 */
public interface PmAttr<T> extends PmObject, PmDataInput {

	/**
	 * Provides the actual value of the attribute.
	 * <p>
	 * For details on how to provide values see: {@link PmAttrImpl#getValue()}
	 *
	 * @return The attribute value.
	 */
	T getValue();

	/**
	 * @param value
	 *          The new value.
	 */
	void setValue(T value);

	/**
	 * @return The string for the current value.
	 */
	String getValueAsString();

	/**
	 * Sets the value with a string value.
	 *
	 * @param text
	 *          The new value as string.
	 */
	void setValueAsString(String text);

	/**
	 * Provides the set of value options the user may choose from.
	 * <p>
	 * Some attributes (e.g. attributes representing enums or references to other objects)
	 * usually provide an option set.
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
	 * @return <code>true</code> if the attribute value needs to be set to a value
	 *         that is not <code>null</code>.
	 */
	boolean isRequired();

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
    @Override
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
    @Override
	void setPmValueChanged(boolean newChangedState);

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
	 *       {@link PmAttrNumber#RESKEY_DEFAULT_FLOAT_FORMAT_PATTERN}.</li>
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
	interface Converter<T> {

		T stringToValue(PmAttr<?> pmAttr, String s) throws PmConverterException;

		String valueToString(PmAttr<?> pmAttr, T v);

		T serializeableToValue(PmAttr<?> pmAttr, Serializable s) throws PmConverterException;

		Serializable valueToSerializable(PmAttr<?> pmAttr, T v);

	}

  /**
   * Interface for the internally generated command that gets generated for each value change operation.
   * <p>
   * This command based value change implementation provides the basis for the features: undo and value change
   * command decorator.
   *
   * @param <T_VALUE> The attribute value type.
   */
	interface ValueChangeCommand<T_VALUE> extends PmCommand {
    PmAttr<T_VALUE> getPmAttr();
    T_VALUE getNewValue();
    T_VALUE getOldValue();
	}

}

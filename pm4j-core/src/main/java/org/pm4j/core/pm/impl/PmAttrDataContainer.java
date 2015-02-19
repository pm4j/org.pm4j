package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmAttr;

/**
 * A container that holds the optional attribute values (local value, cached value, invalid value etc.).
 * Is will only be instantiated when there one of its parts is used.
 * <p>
 * Usual attributes that represent values of a backing bean and don't have such information
 * don't have that instance an cmdSave this way some memory.
 *
 * @author olaf boede
 *
 * @param <T_PM_VALUE>
 * @param <T_BEAN_VALUE>
 */
class PmAttrDataContainer <T_PM_VALUE, T_BEAN_VALUE> {

  /**
   * The value stored locally within the attribute. Used for attributes that
   * are not bound to another data storage. E.g. to a backing bean attribute.
   */
  Object localValue;

  /**
   * Container for invalid values that the user should correct.
   * Is only filled as long as there is a not validated value.
   */
  SetValueContainer<T_PM_VALUE> invalidValue;

  /**
   * If the {@link PmAttr} is unchanged, this field references the
   * {@link PmAttrBase#UNCHANGED_VALUE_INDICATOR}. Otherwise it contains the
   * original value of the attribute.
   */
  Object originalValue = PmAttrBase.UNCHANGED_VALUE_INDICATOR;

  /**
   * Only used in case of a value cache. <br>
   * Is of type <code>T_PM_VALUE</code>. It has only {@link Object} type to
   * allow a replacement with a <code>null</code> value marker object.
   */
  Object cachedValue;

  /**
   * Only used in case of cached options.
   */
  Object cachedOptionSet;

}

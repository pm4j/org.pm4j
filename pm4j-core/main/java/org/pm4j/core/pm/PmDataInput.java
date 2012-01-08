package org.pm4j.core.pm;

/**
 * Interface for data input related presentation models.
 *
 * @author olaf boede
 */
public interface PmDataInput extends PmObject {

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
   * Resets the values of editable attributes to their default value.
   */
  void resetPmValues();

  /**
   * @return <code>true</code> when each pm value modification will be applied
   *         to an edit buffer only. Changed values will be applied when
   *         {@link #commitBufferedPmChanges()} is called.
   */
  boolean isBufferedPmValueMode();

  /**
   * Commits all changed values to the data store behind the presentation model.
   */
  void commitBufferedPmChanges();

  /**
   * Clears all uncommitted changes.
   * <p>
   * Does not change values of the data store behind the presentation model.
   */
  void rollbackBufferedPmChanges();

  /**
   * Validates this PM.<br>
   * Generates error messages in case of validation problems.<br>
   * Fires {@link PmEvent#VALIDATION_STATE_CHANGE} events in case of a change
   * of the valid-state.
   */
  void pmValidate();

}

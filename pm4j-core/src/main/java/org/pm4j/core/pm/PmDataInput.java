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

}

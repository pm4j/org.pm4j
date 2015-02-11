package org.pm4j.core.pm;


/**
 * For internal use only!
 * <p>
 * Interface for data input related presentation models.
 *
 * @author olaf boede
 */
public interface PmDataInput extends PmObject {

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

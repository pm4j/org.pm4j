package org.pm4j.common.util.collection;

import java.io.Serializable;
import java.util.Arrays;

/**
 * An object that provides a value (or identity) from an array of values.
 * <p>
 * It computes its {@link #equals(Object)} and {@link #hashCode()} results based on
 * the corresponding item results.
 *
 * @author olaf boede
 * @deprecated It's in an experimental state. Please don't use it.
 */
public class MultiObjectValue implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Object[] parts;

  /**
   * Creates the instance based on the passed items.
   *
   * @param items the set of items. At least a single item should be provided.
   */
  public MultiObjectValue(Object... items) {
    assert items.length > 0 : "At least a single item should be provided.";
    this.parts = items;
  }

  /**
   * Provides the parts passed to the constructor.
   *
   * @return the parts.
   */
  public Object[] getParts() {
    return parts;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    return Arrays.equals(parts, ((MultiObjectValue)obj).parts);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(parts);
  }

  @Override
  public String toString() {
    return Arrays.toString(parts);
  }

}

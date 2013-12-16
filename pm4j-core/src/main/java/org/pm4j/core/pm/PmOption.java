package org.pm4j.core.pm;

import java.io.Serializable;


/**
 * An option for an attribute value.
 * <p>
 * The <code>toString</code> method provides the title of the option.
 */
public interface PmOption {

  /**
   * @return The identifier of the option to select.
   */
  Serializable getId();

  /**
   * @return The identifier of the option to select as string.
   */
  String getIdAsString();

  /**
   * @return <code>true</code> when the option can be used for setting a value.
   */
  boolean isEnabled();

  /**
   * Provides an optional resolved attribute value behind this option.
   *
   * @return <code>null</code> or the value that may be used to set the value of the attribute <code>null</code>.
   */
  <T> T getValue();

  /**
   * Provides an optional resolved attribute backing value behind this option.
   *
   * @return <code>null</code> or the backing value that may be used to set the value of the attribute <code>null</code>.
   */
  <T> T getBackingValue();

  /**
   * @return The localized title.
   */
  String getPmTitle();
}

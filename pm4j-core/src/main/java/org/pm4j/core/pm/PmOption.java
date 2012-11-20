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
   * Provides an optional object behind this option.
   * <p>
   * Options that represent references to other objects can provide here the
   * presentation model of the referenced object.
   * <p> 
   * Other option implementations e.g. for numeric values might provide a number here.
   * Have a look at the concrete option provider documentation for that.
   * 
   * @return The object behind the option or <code>null</code>.
   */
  <T> T getValue();
  
  /**
   * @return The localized title.
   */
  String getPmTitle();
}

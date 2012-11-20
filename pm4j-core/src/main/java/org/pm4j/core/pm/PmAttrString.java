package org.pm4j.core.pm;


public interface PmAttrString extends PmAttr<String> {

  /**
   * @return <code>true</code> if the attribute should be represented using a
   *         kind of text area control.
   */
  boolean isMultiLine();

}

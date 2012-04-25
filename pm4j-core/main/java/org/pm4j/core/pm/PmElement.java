package org.pm4j.core.pm;

import java.io.Serializable;

/**
 * A presentation model that supports a set of attributes.
 * <p>
 * Such models and their attributes are intended to be visualized in form displays.
 *
 * @author olaf boede
 */
public interface PmElement extends PmObject, PmDataInput, PmTreeNode {

  /**
   * @return A key that identifies the element within the session.
   */
  Serializable getPmKey();

}

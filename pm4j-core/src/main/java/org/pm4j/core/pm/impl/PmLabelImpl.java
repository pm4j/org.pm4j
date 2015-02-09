package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmLabel;
import org.pm4j.core.pm.PmObject;

/**
 * A label implementation.
 *
 * @deprecated Please use {@link PmObjectBase}.
 *
 * @author olaf boede
 */
@Deprecated
public class PmLabelImpl extends PmObjectBase implements PmLabel {

  /**
   * @param pmParent
   *          The parent element or session context to get some data like
   *          the current language.<br>
   *          It also provides the resource loading context.
   */
  public PmLabelImpl(PmObject pmParent) {
    super(pmParent);
  }

}

package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmObject;

public abstract class PmDataInputBase extends PmObjectBase implements PmDataInput {

  public PmDataInputBase(PmObject parentPm) {
    super(parentPm);
  }

  // ======== Buffered data input support ======== //

  @Override
  public boolean isBufferedPmValueMode() {
    return getPmConversation().isBufferedPmValueMode();
  }

  @Override
  public void rollbackBufferedPmChanges() {
  }

  @Override
  public void commitBufferedPmChanges() {
  }

}

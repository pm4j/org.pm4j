package org.pm4j.core.pm.serialization;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.impl.PmCommandImpl;

public class PmContentChangeCommand extends PmCommandImpl {

  private PmContentContainer content;

  public PmContentChangeCommand(PmObject pmParent, PmContentContainer content) {
    super(pmParent);
    this.content = content;
  }

  @Override
  protected void doItImpl() {
    if (content != null) {
      PmVisitorApi.visit(getPmParent(), new PmContentSetVisitorCallBack(content));
    }
  }
}

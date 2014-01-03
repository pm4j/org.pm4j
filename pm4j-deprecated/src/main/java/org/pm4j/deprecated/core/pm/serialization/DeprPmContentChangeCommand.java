package org.pm4j.deprecated.core.pm.serialization;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.impl.PmCommandImpl;

public class DeprPmContentChangeCommand extends PmCommandImpl {

  private DeprPmContentContainer content;

  public DeprPmContentChangeCommand(PmObject pmParent, DeprPmContentContainer content) {
    super(pmParent);
    this.content = content;
  }

  @Override
  protected void doItImpl() {
    if (content != null) {
      PmVisitorApi.visit(getPmParent(), new DeprPmContentSetVisitorCallBack(content));
    }
  }
}

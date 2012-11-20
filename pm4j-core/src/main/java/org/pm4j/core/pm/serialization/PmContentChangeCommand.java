package org.pm4j.core.pm.serialization;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmCommandImpl;

public class PmContentChangeCommand extends PmCommandImpl {

  private PmContentContainer content;
  
  public PmContentChangeCommand(PmObject pmParent, PmContentContainer content) {
    super(pmParent);
    this.content = content;
  }
  
  @Override
  public PmCommand doIt() {
    if (content != null) {
      PmContentSetVisitor v = new PmContentSetVisitor(this, content);
      getPmParent().accept(v);
      getPmConversation().getPmCommandHistory().commandDone(this);
    }

    return super.doIt();
  }
  
}

package org.pm4j.swt.pb.standards;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.standards.PmInputDialog;
import org.pm4j.swt.testtools.SwtTestShell;

public class PbInputDialogCheck {

  public static void main(String[] args) {
    SwtTestShell s = new SwtTestShell(500, 350, "jFace Input Dialog Demo");
    new PbInputDialog().build(s.getShell(), getPmInputDialog());
    s.show();
  }

  private static PmInputDialog getPmInputDialog() {
    PmConversation pmConversation = new PmConversationImpl();
    return new PmInputDialog(pmConversation);
  }

}

package org.pm4j.swing.pb.standards;

import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.standards.PmConfirmDialog;
import org.pm4j.standards.PmConfirmedCommand;

public class PbConfirmDialogCheck {

  public static class TestSession extends PmConversationImpl {
    @PmTitleCfg(title="Hello World")
    public final PmConfirmedCommand cmdToConfirm = new PmConfirmedCommand(this) {
      protected void doItImpl() {
        System.out.println("+++ cmdToConfirm executed +++");
      };
    };
  }

  public static void main(String[] args) {
    TestSession session = new TestSession();
    PmConfirmDialog confirmDialog = new PmConfirmDialog(session.cmdToConfirm);
    new PbConfirmDialog().build(null, confirmDialog);
  }
}

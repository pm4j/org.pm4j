package org.pm4j.swt.pb.standards;

import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.standards.PmConfirmDialog;
import org.pm4j.standards.PmConfirmedCommand;
import org.pm4j.swt.testtools.SwtTestShell;

public class PbConfirmDialogCheck {

  public static class TestSession extends PmConversationImpl {
    @PmTitleCfg(title="Hello World")
    public final PmConfirmedCommand cmdToConfirm = new PmConfirmedCommand(this);
  }

  public static void main(String[] args) {
    SwtTestShell s = new SwtTestShell(250, 350, PbConfirmDialog.class.getSimpleName() + " Demo");

    TestSession session = new TestSession();
    PmConfirmDialog confirmDialog = new PmConfirmDialog(session.cmdToConfirm);
    new PbConfirmDialog().build(s.getShell(), confirmDialog);
  }
}

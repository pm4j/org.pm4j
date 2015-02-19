package org.pm4j.swing.pb.standards;

import java.awt.Frame;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.standards.PmConfirmedCommand;
import org.pm4j.swing.pb.PbJButton;

public class PbConfirmedCommandCheck {

  public static class TestSession extends PmConversationImpl {
    @PmTitleCfg(title="A confirmed command")
    public final PmConfirmedCommand cmdToConfirm = new PmConfirmedCommand(this) {
      protected void doItImpl() {
        System.out.println("+++ cmdToConfirm executed +++");
      }
    };
  }

  public static void main(String[] args) {
    TestSession session = new TestSession();
    session.setPmLocale(Locale.ENGLISH);

    JDialog dlg = new JDialog((Frame) null, PbConfirmedCommandCheck.class.getSimpleName(), true);
    JPanel contentPane = new JPanel();
    
    new PbJButton().build(contentPane, session.cmdToConfirm);

    dlg.setContentPane(contentPane);
    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dlg.setSize(450, 200);
    dlg.setLocationRelativeTo(null); // centered
    dlg.setVisible(true);
  }
}

package org.pm4j.swing.pb.standards;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pb.PbFactoryBase;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.standards.PmConfirmDialog;
import org.pm4j.swing.pb.PbJButton;
import org.pm4j.swing.pb.PbJLabel;

public class PbConfirmDialog
   extends PbFactoryBase<JDialog, Object, PmConfirmDialog> {


  // XXX olaf: quick and dirty mix of layout and logic:
  @Override
  public JDialog makeView(Object parent, PmConfirmDialog pm) {
    final JDialog dialog = makeJDialogForControl((Component)parent);
    dialog.setModal(true);

    dialog.setTitle(pm.getPmTitle());
    JPanel contentPane = new JPanel();
    dialog.setContentPane(contentPane);

    // TODO: add some layout information.
    contentPane.setLayout(new GridLayout(2, 1));

    new PbJLabel().build(contentPane, pm.dialogMessage);

    JPanel buttonPanel = new JPanel();
    contentPane.add(buttonPanel);

    new PbJButton().build(buttonPanel, pm.cmdYes);
    new PbJButton().build(buttonPanel, pm.cmdNo);

    PmEventListener closeListener = new PmEventListener() {
      @Override
      public void handleEvent(PmEvent event) {
        dialog.dispose();
      }
    };

    PmEventApi.addPmEventListener(pm.cmdYes, PmEvent.EXEC_COMMAND, closeListener);
    PmEventApi.addPmEventListener(pm.cmdNo, PmEvent.EXEC_COMMAND, closeListener);

    dialog.pack();

    return dialog;
  }

  @Override
  protected void bindImpl(JDialog view, PmConfirmDialog pm) {
    view.setVisible(true);
  }

  private JDialog makeJDialogForControl(Component comp) {
    JDialog dlg;
    if (comp == null)
      dlg = new JDialog();
    else if (comp instanceof Dialog)
      dlg = new JDialog((Dialog)comp);
    else if (comp instanceof Frame)
      dlg = new JDialog((Frame)comp);
    else if (comp instanceof Window)
      dlg = new JDialog((Window)comp);
    else if (comp instanceof JComponent)
      dlg = makeJDialogForControl(((JComponent)comp).getParent());
    else
      throw new PmRuntimeException("Unsupported dialog owner class: " + comp.getClass());

    dlg.setLocationRelativeTo(null); // centered

    return dlg;
  }

}

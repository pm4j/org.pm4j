package org.pm4j.swing.pb.internal.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.pm4j.core.pb.PbFactoryBase;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.swing.pb.standards.PbConfirmDialog;

/**
 * Listens for selection events.<br>
 * Executes a command that is bound to the widget.<br>
 * Performs the required command postprocessing steps, such as
 * displaying a dialog, if required.
 */
public class CommandExecListener implements ActionListener {

  /**
   * The listener is stateless and may be used as a singleton.
   */
  public static final CommandExecListener INSTANCE = new CommandExecListener();
  
  @Override
  public void actionPerformed(ActionEvent e) {
    PmCommand cmd = PbFactoryBase.getBoundPm(e.getSource());
    PmObject pm = cmd.doItReturnNextDlgPm();
    if (pm != null) {
      // TODO: add a view mapping here...
//        PmSwtUtil.buildView(e.widget, pm);
      new PbConfirmDialog().build(e.getSource(), pm);
    }
  }

}

package org.pm4j.swt.pb.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.pm4j.core.pb.PbFactoryBase;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.swt.pb.standards.PbConfirmDialog;

/**
 * Listens for selection events.<br>
 * Executes a command that is bound to the widget.<br>
 * Performs the required command post processing steps, such as
 * displaying a dialog, if required.
 *
 * @author olaf boede
 */
public class CommandExecListener extends SelectionAdapter {

  /**
   * The listener is stateless and may be used as a singleton.
   */
  public static final CommandExecListener INSTANCE = new CommandExecListener();

  @Override
  public void widgetSelected(SelectionEvent e) {
    PmCommand cmd = PbFactoryBase.getBoundPm(e.widget);
    PmObject pm = cmd.doItReturnNextDlgPm();
    if (pm != null) {
      // FIXME: add a view mapping here...
//        PmSwtUtil.buildView(e.widget, pm);
      new PbConfirmDialog().build(e.widget, pm);
    }
  }

}

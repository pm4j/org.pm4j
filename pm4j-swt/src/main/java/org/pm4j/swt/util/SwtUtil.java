package org.pm4j.swt.util;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.pm4j.core.exception.PmRuntimeException;

/**
 * Some simple SWT helper methods.
 * 
 * @author olaf boede
 */
public abstract class SwtUtil {

  /**
   * Shows a simple OK message box.
   * 
   * @param shell
   *          The shell that handles the dialog.
   * @param title
   *          The the text shown in the dialog title bar.
   * @param message
   *          The text shown in the dialog content area.
   * @return The ID of the used button (@link {@link SWT#OK}).
   */
  public static int infoBox(Shell shell, String title, String message) {
    MessageBox mb = new MessageBox(shell);
    mb.setText(title);
    mb.setMessage(message);
    return mb.open();
  }

  /**
   * Redisplays a control.<br>
   * Single-line helper that handles the display issues of controls with changed
   * content.
   * 
   * @param <T>
   *          The type of the control.
   * @param control
   *          The control to redisplay.
   * @return The control again for chained operations.
   */
  public static <T extends Control> T reDisplay(T control) {
    Point p = control.getSize();
    control.pack();
    control.setSize(p);
    return control;
  }

  /**
   * Extracts the SWT shell from a given SWT object.
   * 
   * @param swtObj
   *          The SWT object to analyze.
   * @return The found shell.
   * @throws Pm4jRuntimeException
   *           if this method was not able to get the shell.
   */
  public static Shell getShellFromSWTObj(Object swtObj) {
    if (swtObj instanceof Shell) {
      return (Shell) swtObj;
    }
    else if (swtObj instanceof Window) {
      return ((Window)swtObj).getShell();
    }
    else if (swtObj instanceof Widget) {
      // FIXME: that does not really work...
      return ((Widget)swtObj).getDisplay().getShells()[0];
    }
    else {
      throw new PmRuntimeException("Unable to get swt shell from instance: " + swtObj);
    }
  }


}

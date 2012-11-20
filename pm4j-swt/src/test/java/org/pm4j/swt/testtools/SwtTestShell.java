package org.pm4j.swt.testtools;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SwtTestShell {

  private final Display display;
  private final Shell shell;

  public SwtTestShell(int width, int height, String title) {
    display = new Display();
    shell = new Shell(display);
    shell.setSize(width, height);
    shell.setLayout(new FillLayout());
    shell.setText(title);
  }

  public void show() {
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }

  public Display getDisplay() {
    return display;
  }

  public Shell getShell() {
    return shell;
  }

}

package org.pm4j.demo.basic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pm4j.swt.testtools.SwtTestShell;


public class GridFillCheck {
  
  public static void main(String[] args) {
    SwtTestShell s = new SwtTestShell(250, 350, "GridFillCheck");
    Shell shell = s.getShell();
    Composite c = new Composite(shell, SWT.BORDER);
    c.setLayout(new GridLayout(2, false));
    
    GridData textLayoutData = new GridData(SWT.FILL, SWT.NONE, true, false);

    new Label(c, SWT.NONE).setText("label");
    new Text(c, SWT.BORDER).setLayoutData(textLayoutData);
    
    GridData textAreaLayoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
    textAreaLayoutData.heightHint = 40;
    
    new Label(c, SWT.NONE).setText("label 2");
    new Text(c, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL).setLayoutData(textAreaLayoutData);
    
    s.show();
  }
}

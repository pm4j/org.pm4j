package org.pm4j.swt.pb;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.swt.testtools.SwtTestShell;

/**
 * A small UI-test programm with single field that may be entered by a {@link PbListForOptions}
 * and/or by a simple text field.
 * <p>
 * Data entry in one field should be reflected by the other one.
 * 
 * @author olaf boede
 */
public class PbListForOptionsCheck {

  public static class TestPm extends PmConversationImpl {
    enum MyOptions { ONE, TWO, THREE };
    public PmAttrEnum<MyOptions> myEnumAttr = new PmAttrEnumImpl<MyOptions>(this, MyOptions.class);
  }

  public static void main(String[] args) {
    SwtTestShell s = new SwtTestShell(40, 130, "List for options");
    
    Composite viewArea = s.getShell();
    viewArea.setLayout(new GridLayout());
    
    TestPm testPm = new TestPm();
    
    new PbListForOptions(SWT.BORDER).build(viewArea, testPm.myEnumAttr);
    new PbText(SWT.BORDER).build(viewArea, testPm.myEnumAttr);
    
    s.show();
  }
  
}

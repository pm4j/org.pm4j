package org.pm4j.jface.pb;

import java.util.Collection;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmTableCfg;
import org.pm4j.core.pm.annotation.PmTableColCfg;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl;
import org.pm4j.core.pm.impl.DeprecatedPmTableOfPmElementsImpl;
import org.pm4j.demo.basic.BasicDemoElementPm;
import org.pm4j.swt.pb.PbTable;
import org.pm4j.swt.testtools.DemoData;
import org.pm4j.swt.testtools.SwtTestShell;

public class PbTableCheck {

  @PmTableCfg(sortable=PmBoolean.TRUE)
  static public class MyTablePm extends DeprecatedPmTableOfPmElementsImpl {

    public MyTablePm(PmObject pmCtxt, Collection<BasicDemoElementPm> elements) {
      super(pmCtxt, elements);
    }

    @PmTableColCfg(prefSize="60pt")
    public final PmTableCol intField = new PmTableColImpl(this);
    @PmTableColCfg(prefSize="50")
    public final PmTableCol textFieldShort = new PmTableColImpl(this);
    @PmTableColCfg(prefSize="20", sortable=PmBoolean.TRUE)
    public final PmTableCol color = new PmTableColImpl(this);
  }

  
  public static void main(String[] args) {
    PmConversation session = new PmConversationImpl();
    PmTable pm = new MyTablePm(session, DemoData.makeDemoPmList(session, 20));
    
    SwtTestShell s = new SwtTestShell(500, 350, "jFace Table Binding Demo");
    new PbTable().build(s.getShell(), pm);
    s.show();
  }
 
}

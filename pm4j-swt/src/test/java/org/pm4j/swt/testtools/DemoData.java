package org.pm4j.swt.testtools;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.demo.basic.BasicDemoElementPm;

public class DemoData {

  public static BasicDemoElementPm makeDemoPm() {
    PmConversation session = new PmConversationImpl();
    BasicDemoElementPm pm = new BasicDemoElementPm(session);
    return pm;
  }

  public static List<BasicDemoElementPm> makeDemoPmList(PmObject pmCtxt, int size) {
    List<BasicDemoElementPm> list = new ArrayList<BasicDemoElementPm>(size);
    
    BasicDemoElementPm.Color[] colors = BasicDemoElementPm.Color.values(); 

    for (int i=0; i<size; ++i) {
      BasicDemoElementPm pm = new BasicDemoElementPm(pmCtxt);
      
      pm.intField.setValue(i);
      pm.textFieldShort.setValue("shortText " + i);
      pm.color.setValue(colors[i % colors.length]);
      
      list.add(pm);
    }

    return list;
  }
  
  

}

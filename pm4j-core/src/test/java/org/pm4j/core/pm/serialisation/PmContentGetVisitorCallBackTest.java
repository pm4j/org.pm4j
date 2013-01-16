package org.pm4j.core.pm.serialisation;

import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.lang.SerializationUtils;
import org.pm4j.core.pm.PmAspect;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.serialization.PmContentCfg;
import org.pm4j.core.pm.serialization.PmContentChangeCommand;
import org.pm4j.core.pm.serialization.PmContentContainer;
import org.pm4j.core.pm.serialization.PmContentGetVisitorCallBack;

public class PmContentGetVisitorCallBackTest extends TestCase {
  
  public static class MyPm extends PmConversationImpl {
    public final PmAttrString  s = new PmAttrStringImpl(this);
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);
    public MyPm(PmObject parent) {
      super(parent);
    }
    public MyPm() { }
  }

  public static class MyPmRoot extends PmConversationImpl {
    public final PmAttrString  s = new PmAttrStringImpl(this);
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);    
    public final MyPm myPm1 = new MyPm(this);
    public final MyPm myPm2 = new MyPm(this);
  }
  
  
  public void testGetHierarchical() {
    MyPmRoot pm = new MyPmRoot();
    pm.s.setValue("myPmRoot_s");
    pm.i.setValue(1);
    pm.myPm1.s.setValue("myPm1_s");
    pm.myPm1.i.setValue(11);
    pm.myPm2.s.setValue("myPm2_s");
    pm.myPm2.i.setValue(12);
      
    
    PmContentGetVisitorCallBack v = new PmContentGetVisitorCallBack();
    v.getContentCfg().addAspects(PmAspect.VALUE);
    v.getContentContainer().setPmPath("Bla");
    PmVisitorApi.visit(pm, v);
    assertEquals("Bla", v.getContentContainer().getPmPath());
    
    // System.out.println(PmContentContainerToStringUtil.toString(v.getContentContainer()));
    
    Map<String, PmContentContainer> root = v.getContentContainer().getNamedChildContentMap();
    assertEquals("myPmRoot_s", root.get("s").getAspect(PmAspect.VALUE));
    assertEquals(new Integer(1), root.get("i").getAspect(PmAspect.VALUE));
   
    Map<String, PmContentContainer> myPm1 = root.get("myPm1").getNamedChildContentMap();
    assertEquals("myPm1_s",       myPm1.get("s").getAspect(PmAspect.VALUE));
    assertEquals(new Integer(11), myPm1.get("i").getAspect(PmAspect.VALUE));

    Map<String, PmContentContainer> myPm2 = root.get("myPm2").getNamedChildContentMap();
    assertEquals("myPm2_s",       myPm2.get("s").getAspect(PmAspect.VALUE));
    assertEquals(new Integer(12), myPm2.get("i").getAspect(PmAspect.VALUE));
  }
  
  
  
  
  public void testGetContent() {
    MyPm pm = new MyPm();
    pm.s.setValue("abc");
    pm.i.setValue(3);
    
    PmContentGetVisitorCallBack v = new PmContentGetVisitorCallBack();
    v.getContentCfg().addAspects(PmAspect.VALUE);
    PmVisitorApi.visit(pm, v);

    // System.out.println(PmContentContainerToStringUtil.toString(v.getContentContainer()));
    
    Map<String, PmContentContainer> root = v.getContentContainer().getNamedChildContentMap();
    assertEquals("abc", root.get("s").getAspect(PmAspect.VALUE));
    assertEquals(new Integer(3), root.get("i").getAspect(PmAspect.VALUE));
 
  }
  
  private PmContentCfg valueTransferContentCfg = new PmContentCfg(PmAspect.VALUE);
  
  public void testSerializeAndDeserialize() {
    MyPm pm = new MyPm();
    pm.s.setValue("abc");
    pm.i.setValue(3);
    
    
    PmContentGetVisitorCallBack v = new PmContentGetVisitorCallBack(valueTransferContentCfg);
    PmVisitorApi.visit(pm, v);
    
    // System.out.println(PmContentContainerToStringUtil.toString(v.getContentContainer()));
    
    MyPm pm2 = new MyPm();
    
    byte[] bytes = SerializationUtils.serialize(v.getContentContainer());
    PmContentContainer contentContainer = (PmContentContainer)SerializationUtils.deserialize(bytes);
    
    new PmContentChangeCommand(pm2, contentContainer).doIt();
        
    assertEquals("abc", pm2.s.getValue());
    assertEquals(new Integer(3), pm2.i.getValue());
    
    pm2.s.setValue("ab");
    pm2.i.setValue(null);

    v = new PmContentGetVisitorCallBack(valueTransferContentCfg);
    PmVisitorApi.visit(pm2, v);

    new PmContentChangeCommand(pm, v.getContentContainer()).doIt();
    
    assertEquals("ab", pm.s.getValue());
    assertEquals(null, pm2.i.getValue());
  }
}

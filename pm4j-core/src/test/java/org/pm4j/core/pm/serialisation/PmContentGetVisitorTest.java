package org.pm4j.core.pm.serialisation;

import junit.framework.TestCase;

import org.apache.commons.lang.SerializationUtils;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmAspect;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.serialization.PmContentCfg;
import org.pm4j.core.pm.serialization.PmContentChangeCommand;
import org.pm4j.core.pm.serialization.PmContentContainer;
import org.pm4j.core.pm.serialization.PmContentGetVisitor;

public class PmContentGetVisitorTest extends TestCase {
  
  public static class MyPm extends PmConversationImpl {
    public final PmAttrString  s = new PmAttrStringImpl(this);
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);
    
    @Override
    protected void handleNamedPmObjectNotFound(String name) {
      if ("namedPm".equals(name)) {
        setPmNamedObject("namedPm", new MyPm());
      }
      else {
        super.handleNamedPmObjectNotFound(name);
      }
    }
  }

  public void testGetContent() {
    MyPm pm = new MyPm();
    pm.s.setValue("abc");
    pm.i.setValue(3);
    
    PmContentGetVisitor v = new PmContentGetVisitor();
    v.contentCfg.addAspects(PmAspect.VALUE);
    
    pm.accept(v);
    
    System.out.println(PmContentContainerToStringUtil.toString(v.contentContainer));
  }
  
  private PmContentCfg valueTransferContentCfg = new PmContentCfg(PmAspect.VALUE);
  
  public void testSerializeAndDeserialize() {
    MyPm pm = new MyPm();
    pm.s.setValue("abc");
    pm.i.setValue(3);
    
    PmContentGetVisitor v = new PmContentGetVisitor(valueTransferContentCfg);
    pm.accept(v);
    System.out.println(PmContentContainerToStringUtil.toString(v.contentContainer));
    
    MyPm pm2 = new MyPm();
    
    byte[] bytes = SerializationUtils.serialize(v.contentContainer);
    PmContentContainer contentContainer = (PmContentContainer)SerializationUtils.deserialize(bytes);
    
    new PmContentChangeCommand(pm2, contentContainer).doIt();
    
    
    assertEquals("abc", pm2.s.getValue());
    assertEquals(new Integer(3), pm2.i.getValue());
    
    pm2.s.setValue("ab");
    pm2.i.setValue(null);
    
    v = new PmContentGetVisitor(valueTransferContentCfg);
    pm2.accept(v);
    
    new PmContentChangeCommand(pm, v.contentContainer).doIt();
    
    assertEquals("ab", pm.s.getValue());
    assertEquals(null, pm2.i.getValue());
  }
}

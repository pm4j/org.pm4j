package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmObjectBase;

public class PmObjectTitleTest {
  
  ResKeyBaseTitleTestPm resKeyTestPm = new ResKeyBaseTitleTestPm(); 
  
  @Test
  public void testTitles() {
    assertEquals("TestPm", resKeyTestPm.getPmTitle());
    assertEquals("TestPm", resKeyTestPm.getPmShortTitle());
    assertEquals("A Long", resKeyTestPm.a.getPmTitle());
    assertEquals("A Short", resKeyTestPm.a.getPmShortTitle());
    assertEquals("B", resKeyTestPm.b.getPmTitle());
    assertEquals("B", resKeyTestPm.b.getPmShortTitle());
    assertEquals("C", resKeyTestPm.c.getPmTitle());
    assertEquals("C Short", resKeyTestPm.c.getPmShortTitle());
    assertEquals("E", resKeyTestPm.pmWithFixTitleAndShortTitle.getPmTitle());
    assertEquals("E Short", resKeyTestPm.pmWithFixTitleAndShortTitle.getPmShortTitle());
  }
  
  @PmTitleCfg(resKeyBase="pmObjectTitleTest.testPm")
  static class ResKeyBaseTitleTestPm extends PmConversationImpl {
    public final PmObject a = new PmObjectBase(this);
    public final PmObject b = new PmObjectBase(this);
    public final PmObject c = new PmObjectBase(this) {
      protected String getPmShortTitleImpl() {
        return "C Short";
      };
    };
    @PmTitleCfg(title="D")
    public final PmObject d = new PmObjectBase(this);
    @PmTitleCfg(title="E", shortTitle="E Short")
    public final PmObject pmWithFixTitleAndShortTitle = new PmObjectBase(this);
  }

}

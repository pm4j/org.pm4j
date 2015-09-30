package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmObjectBase;

public class PmObjectTitleTest {
  
  @Test
  public void testResKeyBasedTitles() {
    
    @PmTitleCfg(resKeyBase="pmObjectTitleTest.testPm")
    class TitleTestPm extends PmConversationImpl {
      public final PmObject b = new PmObjectBase(this);
    }

    TitleTestPm pm = new TitleTestPm(); 
    assertEquals("TestPm", pm.getPmTitle());
    assertEquals("TestPm", pm.getPmShortTitle());
    assertEquals(null, pm.getPmTooltip());
    assertEquals(null, pm.getPmIconPath());
    
    assertEquals("B", pm.b.getPmTitle());
    assertEquals("B", pm.b.getPmShortTitle());
    assertEquals(null, pm.b.getPmTooltip());
    assertEquals(null, pm.b.getPmIconPath());
  }

  @Test
  public void testResKeyBasedTitleAndShortTitle() {

    @PmTitleCfg(resKeyBase="pmObjectTitleTest.testPm")
    class TitleTestPm extends PmConversationImpl {
      public final PmObject a = new PmObjectBase(this);
    }

    TitleTestPm pm = new TitleTestPm(); 
    assertEquals("TestPm", pm.getPmTitle());
    assertEquals("TestPm", pm.getPmShortTitle());
    assertEquals("A Long", pm.a.getPmTitle());
    assertEquals("A Short", pm.a.getPmShortTitle());
  }

  
  @Test
  public void testResKeyBasedTitleCombinedWithMethodBasedShortTitle() {
    
    @PmTitleCfg(resKeyBase="pmObjectTitleTest.testPm")
    class TitleTestPm extends PmConversationImpl {
      protected String getPmShortTitleImpl() {
        return "Short Title";
      };
    }
    
    TitleTestPm pm = new TitleTestPm(); 
    assertEquals("TestPm", pm.getPmTitle());
    assertEquals("Short Title", pm.getPmShortTitle());
  }

  @Test
  public void testAnnotationBasedTitles() {

    class TitleTestPm extends PmConversationImpl {
      
      @PmTitleCfg(title="D")
      public final PmObject d = new PmObjectBase(this);
      
      @PmTitleCfg(title="E", shortTitle="E Short", tooltip="E Tooltip", icon="e-icon")
      public final PmObject e = new PmObjectBase(this);
    }

    TitleTestPm pm = new TitleTestPm(); 
    assertEquals("D", pm.d.getPmTitle());
    assertEquals("D", pm.d.getPmShortTitle());
    assertEquals(null, pm.d.getPmTooltip());
    assertEquals(null, pm.d.getPmIconPath());
    
    assertEquals("E", pm.e.getPmTitle());
    assertEquals("E Short", pm.e.getPmShortTitle());
    assertEquals("E Tooltip", pm.e.getPmTooltip());
    assertEquals("e-icon", pm.e.getPmIconPath());
  }
}

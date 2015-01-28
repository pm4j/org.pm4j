package org.pm4j.core.pm.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.annotation.PmObjectCfg;
import org.pm4j.core.pm.annotation.PmObjectCfg.Enable;
import org.pm4j.core.pm.annotation.PmObjectCfg.Visible;
import org.pm4j.tools.test.PmAssert;

/**
 * Test @PmObjectCfg annotation effects on PmTables
 * 
 * @author JHETMANS
 * 
 */
public class PmObjectCfgWithPmAttrsTest {
  
  @Test
  public void testVisibleIfNotEmpty() {
    TestPm testPm = new TestPm();
    
    PmAssert.setValue(testPm.visibleIfNotEmpty, "SOME VALUE");
    PmAssert.assertVisible(testPm.visibleIfNotEmpty);
  }
  
  @Test
  public void testReadOnlyParentPmBehavior() {
    // assign
    TestPm underTest = new TestPm();
    underTest.isReadOnly = true;

    PmAssert.assertNotEnabled(
        underTest.attrDefaultEnabledAndVisibleBehavior,
        underTest.alwaysDisabled,
        underTest.neverVisible,
        underTest.visibleIfEnabled,
        underTest.visibleIfEnabledNo,
        underTest.visibleIfNotEmpty,
        underTest.visibleInEditableContext
    );
    
    PmAssert.assertVisible(
        underTest.attrDefaultEnabledAndVisibleBehavior,
        underTest.alwaysDisabled
    );
    
    PmAssert.assertNotVisible(
        underTest.neverVisible,
        underTest.visibleIfEnabled,
        underTest.visibleIfNotEmpty,
        underTest.visibleInEditableContext
    );
  }

  @Test
  public void testEditableParentPmBehavior() {
    TestPm underTest = new TestPm();
    underTest.isReadOnly = false;
    
    PmAssert.assertEnabled(
        underTest.attrDefaultEnabledAndVisibleBehavior,
        underTest.neverVisible,
        underTest.visibleIfEnabled,
        underTest.visibleIfNotEmpty,
        underTest.visibleInEditableContext
    );
    
    PmAssert.assertNotEnabled(
        underTest.visibleIfEnabledNo,
        underTest.alwaysDisabled
    );
    
    PmAssert.assertVisible(
        underTest.attrDefaultEnabledAndVisibleBehavior,
        underTest.visibleIfEnabled,
        underTest.alwaysDisabled,
        underTest.visibleInEditableContext
    );
    
    PmAssert.assertNotVisible(
        underTest.neverVisible,
        underTest.visibleIfNotEmpty
    );
    
  }
  
  @Test(expected = PmRuntimeException.class)
  public void testUnnessesarilyAnnotatedPmAttrThrowsException() {
    UnnessesaryAnnotationUsage testPm = new UnnessesaryAnnotationUsage();
    
    PmAssert.assertEnabled(testPm.unnessesarilyAnnotatedPm);
  }

  
  private static class TestPm extends PmConversationImpl {
    private boolean isReadOnly = false;
    
    @Override
    protected boolean isPmReadonlyImpl() {
      return isReadOnly;
    }
    
    public final PmAttrString attrDefaultEnabledAndVisibleBehavior = new PmAttrStringImpl(this);
    
    @PmObjectCfg(visible = Visible.IN_EDITABLE_CTXT)
    public final PmAttrString visibleInEditableContext = new PmAttrStringImpl(this);

    @PmObjectCfg(visible = Visible.IF_NOT_EMPTY)
    public final PmAttrString visibleIfNotEmpty = new PmAttrStringImpl(this);

    @PmObjectCfg(visible = Visible.IF_ENABLED)
    public final PmAttrString visibleIfEnabled = new PmAttrStringImpl(this);
    
    @PmObjectCfg(visible = Visible.IF_ENABLED, enabled = Enable.NO)
    public final PmAttrString visibleIfEnabledNo = new PmAttrStringImpl(this);

    @PmObjectCfg(visible = Visible.NO)
    public final PmAttrString neverVisible = new PmAttrStringImpl(this);
    
    @PmObjectCfg(enabled = Enable.NO)
    public final PmAttrString alwaysDisabled = new PmAttrStringImpl(this);
    
  }
  
  private static class UnnessesaryAnnotationUsage extends PmConversationImpl {
    @PmObjectCfg(enabled = Enable.IN_EDITABLE_CTXT)
    public final PmAttrString unnessesarilyAnnotatedPm = new PmAttrStringImpl(this);
  }
}

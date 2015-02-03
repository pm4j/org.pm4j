package org.pm4j.core.pm.impl;

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
  HelperParentPm pm = new HelperParentPm();
  
  @Test
  public void testVisibleIfNotEmpty() {
    
    PmAssert.setValue(pm.visibleIfNotEmpty, "SOME VALUE");
    PmAssert.assertVisible(pm.visibleIfNotEmpty);
  }
  
  @Test
  public void testReadOnlyParentPmBehavior() {
    // assign
    pm.isReadOnly = true;

    PmAssert.assertNotEnabled(
        pm.attrDefaultEnabledAndVisibleBehavior,
        pm.alwaysDisabled,
        pm.neverVisible,
        pm.visibleIfEnabled,
        pm.visibleIfEnabledNo,
        pm.visibleIfNotEmpty,
        pm.visibleInEditableContext
    );
    
    PmAssert.assertVisible(
        pm.attrDefaultEnabledAndVisibleBehavior,
        pm.alwaysDisabled
    );
    
    PmAssert.assertNotVisible(
        pm.neverVisible,
        pm.visibleIfEnabled,
        pm.visibleIfNotEmpty,
        pm.visibleInEditableContext
    );
  }

  @Test
  public void testEditableParentPmBehavior() {
    pm.isReadOnly = false;
    
    PmAssert.assertEnabled(
        pm.attrDefaultEnabledAndVisibleBehavior,
        pm.neverVisible,
        pm.visibleIfEnabled,
        pm.visibleIfNotEmpty,
        pm.visibleInEditableContext
    );
    
    PmAssert.assertNotEnabled(
        pm.visibleIfEnabledNo,
        pm.alwaysDisabled
    );
    
    PmAssert.assertVisible(
        pm.attrDefaultEnabledAndVisibleBehavior,
        pm.visibleIfEnabled,
        pm.alwaysDisabled,
        pm.visibleInEditableContext
    );
    
    PmAssert.assertNotVisible(
        pm.neverVisible,
        pm.visibleIfNotEmpty
    );
    
  }
  
  @Test(expected = PmRuntimeException.class)
  public void testUnnessesarilyAnnotatedPmAttrThrowsException() {
    UnnessesaryAnnotationUsage testPm = new UnnessesaryAnnotationUsage();
    
    PmAssert.assertEnabled(testPm.unnessesarilyAnnotatedPm);
  }

  
  private static class HelperParentPm extends PmConversationImpl {
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

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
  HelperParentPm parentPm = new HelperParentPm();
  
  @Test
  public void testVisibleIfNotEmpty() {
    
    PmAssert.setValue(parentPm.visibleIfNotEmpty, "SOME VALUE");
    PmAssert.assertVisible(parentPm.visibleIfNotEmpty);
  }
  
  @Test
  public void testReadOnlyParentPmBehavior() {
    // assign
    parentPm.isReadOnly = true;

    PmAssert.assertNotEnabled(
        parentPm.attrDefaultEnabledAndVisibleBehavior,
        parentPm.alwaysDisabled,
        parentPm.neverVisible,
        parentPm.visibleIfEnabled,
        parentPm.visibleIfEnabledNo,
        parentPm.visibleIfNotEmpty,
        parentPm.visibleInEditableContext
    );
    
    PmAssert.assertVisible(
        parentPm.attrDefaultEnabledAndVisibleBehavior,
        parentPm.alwaysDisabled
    );
    
    PmAssert.assertNotVisible(
        parentPm.neverVisible,
        parentPm.visibleIfEnabled,
        parentPm.visibleIfNotEmpty,
        parentPm.visibleInEditableContext
    );
  }

  @Test
  public void testEditableParentPmBehavior() {
    parentPm.isReadOnly = false;
    
    PmAssert.assertEnabled(
        parentPm.attrDefaultEnabledAndVisibleBehavior,
        parentPm.neverVisible,
        parentPm.visibleIfEnabled,
        parentPm.visibleIfNotEmpty,
        parentPm.visibleInEditableContext
    );
    
    PmAssert.assertNotEnabled(
        parentPm.visibleIfEnabledNo,
        parentPm.alwaysDisabled
    );
    
    PmAssert.assertVisible(
        parentPm.attrDefaultEnabledAndVisibleBehavior,
        parentPm.visibleIfEnabled,
        parentPm.alwaysDisabled,
        parentPm.visibleInEditableContext
    );
    
    PmAssert.assertNotVisible(
        parentPm.neverVisible,
        parentPm.visibleIfNotEmpty
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

package org.pm4j.core.pm.impl;

import org.junit.Test;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTableTest.MyTablePm;
import org.pm4j.core.pm.annotation.PmObjectCfg;
import org.pm4j.core.pm.annotation.PmObjectCfg.Enable;
import org.pm4j.core.pm.annotation.PmObjectCfg.Visible;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.tools.test.PmAssert;

/**
 * Test @PmObjectCfg annotation effects on PmTables
 * 
 * @author JHETMANS
 * 
 */
public class PmObjectCfgWithPmTablesAndAttrsTest {

  HelperParentPm helperParentPm = new HelperParentPm();
  
  @Test
  public void testAnnotatedEmptyTableVisibility() {
    // assign:
    helperParentPm.tableNotVisibleIfEmpty.numberOfPmRows = 0L;
    
    // act & assert:
    PmAssert.assertNotVisible(helperParentPm.tableNotVisibleIfEmpty);
  }
  
  @Test
  public void testAnnotatedNotEmptyTableVisibility() {
    // assign:
    helperParentPm.tableNotVisibleIfEmpty.numberOfPmRows = 1L;
    
    // act & assert: 
    PmAssert.assertVisible(helperParentPm.tableNotVisibleIfEmpty);
  }
  
  @Test
  public void testDefaultEmptyTableVisibility() {
    // assign:
    helperParentPm.tableWithDefaultVisibility.numberOfPmRows = 0;
    
    // act & assert:
    PmAssert.assertVisible(helperParentPm.tableWithDefaultVisibility);
  }
  
  @Test
  public void testDefaultNotEmptyTableVisibility() {
    // assign:
    helperParentPm.tableWithDefaultVisibility.numberOfPmRows = 0;
    
    // act & assert:
    PmAssert.assertVisible(helperParentPm.tableWithDefaultVisibility);
  }
  
  @Test
  public void testAnnotatedAndNotEmptyButSuperInvisible() {
    // assign:
    helperParentPm.tableNotVisibleIfEmpty.numberOfPmRows = 1000;
    helperParentPm.tableNotVisibleIfEmpty.setPmVisible(false);
    
    // act & assert:
    PmAssert.assertNotVisible(helperParentPm.tableNotVisibleIfEmpty);
  }
  
  @Test
  public void testPmShouldBeInvisibleAndDisabled() {
    // assign:
    helperParentPm.isReadOnly = true;
   
    // act & assert:
    PmAssert.assertNotVisible(helperParentPm.tableWithEditableAndVisibleInEditableCtx);
    PmAssert.assertNotEnabled(helperParentPm.tableWithEditableAndVisibleInEditableCtx);
  }
  
  @Test
  public void testPmShouldBeEnabledAndVisible() {
    // assign:
    helperParentPm.isReadOnly = false;
   
    // act & assert:
    PmAssert.assertVisible(helperParentPm.tableWithEditableAndVisibleInEditableCtx);
    PmAssert.assertEnabled(helperParentPm.tableWithEditableAndVisibleInEditableCtx);
  }
  
  private static class HelperParentPm extends PmConversationImpl {
    private boolean isReadOnly = false;
    
    @Override
    protected boolean isPmReadonlyImpl() {
      return isReadOnly;
    }
 
    @PmTitleCfg(title = "Table visible only if not-empty")
    @PmObjectCfg(visible = Visible.IF_NOT_EMPTY)
    public final TestTablePm tableNotVisibleIfEmpty = new TestTablePm(this);
    
    @PmTitleCfg(title = "Table with default visibility")
    public final TestTablePm tableWithDefaultVisibility = new TestTablePm(this);
    
    @PmObjectCfg(
        visible = Visible.IN_EDITABLE_CTXT,
        enabled = Enable.IN_EDITABLE_CTXT
    )
    public final TestTablePm tableWithEditableAndVisibleInEditableCtx = new TestTablePm(this);
    
  }
  
  private static class TestTablePm extends MyTablePm {
    
    private long numberOfPmRows = 0;
    
    public TestTablePm(PmObject pmParent) {
      super(pmParent);
    }
    
    @Override
    public long getTotalNumOfPmRows() {
      return numberOfPmRows;
    }
  }
}

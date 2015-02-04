package org.pm4j.core.pm.impl;

import org.junit.Test;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.annotation.PmCacheCfg2;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Cache;
import org.pm4j.core.pm.annotation.PmCacheCfg2.CacheMode;
import org.pm4j.core.pm.annotation.PmObjectCfg;
import org.pm4j.core.pm.annotation.PmObjectCfg.Enable;
import org.pm4j.core.pm.annotation.PmObjectCfg.Visible;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.tools.test._PmAssert;

/**
 * Test @PmObjectCfg annotation effects on PmCommands. 
 * 
 * @author JHETMANS
 * 
 */
public class PmObjectCfgWithPmCommandsTest {
  
  HelperParentPm pm = new HelperParentPm();
  
  @Test
  public void testReadOnlyParentPmBehavior() {
    // assign
    pm.isReadOnly = true;

    // asserts:
    _PmAssert.assertEnabled(
            pm.cmdDefaultEnabledBehavior,
            pm.cmdDefaultVisibility,
            pm.cmdVisibleIfEditable
    );

    _PmAssert.assertNotEnabled(
            pm.attrDefaultEnabledBehavior,
            pm.cmdEnabledInEditCtxt,
            pm.cmdEnabledIfEditableAndVisibleWhenEnabled
    );
    
    _PmAssert.assertVisible(
            pm.attrDefaultEnabledBehavior,
            pm.cmdDefaultEnabledBehavior,
            pm.cmdDefaultVisibility,
            pm.cmdEnabledInEditCtxt
    );

    _PmAssert.assertNotVisible(
            pm.cmdVisibleIfEditable,
            pm.cmdEnabledIfEditableAndVisibleWhenEnabled
    );

    assertsForPmObjectsWithImmutableState(pm);
  }

  @Test
  public void testEditableParentPmBehavior() {
    pm.isReadOnly = false;

    _PmAssert.assertEnabled(
            pm.cmdEnabledInEditCtxt,
            pm.cmdDefaultEnabledBehavior,
            pm.cmdDefaultVisibility,
            pm.cmdVisibleWhenEnabled,
            pm.cmdVisibleIfEditable,
            pm.cmdEnabledIfEditableAndVisibleWhenEnabled
    );
    
    _PmAssert.assertVisible(
            pm.cmdDefaultEnabledBehavior,
            pm.cmdDefaultVisibility,
            pm.cmdVisibleWhenEnabled,
            pm.cmdEnabledInEditCtxt,
            pm.cmdVisibleIfEditable,
            pm.cmdEnabledIfEditableAndVisibleWhenEnabled
    );

    assertsForPmObjectsWithImmutableState(pm);
  }
  
  /*
   * Command annotated with:
   * @PmObjectCfg(enabled = Enable.IN_EDITABLE_CTXT)
   */
  @Test
  public void testEnabledInEditableCtxtWhenCacheIsOff() {  
    pm.isReadOnly = false;
    
    _PmAssert.assertEnabled(pm.cmdWithDisabledCache);
    
    pm.isReadOnly = true;
    
    _PmAssert.assertNotEnabled(pm.cmdWithDisabledCache);
  }
  
  /*
   * Command annotated with:
   * @PmObjectCfg(enabled = Enable.IN_EDITABLE_CTXT)
   */
  @Test
  public void testEnabledInEditableCtxtWhenCacheIsOn() {   
    pm.isReadOnly = false;
    
    _PmAssert.assertEnabled(pm.cmdWithEnabledCache);
    
    pm.isReadOnly = true;
    
    _PmAssert.assertEnabled(pm.cmdWithEnabledCache);
    
    PmCacheApi.clearPmCache(pm.cmdWithEnabledCache, CacheKind.ENABLEMENT);
    
    _PmAssert.assertNotEnabled(pm.cmdWithDisabledCache);
    
  }
  
  /*
   * Command annotated with:
   * @PmObjectCfg(visible = Visible.IN_EDITABLE_CTXT)
   */
  @Test
  public void testVisibleInEditableCtxtWhenCacheIsOff() {
    pm.isReadOnly = false;
    
    _PmAssert.assertVisible(pm.cmdWithVisiblityCacheOff);
    
    pm.isReadOnly = true;
    
    _PmAssert.assertNotVisible(pm.cmdWithVisiblityCacheOff);
  }
  
  /*
   * Command annotated with:
   * @PmObjectCfg(visible = Visible.IN_EDITABLE_CTXT)
   */
  @Test
  public void testVisibleInEditableCtxtWhenCacheIsOn() {
    pm.isReadOnly = false;
    
    _PmAssert.assertVisible(pm.cmdWithVisiblityCacheOn);
    
    pm.isReadOnly = true;
    
    _PmAssert.assertVisible(pm.cmdWithVisiblityCacheOn);
  }
  
  /*
   * Command annotated with
   * 
    @PmObjectCfg(
      enabled = Enable.IN_EDITABLE_CTXT,
      visible = Visible.IF_ENABLED
    )
  */
  @Test
  public void testVisibilityDependsOnCachedEnablement() {
    pm.isReadOnly = true;
    
    _PmAssert.assertNotVisible(pm.cmdVisibilityDependsOnCachedEnablement);
    _PmAssert.assertNotEnabled(pm.cmdVisibilityDependsOnCachedEnablement);
    
    pm.isReadOnly = false;
    
    _PmAssert.assertNotVisible(pm.cmdVisibilityDependsOnCachedEnablement);
    _PmAssert.assertNotEnabled(pm.cmdVisibilityDependsOnCachedEnablement);
  }
 
  /*
   * Those should remain in the same state no matter what
   */
  private void assertsForPmObjectsWithImmutableState(HelperParentPm parentPm) {
    _PmAssert.assertNotEnabled(parentPm.cmdAlwaysDisabled);
    _PmAssert.assertNotVisible(parentPm.cmdAlwaysInvisible);
  }

  private static class HelperParentPm extends PmConversationImpl {
    
    private boolean isReadOnly = false;
    
    @Override
    protected boolean isPmReadonlyImpl() {
      return isReadOnly;
    }

    /**
     * PmObjectCfg Enablement options usage:
     */
    @PmTitleCfg(title = "Enabled in edit mode")
    @PmObjectCfg(enabled = Enable.DEFAULT)
    public final PmCommand cmdDefaultEnabledBehavior = new PmCommandImpl(this);
 
    
    @PmTitleCfg(title = "PmAttr default enablement behavior")
    public final PmAttrString attrDefaultEnabledBehavior = new PmAttrStringImpl(this);

    
    @PmTitleCfg(title = "Enabled in edit mode")
    @PmObjectCfg(enabled = Enable.IN_EDITABLE_CTXT)
    public final PmCommand cmdEnabledInEditCtxt = new PmCommandImpl(this);

    @PmTitleCfg(title = "Always disabled")
    @PmObjectCfg(enabled = Enable.NO)
    public final PmAttrString cmdAlwaysDisabled = new PmAttrStringImpl(this);

    /**
     * PmObjectCfg Visibility options usage
     */

    @PmTitleCfg(title = "Default visibility")
    @PmObjectCfg(visible = Visible.DEFAULT)
    public final PmCommand cmdDefaultVisibility = new PmCommandImpl(this);

    
    @PmTitleCfg(title = "Visible in edit mode")
    @PmObjectCfg(visible = Visible.IN_EDITABLE_CTXT)
    public final PmCommand cmdVisibleIfEditable = new PmCommandImpl(this);

    
    @PmTitleCfg(title = "Always invisible")
    @PmObjectCfg(visible = Visible.NO)
    public final PmCommand cmdAlwaysInvisible = new PmCommandImpl(this);

    
    @PmTitleCfg(title = "Visible when enabled")
    @PmObjectCfg(visible = Visible.IF_ENABLED)
    public final PmCommand cmdVisibleWhenEnabled = new PmCommandImpl(this);

    
    // TODO: JHE implement not-empty
    @PmTitleCfg(title = "Visible if not-empty")
    public final PmCommand cmdVisibleIfNotEmpty = new PmCommandImpl(this);
    
    /*
     * Mixed visibility and enablement options
     */
    @PmTitleCfg(title = "Simple Command")
    @PmObjectCfg(enabled = Enable.IN_EDITABLE_CTXT, visible = Visible.IF_ENABLED)
    public final PmCommand cmdEnabledIfEditableAndVisibleWhenEnabled = new PmCommandImpl(this) {
      protected void doItImpl() {
        PmMessageApi.addStringMessage(this, Severity.INFO, "cmdSimple was executed.");
      }
    };
    
    /* Cache interaction */
    
    @PmTitleCfg(title = "Enablement cache disabled")
    @PmCacheCfg2(value = {
        @Cache(property = CacheKind.ENABLEMENT, mode = CacheMode.OFF)
    })
    @PmObjectCfg(enabled = Enable.IN_EDITABLE_CTXT)
    public final PmCommand cmdWithDisabledCache = new PmCommandImpl(this);
    
    
    @PmTitleCfg(title = "Enablement cache enabled")
    @PmCacheCfg2(value = {
        @Cache(property = CacheKind.ENABLEMENT, mode = CacheMode.ON)
    })
    @PmObjectCfg(enabled = Enable.IN_EDITABLE_CTXT)
    public final PmCommand cmdWithEnabledCache = new PmCommandImpl(this);
    
    
    @PmTitleCfg(title = "Visibility cache enabled")
    @PmCacheCfg2(value = {
        @Cache(property = CacheKind.VISIBILITY, mode = CacheMode.ON)
    })
    @PmObjectCfg(visible = Visible.IN_EDITABLE_CTXT)
    public final PmCommand cmdWithVisiblityCacheOn = new PmCommandImpl(this);
    
    
    @PmTitleCfg(title = "Visibility cache disabled")
    @PmCacheCfg2(value = {
        @Cache(property = CacheKind.VISIBILITY, mode = CacheMode.OFF)
    })
    @PmObjectCfg(visible = Visible.IN_EDITABLE_CTXT)
    public final PmCommand cmdWithVisiblityCacheOff = new PmCommandImpl(this);
    
    
    @PmTitleCfg(title = "Enablement cache on & Visibility cache off")
    @PmCacheCfg2(value = {
        @Cache(property = CacheKind.ENABLEMENT, mode = CacheMode.ON),
        @Cache(property = CacheKind.VISIBILITY, mode = CacheMode.OFF)
    })
    @PmObjectCfg(enabled = Enable.IN_EDITABLE_CTXT, visible = Visible.IF_ENABLED)
    public final PmCommand cmdVisibilityDependsOnCachedEnablement = new PmCommandImpl(this);
  }
}

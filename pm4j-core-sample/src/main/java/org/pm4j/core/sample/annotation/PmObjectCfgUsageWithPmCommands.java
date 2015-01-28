package org.pm4j.core.sample.annotation;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmTab;
import org.pm4j.core.pm.annotation.PmObjectCfg;
import org.pm4j.core.pm.annotation.PmObjectCfg.Enable;
import org.pm4j.core.pm.annotation.PmObjectCfg.Visible;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmElementBase;

public class PmObjectCfgUsageWithPmCommands extends PmElementBase implements PmTab {

  /*
   * Default enablement option
   */
  @PmObjectCfg(enabled = Enable.DEFAULT)
  public final PmCommand defaultEnablement = new PmCommandImpl(this);
  
  /*
   * Command enabled in editable (non-ready-only) context 
   */
  @PmObjectCfg(enabled = Enable.IN_EDITABLE_CTXT)
  public final PmCommand enableIfEditable = new PmCommandImpl(this);  

  /*
   * Command always disabled. 
   * 
   * Probably it makes the most sense to use this option in conjunction
   * with PmAttr family objects, but one is free to use it with PmCommands also.
   * E.g. to temporarily disable a button that leads to some functionality, 
   * that is not finished yet.
   */
  @PmObjectCfg(enabled = Enable.NO)
  public final PmCommand temporarilyDisabled = new PmCommandImpl(this); 
  
  /*
   * Default visibility option
   */
  @PmObjectCfg(visible = Visible.DEFAULT)
  public final PmCommand defaultVisibility = new PmCommandImpl(this);
  
  /*
   * Visible if in editable (non-read-only) context
   */
  @PmObjectCfg(visible = Visible.IN_EDITABLE_CTXT)
  public final PmCommand visibleIfEditable = new PmCommandImpl(this);
  
  /*
   * Visible if it is also enabled. Not visible otherwise
   */
  @PmObjectCfg(visible = Visible.IF_ENABLED)
  public final PmCommand visibleIfEnabled = new PmCommandImpl(this);
  
  /*
   * Permanently not visible command
   */
  @PmObjectCfg(visible = Visible.NO)
  public final PmCommand neverVisible = new PmCommandImpl(this);
  
  /**
   * Mixed options 
   */
  
  /* Example of command with visibility & enablement options combined */
  @PmObjectCfg(enabled = Enable.IN_EDITABLE_CTXT, visible = Visible.IF_ENABLED)
  public final PmCommand enabledWhenEditableAndVisibleIfEnabled = new PmCommandImpl(this);
  
}

package org.pm4j.core.sample.annotation;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmTab;
import org.pm4j.core.pm.annotation.PmObjectCfg;
import org.pm4j.core.pm.annotation.PmObjectCfg.Enable;
import org.pm4j.core.pm.annotation.PmObjectCfg.Visible;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmElementBase;
import org.pm4j.core.pm.impl.PmObjectBase;


/**
 * Example of @PmObjectCfg usage with PmAttr and PmTable
 * 
 * For more options see {@link PmObjectCfgUsageWithPmCommands}
 * All options presented there are also applicable here with
 * one exception: 
 * 
 * Applying {@link PmObjectCfg.Enable.IN_EDITABLE_CTX} onto 
 * the PmAttr results in PmException being thrown. That's 
 * because the option is redundant for PmAttr types tree. 
 * It's their default behavior only to be enabled when in editable context.
 * This behavior could be changed only by overriding 
 * {@link PmObjectBase.isPmReadOnlyImpl}.
 */
public class PmObjectCfgUsageWithPmAttrsAndTables extends PmElementBase implements PmTab {

  /*
   * Visible.IF_NOT_EMPTY option is specific to PmAttr and PmTables types
   * and their sub-types. 
   * It has no effect on e.g. PmCommands.
   * 
   * Basically it hides a PmAttr until it's value is set.
   * 
   * If Visible.IF_NOT_EMPTY is applied to PmTable component
   * the component is visible if it's getTotalNumOfRows() 
   * returns positive value.
   */
  @PmObjectCfg(visible = Visible.IF_NOT_EMPTY) 
  public final PmAttr<String> visibleIfNotEmpty = new PmAttrStringImpl(this);
  
  /*
   * PmAttr permanently invisible
   */
  @PmObjectCfg(enabled = Enable.NO)
  public final PmAttr<String> neverEnabled = new PmAttrStringImpl(this);
}

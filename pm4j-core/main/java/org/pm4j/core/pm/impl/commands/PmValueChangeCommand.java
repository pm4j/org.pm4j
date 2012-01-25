package org.pm4j.core.pm.impl.commands;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmCommandImpl;

/**
 * Command that changes an attribute value.
 *
 * @author olaf boede
 */
@PmTitleCfg(resKey="pmValueChangeCommand")
public final class PmValueChangeCommand extends PmCommandImpl {

  private final Object newValue;

  public PmValueChangeCommand(PmAttrBase<?,?> changedPmAttr, Object newValue) {
    super(changedPmAttr);
    this.newValue = newValue;
    setUndoCommand(new PmValueChangeCommand(this, changedPmAttr.getUncachedValidValue()));
  }

  /**
   * Constructor for the corresponding undo command.
   *
   * @param doCommand The command to undo.
   * @param oldValue The old value that the undo command should set.
   */
  private PmValueChangeCommand(PmValueChangeCommand doCommand, Object oldValue) {
    super(doCommand.getPmParent());

    this.newValue = oldValue;
    setUndoCommand(doCommand);
  }


  @Override @SuppressWarnings("unchecked")
  protected void doItImpl() throws Exception {
    ((PmAttr<Object>)getPmParent()).setValue(newValue);
  }

  /**
   * The referenced presentation model should be enabled.
   */
  @Override
  protected boolean isPmEnabledImpl() {
    return super.isPmEnabledImpl() && getPmParent().isPmEnabled();
  }

}

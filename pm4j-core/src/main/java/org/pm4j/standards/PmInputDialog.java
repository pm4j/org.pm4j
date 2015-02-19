package org.pm4j.standards;

import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmObjectBase;

import static org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO.CLEAR;

/**
 * Supports the common scenario, where a string should be entered within a dialog.
 * <p>
 * A typical use case is a rename or a create new scenario.
 *
 * @author olaf boede
 */
public class PmInputDialog extends PmObjectBase {

  /**
   * Constructor for dependency injection scenarios.<br>
   * Please make sure that {@link #setPmParent(PmObject)} gets called
   * before this instance gets used.
   */
  public PmInputDialog() {
    super();
  }

  /**
   * Initializing constructor.
   *
   * @param pmParent The PM context of this dialog.
   */
  public PmInputDialog(PmObject pmParent) {
    super(pmParent);
  }

  @PmAttrCfg(required=true)
  public final PmAttrString name = new PmAttrStringImpl(this);

  public final PmCommand cmdOk = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      onOk();
    }
  };

  @PmCommandCfg(beforeDo=CLEAR)
  public final PmCommand cmdCancel = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      onCancel();
    }
  };

  /**
   * Gets called when the user clicked on the OK button.
   */
  protected void onOk() {
  }

  /**
   * Gets called when the user clicked on the cancel button.
   */
  protected void onCancel() {
  }

  // -- getter for EL etc. --

  public PmAttrString getName() {
    return name;
  }

  public PmCommand getCmdOk() {
    return cmdOk;
  }

  public PmCommand getCmdCancel() {
    return cmdCancel;
  }

}

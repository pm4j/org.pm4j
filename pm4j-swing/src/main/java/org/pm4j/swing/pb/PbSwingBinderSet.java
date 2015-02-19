package org.pm4j.swing.pb;

import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.core.pb.PbFactory;

/**
 * A set of view binders for Swing standard controls.
 */
public class PbSwingBinderSet implements Cloneable {

  public PbJButton         pbButton         = new PbJButton();
  public PbJCheckBox       pbCheckBox       = new PbJCheckBox();
  public PbJComboBox       pbCombo          = new PbJComboBox();
  public PbFactory<?> labelBuilder;
  public PbJLabel          pbLabel          = new PbJLabel();
  public PbJListForOptions pbListForOptions = new PbJListForOptions();
  public PbJTextField      pbTextField      = new PbJTextField();

  // TODO olaf: copy Pb's instead of distributing references.
  //            ensure that Pb-modifications can be
  //            done without unexpected side effects.
  @Override
  public PbSwingBinderSet clone() {
    try {
      return (PbSwingBinderSet) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

}

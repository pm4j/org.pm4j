package org.pm4j.standards;

import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.navi.NaviLink;


/**
 * A confirmed command that uses resource strings that start with
 * 'pmConfirmDialogDelete'.
 * <p>
 * It allows to use standard string resources for delete scenarios.
 *
 * @author olaf boede
 */
public class PmConfirmedDeleteCommand extends PmConfirmedCommand {

  public PmConfirmedDeleteCommand(PmObject pmParent) {
    super(pmParent);
  }

  public PmConfirmedDeleteCommand(PmObject pmParent,
      NaviLink confirmNaviLink) {
    super(pmParent, confirmNaviLink);
  }

  @Override
  protected PmObject makeConfirmDialogPm() {
    return new PmConfirmDialog(this) {
      @Override
      protected String getNameOfThingToConfirm() {
        return PmConfirmedDeleteCommand.this.getNameOfThingToConfirm();
      }
      @Override
      protected String getDialogResKeyBase() {
        return "pmConfirmDialogDelete";
      }
    };
  }


}

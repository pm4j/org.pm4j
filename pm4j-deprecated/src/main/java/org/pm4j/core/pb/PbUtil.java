package org.pm4j.core.pb;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.navi.impl.NaviLinkImpl;
import org.pm4j.standards.PmConfirmedCommand;

public class PbUtil {

  /**
   * Executes the command. If the command provides a next dialog (E.g. in case of wizard
   * page flows), the PM of the next page will be returned.
   *
   * @param the command to execute.
   * @return The PM of the next page to show.
   */
  public static PmObject doItReturnNextDlgPm(PmCommand cmd) {
    PmCommand executedCmd = cmd.doIt();
    PmObject nextDlgPm = null;
    NaviLinkImpl l = (NaviLinkImpl)executedCmd.getNaviLink();
    if (l != null) {
      nextDlgPm = (PmObject) l.getNaviScopeParams().get(PmConfirmedCommand.NAVI_PARAM_NEXT_DLG_PM);
    }
    return nextDlgPm;
  }


}

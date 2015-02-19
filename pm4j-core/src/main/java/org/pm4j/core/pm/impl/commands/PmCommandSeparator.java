package org.pm4j.core.pm.impl.commands;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmObjectBase;


/**
 * A command separator.
 *
 * @author olaf boede
 */
public class PmCommandSeparator extends PmCommandImpl {

  /**
   * Creates a command separator.
   *
   * @param pmParent The presentation model it acts for.
   */
  public PmCommandSeparator(PmObject pmParent) {
    super(pmParent);
  }

  @Override
  protected boolean isPmEnabledImpl() {
    return false;
  }

  /**
   * Provides only the visible commands and separators.
   *
   * @param allCommands
   *          The set of available commands and separators.
   * @return Only the visible commands and separators to display.
   */
  public static List<PmCommand> filterVisibleCommandsAndSeparators(List<PmCommand> allCommands) {
    if (allCommands.isEmpty()) {
      return allCommands;
    }

    List<PmCommand> cmdList = new ArrayList<PmCommand>();
    PmCommand prevVisibleCmdOfSection = null;
    for (int i=0; i < allCommands.size(); ++i) {
      PmCommand cmd = allCommands.get(i);

      if (cmd instanceof PmCommandSeparator) {
        // A separator should not be the first item of the list.
        if (prevVisibleCmdOfSection == null) {
          continue;
        }

        for (int nextIdx = i+1; nextIdx < allCommands.size(); ++nextIdx) {
          PmCommand nextCmd = allCommands.get(nextIdx);
          // A following visible command which is not a separator provides
          // a reason to show this separator.
          if (nextCmd.isPmVisible() &&
              ! (nextCmd instanceof PmCommandSeparator))
          {
            cmdList.add(cmd);
            cmdList.add(nextCmd);
            prevVisibleCmdOfSection = nextCmd;
            i = nextIdx;
            break;
          }
        }
      }
      else {
        if (cmd.isPmVisible()) {
          cmdList.add(cmd);
          prevVisibleCmdOfSection = cmd;
        }
      }
    }

    return cmdList;
  }


  // ======== Static data ======== //

  /**
   * Defines the specific command kind.
   */
  @Override
  protected void initMetaData(PmObjectBase.MetaData staticData) {
    PmCommandImpl.MetaData sd = (PmCommandImpl.MetaData) staticData;
    super.initMetaData(sd);

    sd.setCmdKind(CmdKind.SEPARATOR);
  }
}

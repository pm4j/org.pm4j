package org.pm4j.core.sample.filebrowser;

import java.util.Arrays;
import java.util.List;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.commands.PmCommandGroup;



/**
 * PM for simple files.
 */
// TODO olaf: Move popup command configuration to an annotation
//   @PmCommandSet(popup="cmdDelete, cmdSeparator, cmdGroup)
public class FilePm extends FilePmBase {

  @Override
  public List<PmCommand> getVisiblePmCommands(PmCommand.CommandSet commandSet) {
    return Arrays.asList(cmdDelete, cmdSeparator, cmdGroup);
  }

  @SuppressWarnings("unused")
  public final PmCommand cmdGroup = new PmCommandGroup(this) {
    public final PmCommand cmdChild1 = new PmCommandImpl(this);
    public final PmCommand cmdChild2 = new PmCommandImpl(this);
  };

}

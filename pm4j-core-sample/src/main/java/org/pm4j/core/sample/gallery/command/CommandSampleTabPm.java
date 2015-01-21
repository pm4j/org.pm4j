package org.pm4j.core.sample.gallery.command;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmTab;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmElementBase;

public class CommandSampleTabPm extends PmElementBase implements PmTab {

  @PmTitleCfg(title = "Simple Command", tooltip = "A command that triggers by default a validation of its embedding PM.")
  public final PmCommand cmdSimple = new PmCommandImpl(this) {
    protected void doItImpl() {
      PmMessageApi.addStringMessage(this, Severity.INFO, "cmdSimple was executed.");
    }
  };

}

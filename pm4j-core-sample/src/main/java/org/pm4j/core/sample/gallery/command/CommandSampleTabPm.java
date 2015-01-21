package org.pm4j.core.sample.gallery.command;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmTab;
import org.pm4j.core.pm.annotation.PmObjectCfg;
import org.pm4j.core.pm.annotation.PmObjectCfg.Enable;
import org.pm4j.core.pm.annotation.PmObjectCfg.Visible;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmElementBase;

public class CommandSampleTabPm extends PmElementBase implements PmTab {

  @PmTitleCfg(title = "Simple Command")
  @PmObjectCfg(enabled=Enable.IN_EDITABLE_CTXT, visible=Visible.IF_ENABLED)
  public final PmCommand cmdSimple = new PmCommandImpl(this) {
    protected void doItImpl() {
      PmMessageApi.addStringMessage(this, Severity.INFO, "cmdSimple was executed.");
    }
  };

  @PmTitleCfg(title = "Enabled in edit mode")
  @PmObjectCfg(enabled=Enable.IN_EDITABLE_CTXT)
  public final PmCommand cmdEnabledInEditCtxt = new PmCommandImpl(this);

  @PmTitleCfg(title = "Visible in edit mode")
  @PmObjectCfg(visible=Visible.IN_EDITABLE_CTXT)
  public final PmCommand cmdVisibleInEditCtxt = new PmCommandImpl(this);

}

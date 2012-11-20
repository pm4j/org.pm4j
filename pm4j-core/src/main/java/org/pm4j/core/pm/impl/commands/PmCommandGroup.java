package org.pm4j.core.pm.impl.commands;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.navi.NaviLink;


/**
 * A pure command group.
 *
 * @author olaf boede
 */
public class PmCommandGroup extends PmCommandImpl {

  /**
   * Creates an empty command group.
   *
   * @param pmParent The presentation model it acts for.
   */
  public PmCommandGroup(PmObject pmParent) {
    this(pmParent, (NaviLink)null);
  }

  /**
   * Creates an empty command group that navigates to a page.
   *
   * @param pmParent The presentation model it acts for.
   */
  public PmCommandGroup(PmObject pmParent, NaviLink naviLink) {
    super(pmParent, naviLink);
  }

  @Override
  protected boolean isPmEnabledImpl() {
    return super.isPmEnabledImpl() &&
           isASubCommandEnabled();
  }

  @Override
  protected boolean isPmVisibleImpl() {
    return super.isPmVisibleImpl() &&
           isASubCommandVisible();
  }

  // ======== Static data ======== //

  /**
   * Defines the specific command kind.
   */
  @Override
  protected void initMetaData(PmObjectBase.MetaData staticData) {
    PmCommandImpl.MetaData sd = (PmCommandImpl.MetaData) staticData;
    super.initMetaData(sd);

    // TODO olaf: is that really still required?
    sd.setCmdKind(CmdKind.GROUP);
  }

}

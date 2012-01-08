package org.pm4j.core.pm.impl.commands;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.navi.NaviLink;

/**
 * Navigates back to the previous page.<br>
 * It is intended for the usual 'Cancel' button functionality. So, it does not
 * trigger PM attribute validation.<br>
 * That validation may be overridden by subclasses or just by specifying
 * 'requiresValidValues=true' using a {@link PmCommandCfg} annotation.
 * <p>
 * In some cases there might be no previous page. (E.g. in case of an application
 * session that was started by a bookmark.)
 * In this case it navigates back to the (optionally configrured) start link.
 * <p>
 * If both are not available, the command will be disabled.
 * <p>
 * <b>Attention</b>: This command will only work when the navigation history is enabled.
 *
 * @author olaf boede
 */
@PmCommandCfg(requiresValidValues=false)
public class PmCommandNaviBack extends PmCommandImpl {

  private NaviLink[] linksToSkip;

  /**
   * @param pm
   *          The parent PM.
   * @param linksToSkip
   *          An optional set of links to skip on back navigation.
   */
  public PmCommandNaviBack(PmObject pm, NaviLink... linksToSkip) {
    super(pm);
    this.linksToSkip = linksToSkip;
  }

  /**
   * Sets the fix link value before command execution. <br>
   * That allows to use this command for static web client links that never
   * execute the command and only ask {@link #getNaviLink()}.
   * <p>
   * But: Be careful if the navigation path changes during the live time
   * of this instance.
   * In this case you should ask only the executed command instance for the
   * real navigation link.
   */
  @Override
  protected void onPmInit() {
    navigateBack(linksToSkip);
  }

  /**
   * Is only enabled when there is a back-navigation link.
   */
  @Override
  protected boolean isPmEnabledImpl() {
    return getNaviLink() != null;
  }

  /**
   * Updates the back-navigation link based on the current navigation history
   * state.
   */
  @Override
  protected void doItImpl() throws Exception {
    navigateBack(linksToSkip);
  }

}

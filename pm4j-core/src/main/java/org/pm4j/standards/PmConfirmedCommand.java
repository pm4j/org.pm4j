package org.pm4j.standards;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.navi.NaviHistory;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.impl.NaviLinkImpl;

public class PmConfirmedCommand extends PmCommandImpl {

  /** Indicates if the confirmation dialog is already shown to the user for this command instance. */
  private boolean confirmDisplayed = false;

  private NaviLink confirmNaviLink;
  private NaviLink execNaviLink;

  public PmConfirmedCommand(PmObject pmParent) {
    this(pmParent, null);
  }

  public PmConfirmedCommand(PmObject pmParent, NaviLink confirmNaviLink) {
    super(pmParent, confirmNaviLink);
    this.confirmNaviLink = confirmNaviLink;
  }

  /**
   * Subclasses may provide here very specific confirmation dialog classes.
   * <p>
   * Attention: Please don't create an anonymous class within this method.
   * The pm4j framework currently can't create a meaningful name for the
   * an anonymous class in this place.
   *
   * @return The PM of the confirmation dialog.
   */
  // TODO olaf: Consider registration of the confirm dialog as a composite child.
  //            This would solve the naming issue, but adds some some child
  //            registration/de-registration code.
  protected PmObject makeConfirmDialogPm() {
    return new PmConfirmDialog(this);
  }

  /**
   * Subclasses may define here a string that appears within the first
   * placeholder within the confirmation message.
   * <p>
   * The default implementation return the title of this command.
   *
   * @return The name of the thing to confirm. May appear in the title and
   *         dialogMessage.
   */
  protected String getNameOfThingToConfirm() {
    return getPmTitle();
  }

  /**
   * Subclasses may define here specific conditions for confirmation dialog display.
   * <p>
   * The default implementation always returns <code>true</code>.
   *
   * @return <code>true</code> if the confirmation dialog should be displayed.
   */
  protected boolean shouldAskForConfirmation() {
    return true;
  }

  @Override
  protected boolean beforeDo() {
    boolean canDo = super.beforeDo();

    if (canDo &&
        !confirmDisplayed &&
        shouldAskForConfirmation()) {

      PmObject confirmDialogPm = makeConfirmDialogPm();

      // XXX olaf: The rich client currently asks the current navilink for the dialog PM...
      //           Check if there is a chance to get a cleaner communication path...
      NaviLinkImpl link = confirmNaviLink != null
        ? NaviLinkImpl.makeNaviScopeParamLink(confirmNaviLink, NAVI_PARAM_NEXT_DLG_PM, confirmDialogPm)
        : NaviLinkImpl.makeNaviScopeParamLink("", NAVI_PARAM_NEXT_DLG_PM, confirmDialogPm);

      navigateTo(link);

      confirmDisplayed = true;

      NaviHistory h = getPmConversation().getPmNaviHistory();
      if (h != null) {
        // FIXME olaf: works for web applications that start Javascript based
        //             popups.
        //             What about the non-popup scenarios with conventional navigation?
        h.setNaviScopeProperty(NAVI_PARAM_NEXT_DLG_PM, confirmDialogPm);
        // getPmConversationImpl().getPmNavigationHandler().redirect(link);
      }

      // Prepare navigation link for the next execution phase:
      // FIXME: funktioniert ggw. so nicht im Rich Client.
      //        für den Web Client jedoch erforderlich...
      if (confirmNaviLink != null) {
        setNaviLink(execNaviLink);
      }

      return false;
    }

    return canDo;
  }

  public boolean isConfirmed() {
    return confirmDisplayed;
  }

  public void setConfirmed(boolean confirmed) {
    this.confirmDisplayed = confirmed;
  }
}

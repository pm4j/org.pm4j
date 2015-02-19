package org.pm4j.standards.pm.test;

import java.util.Locale;

import junit.framework.TestCase;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.navi.impl.NaviLinkImpl;
import org.pm4j.standards.PmConfirmDialog;
import org.pm4j.standards.PmConfirmedCommand;
import org.pm4j.standards.pm.confirmcmdpkg.PmConfirmedCommandInSubPkg;

public class PmConfirmedCommandTest extends TestCase {

  public static class TestPm extends PmConversationImpl {
    public final PmCommand cmdWithStandardConfimation = new PmConfirmedCommand(this);
    public final PmCommand cmdFromSubPkg = new PmConfirmedCommandInSubPkg(this);
  }

  public void testStandardConfirmCommand() {
    TestPm testPm = new TestPm();
    testPm.setPmLocale(Locale.ENGLISH);

    PmCommand cmd = testPm.cmdWithStandardConfimation.doIt();
    assertNotNull(cmd.getNaviLink());

    // TODO: put the confirm dialog directly in the executed command. - simpler interface.
    NaviLinkImpl l = (NaviLinkImpl)cmd.getNaviLink();
    PmConfirmDialog dlg = (PmConfirmDialog) l.getNaviScopeParams().get(PmConfirmedCommand.NAVI_PARAM_NEXT_DLG_PM);
    assertNotNull(dlg);

    assertEquals("My dialog title from resfile.", dlg.getPmTitle());
    assertEquals("My dialog message from resfile.", dlg.dialogMessage.getPmTitle());
    assertEquals("My package specific Yes", dlg.cmdYes.getPmTitle());
    assertEquals("My package specific No", dlg.cmdNo.getPmTitle());

  }

  /**
   * Verifies that string resources are found in the package of the class the
   * owns the command and not in the package the command class that may be
   * defined in some technology package.
   */
  public void testCommandWithIdDefinedInASubPackage() {
    TestPm testPm = new TestPm();
    testPm.setPmLocale(Locale.ENGLISH);

    PmCommand cmd = testPm.cmdFromSubPkg.doIt();
    assertNotNull(cmd.getNaviLink());

    NaviLinkImpl l = (NaviLinkImpl)cmd.getNaviLink();
    PmConfirmDialog dlg = (PmConfirmDialog) l.getNaviScopeParams().get(PmConfirmedCommand.NAVI_PARAM_NEXT_DLG_PM);
    assertNotNull(dlg);

    assertEquals("My dialog title from resfile.", dlg.getPmTitle());
    assertEquals("My dialog message from resfile.", dlg.dialogMessage.getPmTitle());
    assertEquals("My package specific Yes", dlg.cmdYes.getPmTitle());
    assertEquals("My package specific No", dlg.cmdNo.getPmTitle());

  }



}

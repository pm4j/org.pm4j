package org.pm4j.core.pm;

import junit.framework.TestCase;

import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.core.pm.impl.commands.PmCommandSeparator;

public class PmCommandSeparatorTest extends TestCase {

  public static class MyPm extends PmConversationImpl {
    public final PmCommand cmd1 = new PmCommandImpl(this);
    public final PmCommand separator1 = new PmCommandSeparator(this);
    public final PmCommand cmd2 = new PmCommandImpl(this);
    public final PmCommand cmd3 = new PmCommandImpl(this);
    public final PmCommand separator2 = new PmCommandSeparator(this);
    public final PmCommand cmd4 = new PmCommandImpl(this);
    public final PmCommand cmd5 = new PmCommandImpl(this);
  }

  public void testSeparatorsWithAllCommandsAreVisible() {
    MyPm pm = new MyPm();
    assertEquals("5 commands and 2 separators should appear.",
                 7, PmUtil.getVisiblePmCommands(pm).size());
  }

  public void testSeparatorsWithInvisibleFirstSection() {
    MyPm pm = new MyPm();
    pm.cmd1.setPmVisible(false);
    assertEquals("4 commands and 1 separator should appear.",
                 5, PmUtil.getVisiblePmCommands(pm).size());
  }

  public void testSeparatorsWithOneVisibleCmdInMiddleSection() {
    MyPm pm = new MyPm();
    pm.cmd2.setPmVisible(false);
    assertEquals("4 commands and 2 separators should appear.",
                 6, PmUtil.getVisiblePmCommands(pm).size());
  }

  public void testSeparatorsWithNoVisibleCmdInMiddleSection() {
    MyPm pm = new MyPm();
    pm.cmd2.setPmVisible(false);
    pm.cmd3.setPmVisible(false);
    assertEquals("3 commands and 1 separator should appear.",
                 4, PmUtil.getVisiblePmCommands(pm).size());
  }

  public void testSeparatorsWithOneVisibleCmdInLastSection() {
    MyPm pm = new MyPm();
    pm.cmd5.setPmVisible(false);
    assertEquals("4 commands and 2 separators should appear.",
                 6, PmUtil.getVisiblePmCommands(pm).size());
  }

  public void testSeparatorsWithNoVisibleCmdInLastSection() {
    MyPm pm = new MyPm();
    pm.cmd4.setPmVisible(false);
    pm.cmd5.setPmVisible(false);
    assertEquals("3 commands and 1 separator should appear.",
                 4, PmUtil.getVisiblePmCommands(pm).size());
  }

  public void testSeparatorsWithNoVisibleCmdInFirstAndLastSection() {
    MyPm pm = new MyPm();
    pm.cmd1.setPmVisible(false);
    pm.cmd4.setPmVisible(false);
    pm.cmd5.setPmVisible(false);
    assertEquals("2 commands and no separator should appear.",
                 2, PmUtil.getVisiblePmCommands(pm).size());
  }

  public void testSeparatorsNoVisibleCmd() {
    MyPm pm = new MyPm();
    pm.cmd1.setPmVisible(false);
    pm.cmd2.setPmVisible(false);
    pm.cmd3.setPmVisible(false);
    pm.cmd4.setPmVisible(false);
    pm.cmd5.setPmVisible(false);
    assertEquals("No commands and separator should appear.",
                 0, PmUtil.getVisiblePmCommands(pm).size());
  }



}

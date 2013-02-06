package org.pm4j.tools.test;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.impl.PmInitApi;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * A set of junit test support methods.
 *
 * @author olaf boede
 */
public class PmAssert {

  private PmAssert() {
  }

  public static void assertNoMessages(PmObject pm) {
      assertNoMessages("Unexpected messages found.", pm);
  }

  /**
   * Checks if there is a single expected message for the given PM.
   *
   * @param pm the PM that should have a message.
   * @param severity the expected message severity.
   * @param msgString the expected message title sting.
   */
  public static void assertOnePmMessage(PmObject pm, Severity severity, String msgString) {
    List<PmMessage> messages = pm.getPmConversation().getPmMessages(pm, severity);
    if (messages.size() != 1) {
      assertEquals("Only one message with severity '" + severity + "' expected.\n" +
            messagesToString("Found messages: ", messages),
            1, messages.size());
    }
    assertEquals(msgString, messages.get(0).getTitle());
  }

  public static void assertNoMessages(PmObject pm, Severity minSeverity) {
      assertNoMessages("Unexpected messages found.", pm, minSeverity);
  }

  public static void assertNoMessages(String msg, PmObject pm) {
      assertNoMessages(msg, pm, Severity.INFO);
  }

  public static void assertNoMessages(String msg, PmObject pm, Severity minSeverity) {
      String errMsgs = messagesToString(pm, minSeverity);
      if (StringUtils.isNotBlank(errMsgs)) {
          Assert.fail(msg + " " + pm.getPmRelativeName() + ": " + errMsgs);
      }
  }

  public static void assertNoConversationMessages(PmObject pm) {
      assertNoMessages(pm.getPmConversation());
  }

  public static void assertNoConversationMessages(String msg, PmObject pm) {
      assertNoMessages(msg, pm.getPmConversation());
  }

  public static void assertSingleErrorMessage(PmObject pm, String expectedMsg) {
      List<PmMessage> errorMessages = PmMessageUtil.getPmErrors(pm);
      assertEquals("Error message expected but not found: " + expectedMsg, 1, errorMessages.size());
      assertEquals(expectedMsg, errorMessages.get(0).getTitle());
  }

  public static void assertEnabled(PmObject... pms) {
      for (PmObject pm : pms) {
          if (!pm.isPmEnabled()) {
              fail(pm.getPmRelativeName() + " should be enabled.");
          }
      }
  }

  public static void assertNotEnabled(PmObject... pms) {
      for (PmObject pm : pms) {
          if (pm.isPmEnabled()) {
              fail(pm.getPmRelativeName() + " should not be enabled.");
          }
      }
  }

  public static void assertVisible(PmObject... pms) {
      for (PmObject pm : pms) {
          if (!pm.isPmVisible()) {
              fail(pm.getPmRelativeName() + " should be visible.");
          }
      }
  }

  public static void assertNotVisible(PmObject... pms) {
      for (PmObject pm : pms) {
          if (pm.isPmVisible()) {
              fail(pm.getPmRelativeName() + " should not be visible.");
          }
      }
  }

  public static <T> void setValue(PmAttr<T> attr, T value) {
      assertEnabled(attr);
      attr.setValue(value);
      assertNoMessages(attr);
      assertEquals(value, attr.getValue());
  }

  public static void setValueAsString(PmAttr<?> attr, String value) {
      assertEnabled(attr);
      attr.setValueAsString(value);
      assertNoMessages(attr);
      assertEquals(value, attr.getValueAsString());
  }

  public static void doIt(PmCommand cmd) {
      doIt(cmd.getPmRelativeName(), cmd, CommandState.EXECUTED);
  }

  public static void doIt(String msg, PmCommand cmd) {
      doIt(msg, cmd, CommandState.EXECUTED);
  }

  public static void doIt(PmCommand cmd, CommandState expectedState) {
      doIt(cmd.getPmRelativeName(), cmd, expectedState);
  }

  public static void doIt(String msg, PmCommand cmd, CommandState expectedState) {
      assertEnabled(cmd);
      CommandState execState = cmd.doIt().getCommandState();
      if (execState != expectedState) {
          String msgPfx = StringUtils.isEmpty(msg) ? cmd.getPmRelativeName() : msg;
          Assert.assertEquals(msgPfx + messagesToString(" Messages: ", cmd.getPmConversation(), Severity.WARN), expectedState, execState);
      }
  }

  public static void initPmTree(PmObject rootPm) {
      PmInitApi.ensurePmInitialization(rootPm);
      for (PmObject pm : PmUtil.getPmChildren(rootPm)) {
          initPmTree(pm);
      }
  }

  // --- internal helper ---

  private static String messagesToString(PmObject pm, Severity minSeverity) {
      return messagesToString(null, pm, minSeverity);
  }

  private static String messagesToString(String msgPrefix, PmObject pm, Severity minSeverity) {
      List<PmMessage> mlist = PmMessageUtil.getSubTreeMessages(pm, minSeverity);
      return messagesToString(msgPrefix, mlist);
  }

  private static String messagesToString(String msgPrefix, Collection<PmMessage> mlist) {
    if (mlist.isEmpty()) {
        return "";
    } else {
        StringBuilder sb = new StringBuilder();
        for (PmMessage m : mlist) {
            if (sb.length() == 0) {
                sb.append("\n");
            }
            sb.append(m.getPm().getPmRelativeName()).append(": ").append(m).append("\n");
        }
        return StringUtils.defaultString(msgPrefix) + sb.toString();
    }
}

}

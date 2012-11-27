package org.pm4j.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.api.PmMessageUtil;

public class PmAssert {

  public static void assertNoErrors(PmObject pm) {
    assertNoErrors("Error messages found.", pm);
  }

  public static void assertNoErrors(String msg, PmObject pm) {
    String errMsgs = messagesToString(pm, Severity.ERROR);
    if (StringUtils.isNotBlank(errMsgs)) {
      Assert.fail(msg + " " + pm.getPmRelativeName() + ": " + errMsgs);
    }
  }

  public static void assertNoConversationErrors(PmObject pm) {
    assertNoErrors(pm.getPmConversation());
  }

  public static void assertNoConversationErrors(String msg, PmObject pm) {
    assertNoErrors(msg, pm.getPmConversation());
  }

  public static <T> void setValue(PmAttr<T> attr, T value) {
    setValue(null, attr, value);
  }

  public static <T> void setValue(String msg, PmAttr<T> attr, T value) {
    assertTrue("The attribute should be editable.", attr.isPmEnabled());
    attr.setValue(value);
    assertNoErrors(attr);
    assertEquals(value, attr.getValue());
  }

  public static void setValueAsString(PmAttr<?> attr, String value) {
    setValueAsString(null, attr, value);
  }

  public static void setValueAsString(String msg, PmAttr<?> attr, String value) {
    assertTrue("The attribute should be editable.", attr.isPmEnabled());
    attr.setValueAsString(value);
    assertNoErrors(attr);
    assertEquals(value, attr.getValueAsString());
  }

  public static void doIt(PmCommand cmd, CommandState expectedState) {
    doIt(null, cmd, expectedState);
  }

  public static void doIt(String msg, PmCommand cmd, CommandState expectedState) {
    assertTrue("command " + cmd.getPmName() + " should be enabled", cmd.isPmEnabled());
    CommandState execState = cmd.doIt().getCommandState();
    if (execState != expectedState) {
      String msgPfx = StringUtils.isEmpty(msg)
          ? cmd.getPmRelativeName()
          : msg;
      Assert.assertEquals(
          msgPfx + messagesToString(" Messages: ", cmd, Severity.WARN),
          expectedState, execState);
    }
  }

  private static String messagesToString(PmObject pm, Severity minSeverity) {
    return messagesToString(null, pm, minSeverity);
  }

  private static String messagesToString(String msgPrefix, PmObject pm, Severity minSeverity) {
    List<PmMessage> mlist = PmMessageUtil.getSubTreeMessages(pm, Severity.ERROR);
    if (mlist.isEmpty()) {
      return "";
    }
    else {
      StringBuilder sb = new StringBuilder();
      for (PmMessage m : mlist) {
        if (sb.length() > 0) sb.append(", ");
        sb.append(m.getPm().getPmRelativeName()).append(": ").append(m);
      }
      return StringUtils.defaultString(msgPrefix) + sb.toString();
    }
  }

}

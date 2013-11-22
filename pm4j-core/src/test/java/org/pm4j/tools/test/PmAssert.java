package org.pm4j.tools.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * A set of junit test support methods.
 *
 * @author olaf boede
 */
public class PmAssert {

    private PmAssert() {
    }

    /**
     * Checks if exactly the given message text(s) exist for the given PM.
     * <p>
     * Notice that a {@link org.pm4j.core.pm.PmConversation} will report all messages within its
     * scope. Any other PM will only report its own messages.
     *
     * @param pm
     *            the PM to check the messages for.
     * @param expectedMessages
     *            the set of expected message texts.
     */
    // @formatter:off
    public static void assertMessageText(PmObject pm, String... expectedMessages) {
        List<PmMessage> messages = PmMessageUtil.getPmMessages(pm);
        if (messages.size() != expectedMessages.length) {
            fail("Expected " + expectedMessages.length +
                    " messages but found " + messages.size() + " messages." +
                    "\nFound messages: " + messages +
                    "\nExpected messages: " + Arrays.asList(expectedMessages) +
                    "\nPM context: " + PmUtil.getAbsoluteName(pm));
        }

        Set<String> expectedSet = new HashSet<String>(Arrays.asList(expectedMessages));
        for (PmMessage m : messages) {
            if (!expectedSet.contains(m.getTitle())) {
                fail("Unexpected message found." +
                        "\nFound messages: " + messages +
                        "\nExpected messages: " + Arrays.asList(expectedMessages) +
                        "\nPM context: " + PmUtil.getAbsoluteName(pm));
            }
        }
    }
    // @formatter:on

    /**
     * Checks that there are no active {@link PmMessage}s for the given PM (incl. it's sub-PMs).
     *
     * @param pm
     *            the PM to perform the check for.
     */
    public static void assertNoMessagesInSubTree(PmObject pm) {
        assertNoMessagesInSubTree("Unexpected messages found.", pm);
    }

    /**
     * Checks that there are no active {@link PmMessage}s for the given PM (incl. it's sub-PMs).
     *
     * @param msg
     *            the assert message to display if the check fails.
     * @param pm
     *            the PM to perform the check for.
     */
    public static void assertNoMessagesInSubTree(String msg, PmObject pm) {
        assertNoMessagesInSubTree(msg, pm, Severity.INFO);
    }

    /**
     * Checks that there are no active {@link PmMessage}s with the given
     * <code>minSeverity</code> for the given PM (incl. it's sub-PMs)
     *
     * @param pm
     *            the PM to perform the check for.
     */
    public static void assertNoMessagesInSubTree(PmObject pm, Severity minSeverity) {
        assertNoMessagesInSubTree("Unexpected messages found.", pm, minSeverity);
    }

    /**
     * Checks that there are no active {@link PmMessage}s with the given
     * <code>minSeverity</code> for the given PM (incl. it's sub-PMs)
     *
     * @param msg
     *            the assert message to display if the check fails.
     * @param pm
     *            the PM to perform the check for.
     */
    public static void assertNoMessagesInSubTree(String msg, PmObject pm, Severity minSeverity) {
        String errMsgs = subTreeMessagesToString(pm, minSeverity);
        if (StringUtils.isNotBlank(errMsgs)) {
            Assert.fail(msg + " " + pm.getPmRelativeName() + ": " + errMsgs);
        }
    }

    /**
     * Checks that there are no active {@link PmMessage}s within the
     * {@link org.pm4j.core.pm.PmConversation} of the given PM.
     *
     * @param pm
     *            the PM to perform the check for.
     */
    public static void assertNoMessagesInConversation(PmObject pm) {
        assertNoMessagesInSubTree(pm.getPmConversation());
    }

    /**
     * Checks that there are no active {@link PmMessage}s within the
     * {@link org.pm4j.core.pm.PmConversation} of the given PM.
     *
     * @param msg
     *            the assert message to display if the check fails.
     * @param pm
     *            the PM to perform the check for.
     */
    public static void assertNoMessagesInConversation(String msg, PmObject pm) {
        assertNoMessagesInSubTree(msg, pm.getPmConversation());
    }

    /**
     * Checks if there is the expected error message active for the given PM.
     *
     * @param pm
     *            the PM to check.
     * @param expectedMsg
     *            the expexted error message.
     */
    public static void assertSingleErrorMessage(PmObject pm, String expectedMsg) {
        List<PmMessage> errorMessages = PmMessageUtil.getPmErrors(pm);
        assertEquals("Error message expected but not found: " + expectedMsg, 1, errorMessages.size());
        assertEquals(expectedMsg, errorMessages.get(0).getTitle());
    }

    /**
     * Checks if the given set of PMs is enabled.
     *
     * @param pms
     *            the PMs to check.
     */
    public static void assertEnabled(PmObject... pms) {
        for (PmObject pm : pms) {
            if (!pm.isPmEnabled()) {
                fail(pm.getPmRelativeName() + " should be enabled.");
            }
        }
    }

    /**
     * Checks if the given set of PMs is not enabled.
     *
     * @param pms
     *            the PMs to check.
     */
    public static void assertNotEnabled(PmObject... pms) {
        for (PmObject pm : pms) {
            if (pm.isPmEnabled()) {
                fail(pm.getPmRelativeName() + " should not be enabled.");
            }
        }
    }

    /**
     * Checks if the given set of PMs is visible.
     *
     * @param pms
     *            the PMs to check.
     */
    public static void assertVisible(PmObject... pms) {
        for (PmObject pm : pms) {
            if (!pm.isPmVisible()) {
                fail(pm.getPmRelativeName() + " should be visible.");
            }
        }
    }

    /**
     * Checks if the given set of PMs is not visible.
     *
     * @param pms
     *            the PMs to check.
     */
    public static void assertNotVisible(PmObject... pms) {
        for (PmObject pm : pms) {
            if (pm.isPmVisible()) {
                fail(pm.getPmRelativeName() + " should not be visible.");
            }
        }
    }

    /**
     * Checks if the given set of {@link PmAttr}s is required.
     *
     * @param pms
     *            the PMs to check.
     */
    public static void assertRequired(PmAttr<?>... pms) {
        for (PmAttr<?> pm : pms) {
            if (!pm.isRequired()) {
                fail("\"" + pm.getPmRelativeName() + "\" attribute should be required.");
            }
        }
    }

    /**
     * Checks if the given set of {@link PmAttr}s is not required.
     *
     * @param pms
     *            the PMs to check.
     */
    public static void assertNotRequired(PmAttr<?>... pms) {
        for (PmAttr<?> pm : pms) {
            if (pm.isRequired()) {
                fail("\"" + pm.getPmRelativeName() + "\" attribute should not be required, i.e. optional.");
            }
        }
    }

    /**
     * Performs a checked set value operation.
     * <p>
     * Checks first if the given attribute is enabled.
     * <p>
     * After setting the value it verifies if there are no messages within the subtree of the
     * given PM.
     * <p>
     * Verifies the the getValue() operation provides the expected value.
     *
     * @param attr
     *            the attribute to assign the value to.
     * @param value
     *            the value to assign.
     */
    public static <T> void setValue(PmAttr<T> attr, T value) {
        assertEnabled(attr);
        attr.setValue(value);
        assertNoMessagesInSubTree(attr);
        assertEquals(value, attr.getValue());
    }

    /**
     * Performs a checked set value operation.
     * <p>
     * Checks first if the given attribute is enabled.
     * <p>
     * After setting the value it verifies if there are no messages within the subtree of the
     * given PM.
     * <p>
     * Verifies the the getValue() operation provides the expected value.
     *
     * @param attr
     *            the attribute to assign the value to.
     * @param value
     *            the value to assign.
     */
    public static void setValueAsString(PmAttr<?> attr, String value) {
        assertEnabled(attr);
        attr.setValueAsString(value);
        assertNoMessagesInSubTree(attr);
        assertEquals(value, attr.getValueAsString());
    }

    /**
     * Executes the given command.
     * <ul>
     * <li>Checks first if the command is enabled.</li>
     * <li>Checks if the executed command has the state
     * {@link org.pm4j.core.pm.PmCommand.CommandState#EXECUTED}.</li>
     * <li>In case of an unexpected outcome it reports all messages found in the conversation.</li>
     * </ul>
     *
     * @param cmd
     *            the command to execute.
     */
    public static void doIt(PmCommand cmd) {
        doIt(cmd.getPmRelativeName(), cmd, CommandState.EXECUTED);
    }

    /**
     * Executes the given command.
     * <ul>
     * <li>Checks first if the command is enabled.</li>
     * <li>Checks if the executed command has the state
     * {@link org.pm4j.core.pm.PmCommand.CommandState#EXECUTED}.</li>
     * <li>In case of an unexpected outcome it reports all messages found in the conversation.</li>
     * </ul>
     *
     * @param msg
     *            the assert message to display if the operation fails.
     * @param cmd
     *            the command to execute.
     */
    public static void doIt(String msg, PmCommand cmd) {
        doIt(msg, cmd, CommandState.EXECUTED);
    }

    /**
     * Executes the given command.
     * <ul>
     * <li>Checks first if the command is enabled.</li>
     * <li>Checks if the executed command has the expected execution result state.</li>
     * <li>In case of an unexpected outcome it reports all messages found in the conversation.</li>
     * </ul>
     *
     * @param cmd
     *            the command to execute.
     * @param expectedState
     *            the expected execution result state.
     */
    public static void doIt(PmCommand cmd, CommandState expectedState) {
        doIt(cmd.getPmRelativeName(), cmd, expectedState);
    }

    /**
     * Executes the given command.
     * <ul>
     * <li>Checks first if the command is enabled.</li>
     * <li>Checks if the executed command has the expected execution result state.</li>
     * <li>In case of an unexpected outcome it reports all messages found in the conversation.</li>
     * </ul>
     *
     * @param msg
     *            the assert message to display if the operation fails.
     * @param cmd
     *            the command to execute.
     * @param expectedState
     *            the expected execution result state.
     */
    public static void doIt(String msg, PmCommand cmd, CommandState expectedState) {
        assertEnabled(cmd);
        CommandState execState = cmd.doIt().getCommandState();
        if (execState != expectedState) {
            String msgPfx = StringUtils.isEmpty(msg) ? cmd.getPmRelativeName() : msg;
            Assert.assertEquals(msgPfx + subTreeMessagesToString(" Messages: ", cmd.getPmConversation(), Severity.WARN), expectedState, execState);
        }
    }

    // -- internal helper methods --

    private static String subTreeMessagesToString(PmObject pm, Severity minSeverity) {
        return subTreeMessagesToString(null, pm, minSeverity);
    }

    private static String subTreeMessagesToString(String msgPrefix, PmObject pm, Severity minSeverity) {
        List<PmMessage> mlist = PmMessageUtil.getSubTreeMessages(pm, minSeverity);
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

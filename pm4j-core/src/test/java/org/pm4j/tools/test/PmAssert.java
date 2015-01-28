package org.pm4j.tools.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.api.PmValidationApi;
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
     * Checks if exactly the given message text(s) exist for the given PM (incl. its sub-PMs)
     * and the given minimal message severity.
     * <p>
     *
     * @param rootPm
     *            the root PM of the subtree to check the messages for.
     * @param minSeverity
     *            the minimum severity to consider.
     * @param expectedMessages
     *            the set of expected message texts.
     */
    // @formatter:off
    public static void assertMessageText(PmObject rootPm, Severity minSeverity, String... expectedMessages) {
        List<PmMessage> messages = PmMessageApi.getPmTreeMessages(rootPm, minSeverity);
        if (messages.size() != expectedMessages.length) {
            StringBuilder sb = new StringBuilder("Expected " + expectedMessages.length +
                    " messages but found " + messages.size() + " messages." +
                    "\nFound messages: " + messages +
                    "\nExpected messages: " + Arrays.asList(expectedMessages) +
                    "\nPM context: " + PmUtil.getAbsoluteName(rootPm));
            if (messages.size() > 0) {
                sb.append("\nFound message details:");
                for (PmMessage m : messages) {
                    sb.append("\n\t" + getMessageTitlePrependByPmRelativeName(m));
                }
            }
            fail(sb.toString());
        }

        Set<String> expectedSet = new HashSet<String>(Arrays.asList(expectedMessages));
        for (PmMessage m : messages) {
            if (!expectedSet.contains(m.getTitle()) &&
                !expectedSet.contains(getMessageTitlePrependByPmRelativeName(m))) {
                fail("Unexpected message found." +
                        "\nFound messages: " + messages +
                        "\nExpected messages: " + Arrays.asList(expectedMessages) +
                        "\nPM context: " + PmUtil.getAbsoluteName(rootPm));
            }
        }
    }
    // @formatter:on

    /**
     * Checks if exactly the given message text(s) exist for the given PM (incl. its sub-PMs).
     * <p>
     *
     * @param rootPm
     *            the root PM of the subtree to check the messages for.
     * @param expectedMessages
     *            the set of expected message texts.
     */
    public static void assertMessageText(PmObject rootPm, String... expectedMessages) {
        assertMessageText(rootPm, Severity.INFO, expectedMessages);
    }

    /**
     * Checks that there are no active {@link PmMessage}s for the given PM (incl. its sub-PMs).
     *
     * @param pm
     *            the PM to perform the check for.
     */
    public static void assertNoMessagesInSubTree(PmObject pm) {
        assertNoMessagesInSubTree("Unexpected messages found.", pm);
    }

    /**
     * Checks that there are no active {@link PmMessage}s for the given PM (incl. its sub-PMs).
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
     * <code>minSeverity</code> for the given PM (incl. its sub-PMs)
     *
     * @param pm
     *            the PM to perform the check for.
     * @param minSeverity
     *            the minimum severity to consider.
     */
    public static void assertNoMessagesInSubTree(PmObject pm, Severity minSeverity) {
        assertNoMessagesInSubTree("Unexpected messages found.", pm, minSeverity);
    }

    /**
     * Checks that there are no active {@link PmMessage}s with the given
     * <code>minSeverity</code> for the given PM (incl. its sub-PMs)
     *
     * @param msg
     *            the assert message to display if the check fails.
     * @param pm
     *            the PM to perform the check for.
     * @param minSeverity
     *            the minimum severity to consider.
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
     * Checks if there is the expected error message active for the given PM (incl. its
     * sub-PMs).
     *
     * @param rootPm
     *            The root PM of the subtree to check the messages for.
     * @param expectedMsgInSubtree
     *            The expected message text in rootPm or any of its sub-PMs.
     * @deprecated Please use {@link PmAssert.assertMessageText(PmObject rootPm, Severity
     *             minSeverity, String... expectedMessages)} instead.
     */
    @Deprecated
    public static void assertSingleErrorMessage(PmObject rootPm, String expectedMsgInSubtree) {
        List<PmMessage> errorMessages = PmMessageApi.getPmTreeMessages(rootPm, Severity.ERROR);
        assertEquals("Error message expected but not found: " + expectedMsgInSubtree, 1, errorMessages.size());
        assertEquals(expectedMsgInSubtree, errorMessages.get(0).getTitle());
    }

    /**
     * Checks if a message text - severity combination exists for the given PM (incl. its
     * sub-PMs).
     *
     * @param expectedMsgInSubtree
     *            The expected message text in rootPm or any of its sub-PMs.
     * @param severity
     *            The expected message severity.
     * @param rootPm
     *            The root PM of the subtree to check the messages for.
     */
    public static void assertMessage(String expectedMsgInSubtree, Severity severity, PmObject rootPm) {
        List<PmMessage> messages = PmMessageApi.getPmTreeMessages(rootPm, severity);
        List<String> messageStrings = new ArrayList<String>();
        for (PmMessage message : messages) {
            if (StringUtils.equals(expectedMsgInSubtree, message.getTitle()) && message.getSeverity() == severity) {
                return;
            }
            messageStrings.add(message.getSeverity().name() + ": " + message.getTitle());
        }
        assertEquals(severity.name() + ": " + expectedMsgInSubtree, StringUtils.join(messageStrings, "\n"));
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
     * Checks if each of the given PM's is marked as changed.
     *
     * @param pms
     *            the PMs to check.
     */
    public static void assertChanged(PmDataInput... pms) {
        for (PmDataInput pm : pms) {
            if (!pm.isPmValueChanged()) {
                fail(pm.getPmRelativeName() + " should be in a changed state.");
            }
        }
    }

    /**
     * Checks if each of the given PM's is not marked as changed.
     *
     * @param pms
     *            the PMs to check.
     */
    public static void assertNotChanged(PmDataInput... pms) {
        for (PmDataInput pm : pms) {
            if (pm.isPmValueChanged()) {
                fail(pm.getPmRelativeName() + " should not be in a changed state.");
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
     * Checks if the given attribute has the expected option titles.
     *
     * @param expectedTitles
     *            A comma separated string with all expected titles, e.g. "A, B, C".
     * @param pmAttr
     *            The attribute to check.
     */
    public static void assertOptionTitles(String expectedTitles, PmAttr<?> pmAttr) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (PmOption o : pmAttr.getOptionSet().getOptions()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(o.getPmTitle());
            first = false;
        }
        assertEquals(pmAttr.getPmRelativeName() + ": option titles", expectedTitles, sb.toString());
    }

    /**
     * Checks if the given attribute has the expected option titles.
     *
     * @param expectedIds
     *            A comma separated string with all expected IDs, e.g. "A, B, C".
     * @param pmAttr
     *            The attribute to check.
     */
    public static void assertOptionIds(String expectedIds, PmAttr<?> pmAttr) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (PmOption o : pmAttr.getOptionSet().getOptions()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(o.getIdAsString());
            first = false;
        }
        assertEquals(pmAttr.getPmRelativeName() + ": option ids", expectedIds, sb.toString());
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

    /**
     * Validates the PM tree having the given root instance. Fails if the subtree has any
     * messages after the validation call.
     *
     * @param rootPm
     *            The root of the PM tree to validate.
     */
    public static void validateSuccessful(PmDataInput rootPm) {
        if (!PmValidationApi.validateSubTree(rootPm)) {
            List<String> msgStrings = new ArrayList<String>();
            for (PmMessage m : PmMessageApi.getPmTreeMessages(rootPm, Severity.INFO)) {
                msgStrings.add(m.toString());
            }
            Assert.fail("Unexpected messages after validation of " + rootPm.getPmRelativeName() + ":\n" + StringUtils.join(msgStrings.toArray(), "\n\t"));
        }
    }

    /**
     * Validates the PM tree having the given root instance. Fails if the subtree has validated
     * successfully or doesn't contain exactly the expected error messages.
     *
     * @param rootPm
     *            The root of the PM tree to validate.
     * @param expectedErrorMsgsInSubtree
     *            The expected error messages in rootPm or its PM subtree.
     */
    public static void validateNotSuccessful(PmDataInput rootPm, String... expectedErrorMsgsInSubtree) {
        validateNotSuccessful(rootPm, Severity.ERROR, expectedErrorMsgsInSubtree);
    }

    /**
     * Validates the PM tree having the given root instance. Fails if the subtree has validated
     * successfully or doesn't contain exactly the expected messages.
     *
     * @param rootPm
     *            The root of the PM tree to validate.
     * @param minSeverity
     *            The minimum severity to consider.
     * @param expectedMessagesInSubtree
     *            The expected messages in rootPm or its PM subtree.
     */
    public static void validateNotSuccessful(PmDataInput rootPm, Severity minSeverity, String... expectedMessagesInSubtree) {
        if (PmValidationApi.validateSubTree(rootPm)) {
            Assert.fail("Unexpected successful validation of " + rootPm.getPmRelativeName());
        }
        assertMessageText(rootPm, minSeverity, expectedMessagesInSubtree);
    }

    // -- internal helper methods --

    private static String getMessageTitlePrependByPmRelativeName(PmMessage m) {
        return m.getPm().getPmRelativeName() + ": " + m.getTitle();
    }

    private static String subTreeMessagesToString(PmObject pm, Severity minSeverity) {
        return subTreeMessagesToString(null, pm, minSeverity);
    }

    private static String subTreeMessagesToString(String msgPrefix, PmObject pm, Severity minSeverity) {
        List<PmMessage> mlist = PmMessageApi.getPmTreeMessages(pm, minSeverity);
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

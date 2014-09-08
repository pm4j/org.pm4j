package org.pm4j.core.pm.impl.message;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.converter.PmConverterErrorMessage;

/**
 * Test cases for {@link AddPmMessageCallbackBase},
 *
 * @author Olaf Boede
 */
public class AddPmMessageCallbackBaseTest {

  private PmConversationImpl conv = new PmConversationImpl();
  private MyAddPmMessageCallback addMessageCallback = new MyAddPmMessageCallback();

  @Before
  public void setUp() {
    conv.setAddPmMessageCallback(addMessageCallback);
  }

  @Test
  public void testAddPmMessageCallbackIsUsed() {
    PmMessageApi.addMessage(conv, Severity.INFO, PmConstants.MSGKEY_VALIDATION_MISSING_REQUIRED_VALUE, "a field");
    assertEquals("[Please enter a value into \"a field\".]", addMessageCallback.registeredMessages.toString());
    assertEquals("[Please enter a value into \"a field\".]", conv.getPmMessages().toString());
  }

  @Test
  public void testAddPmMessageCallbackBaseFiltersConverterErrors() {
    conv.addPmMessage(new PmConverterErrorMessage(conv, null, PmConstants.MSGKEY_VALIDATION_NUMBER_CONVERSION_FROM_STRING_FAILED, "myNumfield"));
    assertEquals("[]", addMessageCallback.registeredMessages.toString());
    assertEquals("[Unable to convert the entered string to a numeric value in field \"myNumfield\".]", conv.getPmMessages().toString());
  }

  /** A test callback that records all messages passed to beforeAddMessageImpl(). */
  static class MyAddPmMessageCallback extends AddPmMessageCallbackBase {
    private List<PmMessage> registeredMessages = new ArrayList<PmMessage>();

    @Override
    protected PmMessage beforeAddMessageImpl(PmMessage message) {
      registeredMessages.add(message);
      return message;
    }
  }
}

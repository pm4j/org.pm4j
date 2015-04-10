package org.pm4j.core.event.impl;

import junit.framework.TestCase;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.tools.test._RecordingPmEventListener;

public class AbstractEventProvidingObjectTest extends TestCase {

  public void testAllChangeListener() {
    PmConversation instance = new PmConversationImpl();
    _RecordingPmEventListener changeListerner = new _RecordingPmEventListener();

    PmEventApi.addPmEventListener(instance, PmEvent.ALL, changeListerner);
    assertEquals(0, changeListerner.getEvents().size());

    PmEventApi.firePmEvent(instance, PmEvent.ALL);
    assertEquals(1, changeListerner.getEvents().size());

    PmEventApi.removePmEventListener(instance, changeListerner);
    PmEventApi.firePmEvent(instance, PmEvent.ALL);
    assertEquals(1, changeListerner.getEvents().size());
  }

  public void testSpecificChangeListener() {
    PmConversation instance = new PmConversationImpl();
    _RecordingPmEventListener changeListerner = new _RecordingPmEventListener();

    PmEventApi.addPmEventListener(instance, PmEvent.TITLE_CHANGE, changeListerner);
    assertEquals(0, changeListerner.getEvents().size());

    PmEventApi.firePmEvent(instance, PmEvent.ALL);
    assertEquals(1, changeListerner.getEvents().size());

    PmEventApi.firePmEvent(instance, PmEvent.VALUE_CHANGE);
    assertEquals(1, changeListerner.getEvents().size());

    PmEventApi.firePmEvent(instance, PmEvent.TITLE_CHANGE);
    assertEquals(2, changeListerner.getEvents().size());

    PmEventApi.removePmEventListener(instance, PmEvent.TITLE_CHANGE, changeListerner);
    PmEventApi.firePmEvent(instance, PmEvent.TITLE_CHANGE);
    assertEquals(2, changeListerner.getEvents().size());
  }

  public void testMixChangeListener() {
    PmConversation instance = new PmConversationImpl();
    _RecordingPmEventListener changeListerner = new _RecordingPmEventListener();

    PmEventApi.addPmEventListener(instance, PmEvent.TITLE_CHANGE | PmEvent.VALUE_CHANGE,
                                changeListerner);
    assertEquals(0, changeListerner.getEvents().size());

    PmEventApi.firePmEvent(instance, PmEvent.ALL);
    assertEquals(1, changeListerner.getEvents().size());

    PmEventApi.firePmEvent(instance, PmEvent.VALUE_CHANGE);
    assertEquals(2, changeListerner.getEvents().size());

    PmEventApi.firePmEvent(instance, PmEvent.TITLE_CHANGE);
    assertEquals(3, changeListerner.getEvents().size());

    PmEventApi.removePmEventListener(instance, PmEvent.TITLE_CHANGE, changeListerner);
    PmEventApi.firePmEvent(instance, PmEvent.TITLE_CHANGE);
    assertEquals(3, changeListerner.getEvents().size());

    PmEventApi.firePmEvent(instance, PmEvent.VALUE_CHANGE);
    assertEquals(4, changeListerner.getEvents().size());
  }

}

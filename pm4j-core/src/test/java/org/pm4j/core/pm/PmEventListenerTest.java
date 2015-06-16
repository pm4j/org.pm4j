package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmEventListenerBase;
import org.pm4j.tools.test._RecordingPmEventListener;

public class PmEventListenerTest {

  PmConversation pm = new PmConversationImpl();
  _RecordingPmEventListener listerner = new _RecordingPmEventListener();
  List<PmEventListener> namedListenerCalls = new ArrayList<PmEventListener>();

  @Test
  public void testAllChangeListener() {
    PmEventApi.addPmEventListener(pm, PmEvent.ALL, listerner);
    assertEquals(0, listerner.getEvents().size());

    PmEventApi.firePmEvent(pm, PmEvent.ALL);
    assertEquals(1, listerner.getEvents().size());

    PmEventApi.removePmEventListener(pm, listerner);
    PmEventApi.firePmEvent(pm, PmEvent.ALL);
    assertEquals(1, listerner.getEvents().size());
  }

  @Test
  public void testSpecificChangeListener() {
    PmEventApi.addPmEventListener(pm, PmEvent.TITLE_CHANGE, listerner);
    assertEquals(0, listerner.getEvents().size());

    PmEventApi.firePmEvent(pm, PmEvent.ALL);
    assertEquals(1, listerner.getEvents().size());

    PmEventApi.firePmEvent(pm, PmEvent.VALUE_CHANGE);
    assertEquals(1, listerner.getEvents().size());

    PmEventApi.firePmEvent(pm, PmEvent.TITLE_CHANGE);
    assertEquals(2, listerner.getEvents().size());

    PmEventApi.removePmEventListener(pm, listerner);
    PmEventApi.firePmEvent(pm, PmEvent.TITLE_CHANGE);
    assertEquals(2, listerner.getEvents().size());
  }

  @Test
  public void testMixChangeListener() {
    PmEventApi.addPmEventListener(pm, PmEvent.TITLE_CHANGE | PmEvent.VALUE_CHANGE, listerner);
    assertEquals(0, listerner.getEvents().size());

    PmEventApi.firePmEvent(pm, PmEvent.ALL);
    assertEquals(1, listerner.getEvents().size());

    PmEventApi.firePmEvent(pm, PmEvent.VALUE_CHANGE);
    assertEquals(2, listerner.getEvents().size());

    PmEventApi.firePmEvent(pm, PmEvent.TITLE_CHANGE);
    assertEquals(3, listerner.getEvents().size());

    PmEventApi.firePmEvent(pm, PmEvent.VALUE_CHANGE);
    assertEquals(4, listerner.getEvents().size());
  }

  @Test
  public void testListenerCallbackSequence() {
    PmEventApi.addPmEventListener(pm, PmEvent.TITLE_CHANGE, new NamedListener("0"));
    NamedListener l1 = PmEventApi.addPmEventListener(pm, PmEvent.TITLE_CHANGE, new NamedListener("1"));
    PmEventApi.addPmEventListener(pm, PmEvent.TITLE_CHANGE, new NamedListener("2"));

    PmEventApi.firePmEvent(pm, PmEvent.TITLE_CHANGE);
    assertEquals("[0, 1, 2]", namedListenerCalls.toString());

    PmEventApi.removePmEventListener(pm, l1);
    namedListenerCalls.clear();
    PmEventApi.firePmEvent(pm, PmEvent.TITLE_CHANGE);
    assertEquals("[0, 2]", namedListenerCalls.toString());

    PmEventApi.addPmEventListener(pm, PmEvent.TITLE_CHANGE, l1);
    namedListenerCalls.clear();
    PmEventApi.firePmEvent(pm, PmEvent.TITLE_CHANGE);
    assertEquals("[0, 2, 1]", namedListenerCalls.toString());
  }

  @Test
  public void testWeakListener() throws InterruptedException {
    PmEventApi.addPmEventListener(pm, PmEvent.TITLE_CHANGE, new NamedListener("0"));
    NamedListener l1 = PmEventApi.addWeakPmEventListener(pm, PmEvent.TITLE_CHANGE, new NamedListener("1"));
    PmEventApi.addPmEventListener(pm, PmEvent.TITLE_CHANGE, new NamedListener("2"));

    PmEventApi.firePmEvent(pm, PmEvent.TITLE_CHANGE);
    assertEquals("[0, 1, 2]", namedListenerCalls.toString());

    namedListenerCalls.clear();
    l1 = null;

    // Only a GC call makes sure that the weak reference gets cleared.
    // A removePmEventListener call is much more reliable for business logic usage.
    System.gc();

    PmEventApi.firePmEvent(pm, PmEvent.TITLE_CHANGE);
    assertEquals("[0, 2]", namedListenerCalls.toString());
  }

  /** A test that covers some internal an array size optimization code. */
  @Test
  public void testRemoveAndAddListener() {
    NamedListener l0 = PmEventApi.addPmEventListener(pm, PmEvent.TITLE_CHANGE, new NamedListener("0"));
    NamedListener l1 = PmEventApi.addPmEventListener(pm, PmEvent.TITLE_CHANGE, new NamedListener("1"));

    PmEventApi.firePmEvent(pm, PmEvent.TITLE_CHANGE);
    assertEquals("[0, 1]", namedListenerCalls.toString());

    PmEventApi.removePmEventListener(pm, l1);
    namedListenerCalls.clear();
    PmEventApi.firePmEvent(pm, PmEvent.TITLE_CHANGE);
    assertEquals("[0]", namedListenerCalls.toString());

    PmEventApi.addPmEventListener(pm, PmEvent.TITLE_CHANGE, l1);
    namedListenerCalls.clear();
    PmEventApi.firePmEvent(pm, PmEvent.TITLE_CHANGE);
    assertEquals("[0, 1]", namedListenerCalls.toString());

    PmEventApi.removePmEventListener(pm, l0);
    PmEventApi.removePmEventListener(pm, l1);
    namedListenerCalls.clear();
    PmEventApi.firePmEvent(pm, PmEvent.TITLE_CHANGE);
    assertEquals("[]", namedListenerCalls.toString());
  }

  class NamedListener extends PmEventListenerBase {

    public NamedListener(String name) {
      super(name);
    }

    @Override
    public void handleEvent(PmEvent event) {
      namedListenerCalls.add(this);
    }
  }


}

package org.pm4j.core.pm.api;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.pm4j.core.pm.PmEvent.VALUE_CHANGE;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmEventListener.PostProcessor;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.RecursivePmEventProcessor;

public class PmEventApiTest {

  private MyPm myPm = new MyPm();
  private TestListener elementListener = new TestListener();
  private TestListener attrListener = new TestListener();

  @Test
  public void testFireSingleEventForAttribute() {
    PmEventApi.addWeakPmEventListener(myPm, VALUE_CHANGE, elementListener);
    PmEventApi.addWeakPmEventListener(myPm.s, VALUE_CHANGE, attrListener);

    PmEventApi.firePmEvent(myPm.s, VALUE_CHANGE);

    assertEquals(1, attrListener.receivedEvents.size());
    assertEquals(0, elementListener.receivedEvents.size());
  }

  @Test
  public void testFireAllChangeEventsForAttributeAndObserveValueChange() {
    PmEventApi.addWeakPmEventListener(myPm, VALUE_CHANGE, elementListener);
    PmEventApi.addWeakPmEventListener(myPm.s, VALUE_CHANGE, attrListener);

    PmEventApi.firePmEvent(myPm.s, PmEvent.ALL_CHANGE_EVENTS);

    assertEquals(1, attrListener.receivedEvents.size());
    assertEquals(0, elementListener.receivedEvents.size());
  }

  @Test
  public void testObserveEventInHierarchy() {
    PmEventApi.addWeakPmEventListener(myPm, VALUE_CHANGE, elementListener);
    PmEventApi.addWeakPmEventListener(myPm.s, VALUE_CHANGE, attrListener);
    TestListener elemHierarchyListener = new TestListener();
    PmEventApi.addHierarchyListener(myPm, VALUE_CHANGE, elemHierarchyListener);

    PmEventApi.firePmEvent(myPm.s, VALUE_CHANGE);

    assertEquals(1, attrListener.receivedEvents.size());
    assertEquals(0, elementListener.receivedEvents.size());
    assertEquals(1, elemHierarchyListener.receivedEvents.size());
  }

  @Test
  public void testFireEventRecursively() {
    PmEventApi.addWeakPmEventListener(myPm, VALUE_CHANGE, elementListener);
    PmEventApi.addWeakPmEventListener(myPm.s, VALUE_CHANGE, attrListener);

    new RecursivePmEventProcessor(myPm, VALUE_CHANGE).doIt();

    assertEquals(1, attrListener.receivedEvents.size());
    assertEquals(1, elementListener.receivedEvents.size());
    assertEquals(myPm, elementListener.receivedEvents.get(0).getPm());
    assertEquals(myPm.s, attrListener.receivedEvents.get(0).getPm());
  }

  @Test
  public void testPostEventCallback() {
    // setup
    final String[] receivedPostProcessingPayload = new String[1];

    final PostProcessor<String> eventPostProcessor = new PostProcessor<String>() {
      @Override
      public void postProcess(PmEvent event, String postProcessPayload) {
        assertNull(receivedPostProcessingPayload[0]);
        receivedPostProcessingPayload[0] = postProcessPayload;
      }
    };

    PmEventApi.addPmEventListener(myPm, PmEvent.TITLE_CHANGE, new PmEventListener() {
      @Override
      public void handleEvent(PmEvent event) {
        event.addPostProcessingListener(eventPostProcessor, "This is my payload.");
        assertNull("The post processing should not happen immediately.", receivedPostProcessingPayload[0]);
      }

    });

    // test
    PmEventApi.firePmEvent(myPm, PmEvent.TITLE_CHANGE);
    assertEquals("This is my payload.", receivedPostProcessingPayload[0]);
  }

  @Test
  public void testPostEventCallbackForRecursiveEventCall() {
    // setup
    final String[] receivedPostProcessingPayload = new String[1];

    final PostProcessor<String> eventPostProcessor = new PostProcessor<String>() {
      @Override
      public void postProcess(PmEvent event, String postProcessPayload) {
        assertNull(receivedPostProcessingPayload[0]);
        receivedPostProcessingPayload[0] = postProcessPayload;
      }
    };

    PmEventApi.addPmEventListener(myPm.s, PmEvent.TITLE_CHANGE, new PmEventListener() {
      @Override
      public void handleEvent(PmEvent event) {
        event.addPostProcessingListener(eventPostProcessor, "This is my payload.");
        assertNull("The post processing should not happen immediately.", receivedPostProcessingPayload[0]);
      }

    });

    // test

    new RecursivePmEventProcessor(myPm, PmEvent.TITLE_CHANGE).doIt();
    assertEquals("This is my payload.", receivedPostProcessingPayload[0]);
  }

  static class MyPm extends PmConversationImpl {
    public final PmAttrString s = new PmAttrStringImpl(this);

  }

  void addListenersFor(int eventMask) {
    PmEventApi.addWeakPmEventListener(myPm, eventMask, elementListener);
    PmEventApi.addWeakPmEventListener(myPm.s, eventMask, attrListener);
  }

  static class TestListener implements PmEventListener {
    public List<PmEvent> receivedEvents = new ArrayList<PmEvent>();

    @Override
    public void handleEvent(PmEvent event) {
      receivedEvents.add(event);
    }

  }
}

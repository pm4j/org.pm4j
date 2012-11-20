package org.pm4j.core.pm.api;

import static org.junit.Assert.assertEquals;
import static org.pm4j.core.pm.PmEvent.VALUE_CHANGE;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

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

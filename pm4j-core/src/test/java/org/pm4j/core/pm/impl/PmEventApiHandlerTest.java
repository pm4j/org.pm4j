package org.pm4j.core.pm.impl;

import static org.junit.Assert.assertEquals;
import static org.pm4j.core.pm.impl.PmEventApiHandler.EVENT_LISTENER_NUM_CHECK_LIMIT;

import org.junit.Test;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.api.PmEventApi;

/**
 * Tests for internal event listener handling.
 * 
 * @author Olaf Boede
 */
public class PmEventApiHandlerTest {

  /** Sleep time after GC call. Reduces probabiliy of test failures on a busy machine. */
  static final int GC_SLEEP_TIME_MS = 10;
  
  PmObjectBase pm = new PmObjectBase(new PmConversationImpl());
  
  @Test
  public void testReleaseWeakRefs() throws InterruptedException {
    PmEventApi.addWeakPmEventListener(pm, PmEvent.VALUE_CHANGE, new MyListener());
    
    assertEquals(1, pm.pmEventListenerRefs.listenerRefs.length);
    System.gc();
    Thread.sleep(GC_SLEEP_TIME_MS);
    assertEquals(1, pm.pmEventListenerRefs.listenerRefs.length);
    pm.pmEventListenerRefs.compact();
    assertEquals(0, pm.pmEventListenerRefs.listenerRefs.length);
  }
  
  @Test
  public void testAutoCompactAfterReachingMaxRefs() throws InterruptedException {
    for (int i=0; i<EVENT_LISTENER_NUM_CHECK_LIMIT; ++i) {
      PmEventApi.addWeakPmEventListener(pm, PmEvent.VALUE_CHANGE, new MyListener());
    }
    assertEquals(EVENT_LISTENER_NUM_CHECK_LIMIT, pm.pmEventListenerRefs.listenerRefs.length);

    // Automatic compact has only an effect if the GC made the weak references as invalid. 
    System.gc();
    Thread.sleep(GC_SLEEP_TIME_MS);

    PmEventApi.addWeakPmEventListener(pm, PmEvent.VALUE_CHANGE, new MyListener());

    assertEquals(1, pm.pmEventListenerRefs.listenerRefs.length);
  }

  /** dummy listener class */
  static class MyListener implements PmEventListener {
    @Override
    public void handleEvent(PmEvent event) {
    }
  }
}

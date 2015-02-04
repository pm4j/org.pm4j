package org.pm4j.tools.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;

/**
 * Records the observed events.<br>
 * Is helpful for tests that ensure stability of events.
 *
 * @author Olaf Boede
 */
public class RecordingPmEventListener implements PmEventListener {
  private List<PmEvent> receivedEvents = new ArrayList<PmEvent>();

  @Override
  public void handleEvent(PmEvent event) {
    receivedEvents.add(event);
  }

  /**
   * @return the callCount
   */
  public int getCallCount() {
    return receivedEvents.size();
  }

  /**
   * @return the receivedEvents
   */
  public List<PmEvent> getReceivedEvents() {
    return Collections.unmodifiableList(receivedEvents);
  }

  /** Forgets the received events. */
  public void clear() {
    receivedEvents.clear();
  }
}
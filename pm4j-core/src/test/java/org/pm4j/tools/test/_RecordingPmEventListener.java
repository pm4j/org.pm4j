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
public class _RecordingPmEventListener implements PmEventListener {
  private List<PmEvent> events = new ArrayList<PmEvent>();

  @Override
  public void handleEvent(PmEvent event) {
    events.add(event);
  }

  /**
   * @return the callCount
   */
  public int getEventCount() {
    return events.size();
  }

  /**
   * @return the receivedEvents
   */
  public List<PmEvent> getEvents() {
    return Collections.unmodifiableList(events);
  }

  /** Forgets the received events. */
  public void clear() {
    events.clear();
  }
}
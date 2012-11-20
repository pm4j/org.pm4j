package org.pm4j.core.event.impl;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;

public class RecordingTestListener implements PmEventListener {

  private List<PmEvent> events = new ArrayList<PmEvent>();

  public void handleEvent(PmEvent event) {
    events.add(event);
  }

  public PmEvent getLastEvent() {
    return events.get(events.size() - 1);
  }

  public List<PmEvent> getEvents() {
    return events;
  }

  public int getEventCount() {
    return events.size();
  }
  
}

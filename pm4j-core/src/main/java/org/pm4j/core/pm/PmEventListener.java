package org.pm4j.core.pm;

/**
 * Listener type definition.
 */
public interface PmEventListener {
  /**
   * Handles the event.
   *
   * @param event
   *          The event to handle.
   */
  void handleEvent(PmEvent event);
}
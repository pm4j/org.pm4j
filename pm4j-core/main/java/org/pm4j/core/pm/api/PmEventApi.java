package org.pm4j.core.pm.api;

import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmEventApiHandler;

public class PmEventApi {

  private static final PmEventApiHandler apiHandler = new PmEventApiHandler();

  /**
   * The provided <tt>listener</tt> will receive {@link PmEvent} events whenever
   * something happens that affects the given item.
   * <p>
   * This call generates only a weak reference to the listener. The calling code
   * has to ensure the intended live time of the listener.
   *
   * @param pm
   *          The PM to observe
   * @param eventMask
   *          A bit-mask that defines the kind of observed events. See the event
   *          kind constants in {@link PmEvent}.
   * @param listener
   *          The listener that wants to get informed whenever the title
   *          changes.
   */
  public static void addPmEventListener(PmObject pm, int eventMask, PmEventListener listener) {
    apiHandler.addPmEventListener(pm, eventMask, listener);
  }

  public static void addWeakPmEventListener(PmObject pm, int eventMask, PmEventListener listener) {
    apiHandler.addWeakPmEventListener(pm, eventMask, listener);
  }

  public static void addHierarchyListener(PmObject hierachyRootPm, int eventMask, PmEventListener listener) {
    apiHandler.addWeakPmEventListener(hierachyRootPm, eventMask | PmEvent.IS_EVENT_PROPAGATION, listener);
  }


  /**
   * Removes the listener reference.
   *
   * @param listener
   *          The listener to unregister.
   */
  public static void removePmEventListener(PmObject pm, PmEventListener listener) {
    apiHandler.removePmEventListener(pm, listener);
  }

  public static void removePmEventListener(PmObject pm, int eventMask, PmEventListener listener) {
    apiHandler.removePmEventListener(pm, eventMask, listener);
  }

  /**
   * Creates and sends a {@link PmEvent} instance to each registered event
   * listener.
   *
   * @param eventSource
   *          The instance that triggered the event.
   */
  public static void firePmEvent(PmObject pm, int eventMask) {
    apiHandler.firePmEvent(pm,
        new PmEvent(ensureThreadEventSource(pm),
                    pm,
                    eventMask));
  }

  public static void firePmEvent(PmObject pm, PmEvent event) {
    apiHandler.firePmEvent(pm, event);
  }

  public static void firePmEventIfInitialized(PmObject pm, int eventMask) {
    apiHandler.firePmEventIfInitialized(pm, eventMask);
  }

  public static Object setThreadEventSource(Object src) {
    apiHandler.setThreadEventSource(src);
    return src;
  }

  public static Object getThreadEventSource() {
    return apiHandler.getThreadEventSource();
  }

  public static Object ensureThreadEventSource(Object param) {
    Object currSrc = apiHandler.getThreadEventSource();
    return (currSrc == null)
        ? apiHandler.setThreadEventSource(param)
        : currSrc;
  }
}

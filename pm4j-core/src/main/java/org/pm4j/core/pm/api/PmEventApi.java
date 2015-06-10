package org.pm4j.core.pm.api;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEvent.ValueChangeKind;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.BroadcastPmEventProcessor;
import org.pm4j.core.pm.impl.PmAttrValueChangeDecorator;
import org.pm4j.core.pm.impl.PmEventApiHandler;

/**
 * Defines methods that allow to register and un-register event listeners for PM events.
 *
 * @author Olaf Boede
 */
public class PmEventApi {

  private static PmEventApiHandler apiHandler = new PmEventApiHandler();

  /**
   * Adds a decorator that gets notified before and after changing the attribute
   * value.
   * <p>
   * The value change can be prevented if method
   * {@link PmCommandDecorator#beforeDo(PmCommand)} returns <code>false</code>.
   * <p>
   * The decorator base class {@link PmAttrValueChangeDecorator} provides a
   * convenient base implementation that provides old and new value as
   * parameters for the before- and after-change methods.
   *
   * @param pmAttr
   *          The attribute to attach the decorator to.
   * @param decorator
   *          The decorator to use.
   * @return the decorator for fluent programming style usage.
   */
  public static <T extends PmCommandDecorator> T addValueChangeDecorator(PmAttr<?> pmAttr, T decorator) {
    apiHandler.addValueChangeDecorator(pmAttr, decorator);
    return decorator;
  }

  /**
   * The provided <tt>listener</tt> will receive {@link PmEvent} events whenever
   * something happens that affects the given item.
   *
   * @param pm
   *          The PM to observe
   * @param eventMask
   *          A bit-mask that defines the kind of observed events. See the event
   *          kind constants in {@link PmEvent}.
   * @param listener
   *          The listener that wants to get informed whenever the title
   *          changes.
   * @return the listener for fluent programming style,
   */
  public static <T extends PmEventListener> T addPmEventListener(PmObject pm, int eventMask, T listener) {
    apiHandler.addPmEventListener(pm, eventMask, listener);
    return listener;
  }

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
   * @return the listener for fluent programming style,
   */
  public static <T extends PmEventListener> T addWeakPmEventListener(PmObject pm, int eventMask, T listener) {
    apiHandler.addWeakPmEventListener(pm, eventMask, listener);
    return listener;
  }

  /**
   * The provided <tt>listener</tt> will receive {@link PmEvent} events whenever
   * something happens that affects the given item or one of its child PM's.
   *
   * @param pm
   *          The root of the PM tree to observe
   * @param eventMask
   *          A bit-mask that defines the kind of observed events. See the event
   *          kind constants in {@link PmEvent}.
   * @param listener
   *          The listener that wants to get informed whenever the title
   *          changes.
   * @return the listener for fluent programming style,
   */
  public static <T extends PmEventListener> T addHierarchyListener(PmObject hierachyRootPm, int eventMask, T listener) {
    apiHandler.addPmEventListener(hierachyRootPm, eventMask | PmEvent.IS_EVENT_PROPAGATION, listener);
    return listener;
  }

  /**
   * The provided <tt>listener</tt> will receive {@link PmEvent} events whenever
   * something happens that affects the given item or one of its child PM's.
   * <p>
   * This call generates only a weak reference to the listener. The calling code
   * has to ensure the intended live time of the listener.
   *
   * @param pm
   *          The root of the PM tree to observe
   * @param eventMask
   *          A bit-mask that defines the kind of observed events. See the event
   *          kind constants in {@link PmEvent}.
   * @param listener
   *          The listener that wants to get informed whenever the title
   *          changes.
   * @return the listener for fluent programming style,
   */
  public static <T extends PmEventListener> T addWeakHierarchyListener(PmObject hierachyRootPm, int eventMask, T listener) {
    apiHandler.addWeakPmEventListener(hierachyRootPm, eventMask | PmEvent.IS_EVENT_PROPAGATION, listener);
    return listener;
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

  /**
   * Creates and sends a {@link PmEvent} instance to each registered event
   * listener.
   */
  public static void firePmEvent(PmObject pm, int eventMask, ValueChangeKind valueChangeKind) {
    PmEventApiHandler.firePmEvent(pm, new PmEvent(ensureThreadEventSource(pm), pm, eventMask, valueChangeKind), true);
  }

  /**
   * Creates and sends a {@link PmEvent} instance to each registered event
   * listener.
   */
  public static void firePmEvent(PmObject pm, int eventMask) {
    PmEventApiHandler.firePmEvent(pm, new PmEvent(ensureThreadEventSource(pm), pm, eventMask, ValueChangeKind.UNKNOWN), true);
  }

  public static void firePmEventIfInitialized(PmObject pm, int eventMask, ValueChangeKind valueChangeKind) {
    apiHandler.firePmEventIfInitialized(pm, eventMask, valueChangeKind);
  }

  public static void firePmEventIfInitialized(PmObject pm, int eventMask) {
    apiHandler.firePmEventIfInitialized(pm, eventMask, ValueChangeKind.UNKNOWN);
  }

  public static void firePmEvent(PmObject pm, PmEvent event) {
    PmEventApiHandler.firePmEvent(pm, event, true);
  }

  /**
   * Broadcasts events, having the given event mask, to the PM sub-tree having the given root PM.
   *
   * @param rootPm Root of the sub tree to inform.
   * @param eventMask The event mask for the events to fire for all PM's.
   */
  public static void broadcastPmEvent(PmObject rootPm, int eventMask) {
    new BroadcastPmEventProcessor(rootPm, eventMask).doIt();
  }

  public static void firePmEventIfInitialized(PmObject pm, PmEvent event) {
    apiHandler.firePmEventIfInitialized(pm, event);
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
    return (currSrc == null) ? apiHandler.setThreadEventSource(param) : currSrc;
  }

  /**
   * Defines an application specific event api call handler.
   *
   * @param newApiHandler
   *          The alternate handler to use within the application.
   */
  public static void setApiHandler(PmEventApiHandler newApiHandler) {
    apiHandler = newApiHandler;
  }

}

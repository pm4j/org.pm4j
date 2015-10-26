package org.pm4j.core.pm.impl;

import java.util.Arrays;
import java.util.Map;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEvent.ValueChangeKind;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmEventListener.PostProcessor;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.impl.InternalPmEventListenerRefs.ListenerRef;
import org.pm4j.core.pm.impl.PmObjectBase.PmInitState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A pm4j <b>INTERNAL</b> event handling support class.
 * <p>
 * Please use {@link PmEventApi} for handling {@link PmEvent}s.
 *
 * @author olaf boede
 */
public class PmEventApiHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PmEventApiHandler.class);
  
  /** If the number of listeners reaches this number, add calls attempt to remove unused listener refs. */
  static final int EVENT_LISTENER_NUM_CHECK_LIMIT = 80;
  /** To reduce performance impact of unused listener handling, removal attempts will be started only in some interval. */
  static final int EVENT_LISTENER_NUM_CHECK_INTERVAL = 10;

  /**
   * A handler that can hold a reference to an event source within the current thread.<br>
   * May be useful for rich client applications that need to be aware about even source objects
   * to prevent event loops.
   * <p>
   * <b>Attention</b>: In case of environments with re-used threads it needs to be ensured that
   * the {@link #setThreadEventSource(Object)} gets called with a <code>null</code> parameter.
   */
  public static class WithThreadLocalEventSource extends PmEventApiHandler {

    private ThreadLocal<Object> threadEventSource = new ThreadLocal<Object>();

    public Object setThreadEventSource(Object src) {
      threadEventSource.set(src);
      return src;
    }

    public Object getThreadEventSource() {
      return threadEventSource.get();
    }
  }

  public Object setThreadEventSource(Object src) {
    return src;
  }

  public Object getThreadEventSource() {
    return null;
  }

  public void addValueChangeDecorator(PmAttr<?> pmAttr, PmCommandDecorator decorator) {
    ((PmAttrBase<?, ?>)pmAttr).addValueChangeDecorator(decorator);
  }

  public void addPmEventListener(PmObject pm, int eventMask, PmEventListener listener) {
    PmObjectBase pmImpl = (PmObjectBase)pm;
    if (pmImpl.pmEventListenerRefs == null)
      pmImpl.pmEventListenerRefs = new InternalPmEventListenerRefs();

    pmImpl.pmEventListenerRefs.addListenerRef(eventMask, listener);

    if (LOG.isTraceEnabled())
      LOG.trace("Added PM-event listener '" + listener + "' for '" + PmUtil.getPmLogString(pmImpl) + "'.");
    
    checkEventListenerSize(pmImpl);
  }

  public void addWeakPmEventListener(PmObject pm, int eventMask, PmEventListener listener) {
    PmObjectBase pmImpl = (PmObjectBase)pm;
    if (pmImpl.pmEventListenerRefs == null)
      pmImpl.pmEventListenerRefs = new InternalPmEventListenerRefs();

    pmImpl.pmEventListenerRefs.addWeakListenerRef(eventMask, listener);

    if (LOG.isTraceEnabled())
      LOG.trace("Added weak PM-event listener '" + listener + "' for '" + PmUtil.getPmLogString(pmImpl) + "'.");

    checkEventListenerSize(pmImpl);
  }
  
  private void checkEventListenerSize(PmObjectBase pm) {
    if (pm.pmEventListenerRefs != null &&
        pm.pmEventListenerRefs.listenerRefs.length >= EVENT_LISTENER_NUM_CHECK_LIMIT &&
        pm.pmEventListenerRefs.listenerRefs.length % EVENT_LISTENER_NUM_CHECK_INTERVAL == 0) {
      LOG.info(PmUtil.getPmLogString(pm) + " has a high number of PM event listeners: " + pm.pmEventListenerRefs.listenerRefs.length);
      pm.pmEventListenerRefs.compact();
    }
  }

  /**
   * Removes the listener reference.
   *
   * @param listener
   *          The listener to unregister.
   */
  public void removePmEventListener(PmObject pm, PmEventListener listener) {
    PmObjectBase pmImpl = (PmObjectBase)pm;

    if (pmImpl.pmEventListenerRefs != null) {
      if (pmImpl.pmEventListenerRefs.removeListenerRef(listener) == 0) {
        pmImpl.pmEventListenerRefs = null;
      }
    }
  }

  public static void firePmEvent(PmObject pm, PmEvent event, boolean withPreAndPostProcessing) {
    PmObjectBase pmImpl = (PmObjectBase)pm;

    // Do the operations that only should be done non-propagation events.
    if (! event.isPropagationEvent()) {
      if (withPreAndPostProcessing) {
        // call the optional event pre processing.
        sendToListeners(pm, event, true /* pre process */);
      }

      // call the simple onEvent() methods.
      dispatchToOnEventMethodCalls(pmImpl, event, event.getChangeMask());

      if (LOG.isTraceEnabled()) {
        LOG.trace("PMEvent: " + event.getChangeMask() + " fired for: " + pm.getPmRelativeName());
      }
    }

    sendToListeners(pm, event, false /* handle event */);

    if (withPreAndPostProcessing) {
      postProcessEvent(event);

      // Non-init events will be propagated to the parent hierarchy.
      // This allows to maintain the changed state of a sub-tree.
      if (! event.isAllChangedEvent()) {
        propagateEventToParents(pmImpl, event);
      }
    }
  }

  /**
   * Calls the registered {@link PmEventListener.PostProcessor}s.
   *
   * @param event the event to postprocess. It contains a set of registered post processors and related data.
   */
  public static void postProcessEvent(PmEvent event) {
    for (Map.Entry<PostProcessor<?>, Object> e : event.getPostProcessorToPayloadMap().entrySet()) {
      @SuppressWarnings("unchecked")
      PostProcessor<Object> pp = (PostProcessor<Object>) e.getKey();
      pp.postProcess(event, e.getValue());
    }
  }

  /**
   * Propagates the given <code>event</code> to the parents of the given <code>pm</code>.
   * The propagation stopps when the embedding {@link PmConversation} is reached.<br>
   * The start PM and the parent conversation will also receive the propagation call.
   *
   * @param pm the PM to start with.
   * @param event the event to propagate.
   */
  public static void propagateEventToParents(PmObject pm, PmEvent event) {
    // propagate the event to the parent hierarchy until the conversation is reached.
    PmConversation conversationPm = pm.getPmConversation();
    PmEvent propagationEvent = new PmEvent(event.getSource(), event.pm, event.getChangeMask() | PmEvent.IS_EVENT_PROPAGATION, event.getValueChangeKind());
    for (PmObject p = pm; p != null; p = p.getPmParent()) {
      sendToListeners(p, propagationEvent, false /* handle event */);
      // stop after reaching the conversation.
      if (p == conversationPm) {
        break;
      }
    }
  }

  public void firePmEventIfInitialized(PmObject pm, PmEvent event) {
    PmObjectBase pmImpl = (PmObjectBase)pm;
    if (pmImpl.pmInitState == PmInitState.INITIALIZED) {
      PmEventApi.firePmEvent(pmImpl, event);
    }
  }


  public void firePmEventIfInitialized(PmObject pm, int eventMask, ValueChangeKind valueChangeKind) {
    PmObjectBase pmImpl = (PmObjectBase)pm;
    if (pmImpl.pmInitState == PmInitState.INITIALIZED) {
      PmEventApi.firePmEvent(pmImpl, eventMask, valueChangeKind);
    }
  }

  /**
   * Calls the related on...() methods. The set of methods to call is determined
   * by the given <code>eventMask</code> parameter.
   *
   * @param event The event to propagate.
   * @param eventMask Defines the set of on-methods to be called.
   */
  protected static void dispatchToOnEventMethodCalls(PmObjectBase pmImpl, PmEvent event, int eventMask) {
    // TODO olaf: for merge to master: check & consider moving to PmMetaData

    if ((eventMask & PmEvent.VALUE_CHANGE) != 0) {
      pmImpl.onPmValueChange(event);
    }
    
    // Re-load event handling:
    // load/reload new data leads to an unchanged state.
    if (event.isAllChangedEvent() || event.isReloadEvent()) {
      // This kind event gets recursively applied to a PM tree (part). Because of that we don't need to
      // handle the child PMs.
      pmImpl._setPmValueChangedForThisInstanceOnly(pmImpl, false);
      // XXX needs to be optimized: iterates repeated over the PM tree
      pmImpl.clearCachedPmValues(CacheKind.ALL_SET);

      // Cleanup gaps in listener array whenever a completely new data scenario appears.
      if (!event.isReloadEvent() && pmImpl.pmEventListenerRefs != null) {
        pmImpl.pmEventListenerRefs.compact();
      }

      pmImpl.onPmDataExchangeEvent(event);
    }

  }

  /**
   * Sends the event to all registered listeners.
   *
   * @param pm the PM to inform the registered listeners for.
   * @param event the event to handle.
   * @param preProcess if set to <code>true</code>, only the pre process part will be done for each listener.<br>
   *                   if set to <code>false</code>, only the handle part will be done for each listener.<br>
   */
  /* package */ static void sendToListeners(PmObject pm, PmEvent event, boolean preProcess) {
    PmObjectBase pmImpl = (PmObjectBase)pm;
    if (pmImpl.pmEventListenerRefs == null) {
      return;
    }

    ListenerRef[] refs = pmImpl.pmEventListenerRefs.listenerRefs;
    if (LOG.isTraceEnabled())
      LOG.trace("fireChange[" + event + "] for event source   : " + PmEventApi.getThreadEventSource() +
                "\n\teventListeners: " + Arrays.asList(refs));

    if (refs.length > 0) {
      boolean isPropagationEvent = event.isPropagationEvent();
      // copy the listener list to prevent problems with listener
      // set changes within the notification processing loop.
      for (ListenerRef r : Arrays.copyOf(refs, refs.length)) {
        PmEventListener listener = r.getListener();
        // could be null because of WeakReferences.
        if (listener == null) {
          continue;
        }

        int listenerMask = r.eventMask;
        boolean isPropagationListener = ((listenerMask & PmEvent.IS_EVENT_PROPAGATION) != 0);
        // Propagation events have to be passed only to listeners that observe that special flag.
        // Standard events will be passed to listeners that don't have set this flag.
        boolean listenerMaskMatch = (listenerMask & event.getChangeMask()) != 0;
        if (listenerMaskMatch &&
            (isPropagationEvent == isPropagationListener)) {
          if (preProcess) {
            // XXX olaf: may cause runtime overhead because of repeated base class checks.
            // Might be optimize by adding a member to ListenerRef. But that affects the memory footprint.
            // Should be checked by performance tests.
            if (listener instanceof PmEventListener.WithPreprocessCallback) {
              ((PmEventListener.WithPreprocessCallback)listener).preProcess(event);
            }
          } else {
            listener.handleEvent(event);
          }
        }
      }
    }

  }

}

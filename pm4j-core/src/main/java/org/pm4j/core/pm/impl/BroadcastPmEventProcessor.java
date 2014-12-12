package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEvent.ValueChangeKind;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHint;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitResult;
import org.pm4j.core.pm.impl.PmObjectBase.PmInitState;

/**
 * Generates an event for each item within a PM tree.
 * <p>
 * Supports {@link PmEventListener.WithPreprocessCallback} as well as {@link PmEventListener.PostProcessor}s.
 * <p>
 * LIMITATION: Currently pre process events are not sent to dynamic child PMs. That means that they will not be sent to
 * table row PMs.<br>
 * Reason for that limitation: The event may cause (of inform about) a changed data constellation.
 * In this scenario it is not ensured that a dynamic (factory generated) PM that was found in the
 * pre-processing step will still exist in the post-processing step.
 * <p>
 * A future extension may add some different handling. To preserve behaviour compatibility, a changed behaviour
 * must be configured explicitly.
 *
 * @author Olaf Boede
 */
public class BroadcastPmEventProcessor implements Cloneable {
  // TODO: fix skip visit logic in R2.2:
  //  a) skip conversations in general.
  //  b) check if all factory generated should really be skipped. I suspect that the already generated instances
  //     should be informed.
//  private static final PmMatcher INVISIBLE_CONVERSATIONS = new PmMatcherBuilder()
//                                 .pmClass(PmConversation.class).visible(false).build();
  private static final String DEFERRED_VALUE_CHANGE_KEY = "_pm_DeferredValueChange_";


  private final int eventMask;
  private final ValueChangeKind changeKind;
  // runtime members:
  protected PmObject rootPm;
  private Map<PmObject, PmEvent> pmToEventMap;
  private List<PmEvent> eventsToPostProcess;
  private boolean immediateMode;


  /**
   * @param rootPm The root of the PM tree that should be used.
   * @param eventMask The event mask to distribute.
   */
  public BroadcastPmEventProcessor(PmObject rootPm, final int eventMask) {
    this(rootPm, eventMask, ValueChangeKind.UNKNOWN);
  }

  /**
   * @param rootPm The root of the PM tree that should be used.
   * @param eventMask The event mask to distribute.
   * @param changeKind A value change kind (will be deprecated soon.)
   */
  public BroadcastPmEventProcessor(PmObject rootPm, final int eventMask, final ValueChangeKind changeKind) {
    setRootPm(rootPm);
    this.eventMask = eventMask;
    this.changeKind = changeKind;
  }

  /**
   * Sends an individual {@link PmEvent} to each PM tree item.
   *
   * @param pm The root of the PM tree to inform.
   * @param additionalEventFlags Additional event flags to set.
   */
  public static void broadcastAllChangeEvent(PmDataInputBase pm, int additionalEventFlags) {
    // Inform all sub PMs.
    // This is not done within the initialization phase to prevent problems with initialization race conditions.
    if (pm.pmInitState == PmInitState.INITIALIZED) {
      // Inform observers of this instance and all sub-PMs.
      BroadcastPmEventProcessor p = new BroadcastPmEventProcessor(pm, PmEvent.ALL_CHANGE_EVENTS | additionalEventFlags,
            (additionalEventFlags & PmEvent.RELOAD) != 0 ? ValueChangeKind.RELOAD : ValueChangeKind.VALUE) {
        @Override
        protected void fireEvents() {
          // == Cleanup all PM state, because the bean to show is a new one. ==

          // Forget all changes and dynamic PM's below this instance.
          BeanPmCacheUtil.clearBeanPmCachesOfSubtree(rootPm);

          // All sub PM messages are no longer relevant.
          PmMessageApi.clearPmTreeMessages(rootPm);

          // Old cache values are related to the old bean.
          // This cache cleanup can only be done AFTER visiting the tree because
          // it cleans the current-row information that is relevant.
          PmCacheApi.clearPmCache(rootPm);

          // Visit the sub-tree to inform all PM listeners.
          super.fireEvents();

          // Another (postponed) cleanup:
          // Mark the whole sub tree as unchanged.
          // FIXME olaf: Move that to a PmObject related API asap.
          ((PmDataInput)rootPm).setPmValueChanged(false);
        }
      };
      p.doIt();
    }
  }

  /**
   * Sets a new root
   */
  public BroadcastPmEventProcessor setRootPm(PmObject rootPm) {
    this.rootPm = rootPm;
    pmToEventMap = new IdentityHashMap<PmObject, PmEvent>();
    eventsToPostProcess = new ArrayList<PmEvent>();
    immediateMode = false;
    return this;
  }

  public void doIt() {
    if (hasDeferredPmEventHandling(rootPm)) {
      rootPm.setPmProperty(DEFERRED_VALUE_CHANGE_KEY, this);
    } else {
      preProcess();
      fireEvents();
      postProcess();
    }
  }

  protected void preProcess() {
    // The pre process listener is currently limited to inform only the not factory
    // generated PM's (e.g. table rows).
    // TODO oboede: check wheter this restriction is still valid.
    //
    // The generated PM's can only get informed after they got generated by the main
    // event... - Thus it seems to be not possible to pre-process them at all.
    new PmVisitorImpl(new PmVisitCallBack() {
      @Override
      public PmVisitResult visit(PmObject pm) {
        if (hasDeferredPmEventHandling(pm)) {
          return PmVisitResult.SKIP_CHILDREN;
        }
        PmEventApiHandler.sendToListeners(pm, getEventForPm(pm), true /* preProcess */);
        return PmVisitResult.CONTINUE;
      }
    }, PmVisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS, PmVisitHint.SKIP_CONVERSATION)
    .visit(rootPm);
  }

  protected void fireEvents() {
    // Main visitor loop: Each child gets the information about the 'change all' event.
    new PmVisitorImpl(new PmVisitCallBack() {
      @Override
      public PmVisitResult visit(PmObject pm) {
        if (hasDeferredPmEventHandling(pm)) {
          pm.setPmProperty(DEFERRED_VALUE_CHANGE_KEY, BroadcastPmEventProcessor.this.clone().setRootPm(pm));
          return PmVisitResult.SKIP_CHILDREN;
        }
        // If there is an existing event instance from the pre-process loop, it will
        // be used again.
        // This way it is possible to accumulate the post processing requests.
        PmEvent e = getEventForPm(pm);
        PmEventApiHandler.firePmEvent(pm, e, false /* handle */);

        // If there is a registered post processor, remember this event for post
        // processing.
        // Example: A table wants to restore its selection after a reload of its backing bean.
        if (!e.getPostProcessorToPayloadMap().isEmpty()) {
          eventsToPostProcess.add(e);
        }
        return PmVisitResult.CONTINUE;
      }
    }, PmVisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS, PmVisitHint.SKIP_CONVERSATION)
    .visit(rootPm);
  }

  protected void postProcess() {
    // Perform the event postprocessing for all listeners that requested it.
    for (PmEvent e : eventsToPostProcess) {
      PmEventApiHandler.postProcessEvent(e);
    }

    PmEventApiHandler.propagateEventToParents(rootPm, getEventForPm(rootPm));
  }

  /**
   * Provides the event instance used for the given PM.
   * <p>
   * If there is an existing event instance from a previous event processing step, it will
   * be used again.
   * <p>
   * This way it is possible to accumulate PM related data within the event instance.
   *
   * @param pm
   * @return
   */
  protected final PmEvent getEventForPm(PmObject pm) {
    PmEvent event = pmToEventMap.get(pm);
    if (event == null) {
        event = new PmEvent(pm, eventMask, changeKind);
        pmToEventMap.put(pm, event);
    }
    return event;
  }

  private boolean hasDeferredPmEventHandling(PmObject pm) {
    return !immediateMode &&
           (eventMask & PmEvent.VALUE_CHANGE_TO_NULL) == 0 &&
           // TODO oboede: the defer concept should be supported for all PM types.
           ( (pm instanceof PmBeanImpl2) &&
               ((PmBeanImpl2<?>)pm).hasDeferredPmEventHandling() );
  }

  /**
   * Looks for a deferred value change event processor attached to the given PM.<br>
   * If there is one, its reference gets removed from the PM and after that it gets
   * executed.
   *
   * @param pm The PM to find the processor for.
   */
  public static final void doDeferredPmEventExecution(PmObject pm) {
    BroadcastPmEventProcessor p = (BroadcastPmEventProcessor) pm.getPmProperty(DEFERRED_VALUE_CHANGE_KEY);
    if (p != null) {
      pm.setPmProperty(DEFERRED_VALUE_CHANGE_KEY, null);
      p.immediateMode = true;
      p.doIt();
    }
  }

  // TODO: Move to PmEventApi
  /**
   * Ensures that getPmBean was called at least once for each visible {@link PmBean}. In case
   * of deferred event handling, this ensures that each deferred event got fired.
   *
   * @param rootPm
   */
  public static final void doDeferredEventsForVisiblePms(PmObject rootPm) {
      PmVisitorImpl v = new PmVisitorImpl(new PmVisitCallBack() {
          @Override
          public PmVisitResult visit(PmObject pm) {
              doDeferredPmEventExecution(pm);
              return PmVisitResult.CONTINUE;
          }
      }, PmVisitHint.SKIP_INVISIBLE);
      v.visit(rootPm);
  }

  @Override
  protected BroadcastPmEventProcessor clone() {
    try {
      BroadcastPmEventProcessor clone = (BroadcastPmEventProcessor) super.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

}
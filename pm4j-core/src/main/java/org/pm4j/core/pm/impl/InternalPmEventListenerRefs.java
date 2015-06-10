package org.pm4j.core.pm.impl;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.api.PmEventApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for {@link PmEvent} listeners.
 *
 * May handle hard and weak references to {@link PmEventListener}s.
 *
 * @author Olaf Boede
 */
class InternalPmEventListenerRefs implements Cloneable {

  private static final Logger LOG = LoggerFactory.getLogger(InternalPmEventListenerRefs.class);

  /** Placeholder for removed reference. Used to prevent too much repeated array resize operations. */
  private static ListenerRef NO_LISTENER_REF = new ListenerRef(0) {
    @Override PmEventListener getListener() { return null; }
  };

  /** Re-used default array. Used for minimizing memory footprint. */
  private static ListenerRef[] NO_LISTENER_REFS = {};

  /** The set of registered listener references. */
  private ListenerRef[] listenerRefs = NO_LISTENER_REFS;

  /**
   * Adds a hard listener reference.
   *
   * @param eventMask Defines the set of events to handle.
   * @param listener The listener to add.
   */
  public void addListenerRef(int eventMask, PmEventListener listener) {
    addListener(new HardListenerRef(listener, eventMask));
  }

  /**
   * Adds a weak listener reference.
   *
   * @param eventMask Defines the set of events to handle.
   * @param listener The listener to add.
   */
  public void addWeakListenerRef(int eventMask, PmEventListener listener) {
    addListener(new WeakListenerRef(listener, eventMask));
  }

  /**
   * Removes all references to the given listener.
   *
   * @param listener The listener to remove.
   * @return The number of remaining active listeners.
   */
  public int removeListenerRef(PmEventListener listener) {
    int remainingListenerCount = 0;
    for (int i=0; i<listenerRefs.length; ++i) {
      if (listenerRefs[i].getListener() == listener) {
        listenerRefs[i] = NO_LISTENER_REF;
      } else
        if (listenerRefs[i].getListener() != null) {
        ++remainingListenerCount;
      }
    }
    return remainingListenerCount;
  }

  /**
   * @param event the event to handle.
   * @param preProcess if set to <code>true</code>, only the pre process part will be done for each listener.<br>
   *                   if set to <code>false</code>, only the handle part will be done for each listener.<br>
   */
  /* package */ void fireEvent(final PmEvent event, boolean preProcess) {
    if (LOG.isTraceEnabled())
      LOG.trace("fireChange[" + event + "] for event source   : " + PmEventApi.getThreadEventSource() +
                "\n\teventListeners: " + Arrays.asList(listenerRefs));

    if (listenerRefs.length > 0) {
      boolean isPropagationEvent = event.isPropagationEvent();
      // copy the listener list to prevent problems with listener
      // set changes within the notification processing loop.
      for (ListenerRef r : Arrays.copyOf(listenerRefs, listenerRefs.length)) {
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

  @Override
  protected InternalPmEventListenerRefs clone() throws CloneNotSupportedException {
    return (InternalPmEventListenerRefs) super.clone();
  }

  /**
   * Releases all gaps in the listener reference set.
   *
   * Gaps may appear in case of weak references to garbage collected listeners.
   * A call to {@link #removeListenerRef(PmEventListener)} does also not compact the array immediately.
   */
  public void compact() {
    compactAndAllocate(0);
  }

  @Override
  public String toString() {
    return listenerRefs.toString();
  }

  /** Adds the listener at the last position. Compacts the array as well. */
  private void addListener(ListenerRef listenerRef) {
    if (listenerRefs.length == 0) {
      listenerRefs = new ListenerRef[] { listenerRef };
      return;
    }

    if (listenerRefs[listenerRefs.length-1].getListener() == null) {
      // Reuse the last place, if it got just empty.
      // Optimization for repeating add-weak--garbage-collect scenarios.
      listenerRefs[listenerRefs.length-1] = listenerRef;
    } else {
      compactAndAllocate(1);
    }

    listenerRefs[listenerRefs.length-1] = listenerRef;
  }

  /** Should only be called if there is at least one listener reference. */
  private void compactAndAllocate(int incSizeCount) {
    int unusedCount = 0;
    for (ListenerRef r : listenerRefs) {
      if (r.getListener() == null) {
        unusedCount++;
      }
    }

    if (unusedCount == 0) {
      if (incSizeCount > 0) {
        listenerRefs = Arrays.copyOf(listenerRefs, listenerRefs.length+incSizeCount);
      }
    } else {
      // There are unused references. Reorganize the array:
      int newSize = listenerRefs.length - unusedCount + incSizeCount;
      if (newSize > 0) {
        ListenerRef[] refs = new ListenerRef[newSize];
        int pos = 0;
        for (ListenerRef r : listenerRefs) {
          if (r.getListener() != null) {
            refs[pos] = r;
            pos++;
          }
        }
        listenerRefs = refs;
      } else {
        listenerRefs = NO_LISTENER_REFS;
      }
    }
  }

  /** Abstract listener reference. Concrete references may be hard or weak. */
  static abstract class ListenerRef {
    final int eventMask;

    public ListenerRef(int eventMask) {
      this.eventMask = eventMask;
    }
    abstract PmEventListener getListener();
  }

  /** A hard reference to a listener. Keeps it alive. */
  static class HardListenerRef extends ListenerRef {
    final PmEventListener listener;

    public HardListenerRef(PmEventListener listener, int eventMask) {
      super(eventMask);
      this.listener = listener;
    }

    @Override
    PmEventListener getListener() {
      return listener;
    }

    @Override
    public String toString() {
      return "HardRef: " + listener;
    }
  }

  /** A weak reference to a listener. The listener life time is controlled externally. */
  static class WeakListenerRef extends ListenerRef {
    final WeakReference<PmEventListener> ref;

    public WeakListenerRef(PmEventListener listener, int eventMask) {
      super(eventMask);
      this.ref = new WeakReference<PmEventListener>(listener);
    }

    @Override
    PmEventListener getListener() {
      return ref.get();
    }

    @Override
    public String toString() {
      return "WeakRef: " + ref.get();
    }
  }

}

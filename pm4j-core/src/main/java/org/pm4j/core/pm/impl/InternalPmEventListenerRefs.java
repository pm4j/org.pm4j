package org.pm4j.core.pm.impl;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;

/**
 * Container for {@link PmEvent} listeners.
 *
 * May handle hard and weak references to {@link PmEventListener}s.
 *
 * @author Olaf Boede
 */
class InternalPmEventListenerRefs implements Cloneable {

  /** Placeholder for removed reference. Used to prevent too much repeated array resize operations. */
  private static final ListenerRef NO_LISTENER_REF = new ListenerRef(0) {
    @Override PmEventListener getListener() { return null; }
  };

  /** Re-used default array. Used for minimizing memory footprint. */
  private static final ListenerRef[] NO_LISTENER_REFS = {};

  /** The set of registered listener references. */
  /* package */ ListenerRef[] listenerRefs = NO_LISTENER_REFS;

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
   * <p>
   * It just replaces the array position with an empty reference.
   * That is done to prevent slow array resize operations on dynamic
   * remove-and-add listener scenarios.
   * <p>
   * The array gaps will be cleaned up on the next add or compact call.
   *
   * @param listener The listener to remove.
   * @return The number of remaining active listeners.
   */
  public synchronized int removeListenerRef(PmEventListener listener) {
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
    if (countUnused(this.listenerRefs) > 0) {
      synchronized(this) {
        listenerRefs = compactAndAllocate(listenerRefs, null);
      }
    }
  }

  @Override
  public String toString() {
    return listenerRefs.toString();
  }

  /** Adds the listener at the last position. Compacts the array as well. */
  private synchronized void addListener(ListenerRef listenerRef) {
    if (listenerRefs.length == 0) {
      listenerRefs = new ListenerRef[] { listenerRef };
      return;
    }

    int firstFreePos = firstFreePos();
    if (firstFreePos < listenerRefs.length) {
      // Reuse the last place, if it got just empty.
      // Optimization for repeating add-weak--garbage-collect scenarios.
      listenerRefs[firstFreePos] = listenerRef;
    } else {
      listenerRefs = compactAndAllocate(listenerRefs, listenerRef);
    }
  }

  private int firstFreePos() {
    int pos = listenerRefs.length-1;
    while (listenerRefs[pos].getListener() == null) {
      --pos;
    }
    return pos + 1;
  }

  private static ListenerRef[] compactAndAllocate(ListenerRef[] refs, ListenerRef newRef) {
    ListenerRef[] newRefs = NO_LISTENER_REFS;
    int unusedCount = countUnused(refs);
    int incSizeCount = newRef != null ? 1 : 0;
    if (unusedCount == 0) {
      if (incSizeCount > 0) {
        newRefs = Arrays.copyOf(refs, refs.length+incSizeCount);
      }
    } else {
      // There are unused references. Reorganize the array:
      int newSize = refs.length - unusedCount + incSizeCount;
      if (newSize > 0) {
        newRefs = new ListenerRef[newSize];
        int pos = 0;
        for (ListenerRef r : refs) {
          if (r.getListener() != null) {
            newRefs[pos] = r;
            pos++;
          }
        }
        // If the GC was active in the middle of the loop the compact algorithm will be
        // restarted using the original array. The newRefs array can't be used because it
        // has unsupported null items.
        if (pos < newSize - incSizeCount) {
          return compactAndAllocate(refs, newRef);
        }
      }
    }
    if (newRef != null) {
      newRefs[newRefs.length-1] = newRef;
    }
    return newRefs;
  }

  private static int countUnused(ListenerRef[] refs) {
    int unusedCount = 0;
    for (ListenerRef r : refs) {
      if (r.getListener() == null) {
        unusedCount++;
      }
    }
    return unusedCount;
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

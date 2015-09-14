package org.pm4j.core.pb;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmEventApiHandler;

/**
 * The {@link PmEventCallGate} is used for (rich client) views to call PM methods that may
 * cause call-back event, sent back to the view.
 * <p>
 * The gate stores a reference to the event source (usually a view component).
 * This reference is used by view bindings to check if a PM event was triggered
 * by the bound view. In this case the view should not be updated by the PM event.<br>
 * This helps to prevent endless event loops.
 * <p>
 * After finishing the call gate secured operation, the thread local reference to the event source
 * gets removed. This prevents memory leaks.
 *
 * @author olaf boede
 */
// TODO olaf: move to package pb?
public abstract class PmEventCallGate {

  private static PmEventApiHandler apiHandler = new PmEventApiHandler();
  
  /**
   * Creates a PM event call gate for the given event source (usually a view component).
   * <p>
   * Calls immediately the {@link #exec()} method.
   * <p>
   * Resets the event source afterwards.
   *
   * @param eventSource The event source for the call to process.
   */
  public PmEventCallGate(Object eventSource) {
    Object oldEvenSrc = getThreadEventSource();
    try {
      if (eventSource != null) {
        setThreadEventSource(eventSource);
      }
      // Execute the specific task to do.
      exec();
    }
    finally {
      setThreadEventSource(oldEvenSrc);
    }
  }

  /**
   * Concrete instances implement here the call to execute.
   */
  protected abstract void exec();


  public static void setValueAsString(Object eventSource, final PmAttr<?> pmAttr, final String value) {
    new PmEventCallGate(eventSource) {
      @Override
      protected void exec() {
        pmAttr.setValueAsString(value);
      }
    };

    Object oldEvenSrc = getThreadEventSource();
    try {
      if (eventSource != null) {
        setThreadEventSource(eventSource);
      }
      pmAttr.setValueAsString(value);
    }
    finally {
      setThreadEventSource(oldEvenSrc);
    }
  }

  public static <T> void setValue(Object eventSource, PmAttr<T> pmAttr, T value) {
    Object oldEvenSrc = getThreadEventSource();
    try {
      if (eventSource != null) {
        setThreadEventSource(eventSource);
      }
      pmAttr.setValue(value);
    }
    finally {
      setThreadEventSource(oldEvenSrc);
    }
  }

  public static PmCommand doIt(Object eventSource, PmCommand pmCommand) {
    PmCommand executedCommand = null;
    Object oldEvenSrc = getThreadEventSource();
    try {
      if (eventSource != null) {
        setThreadEventSource(eventSource);
      }
      executedCommand = pmCommand.doIt();
    }
    finally {
      setThreadEventSource(oldEvenSrc);
    }

    return executedCommand;
  }

  private static Object setThreadEventSource(Object src) {
    apiHandler.setThreadEventSource(src);
    return src;
  }

  private static Object getThreadEventSource() {
    return apiHandler.getThreadEventSource();
  }

}

package org.pm4j.core.pm.api;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;

public abstract class PmEventCallGate {

  private final Object oldEvenSrc = PmEventApi.getThreadEventSource();

  public PmEventCallGate(Object eventSource) {
    try {
      if (eventSource != null) {
        PmEventApi.setThreadEventSource(eventSource);
      }
      // XXX olaf: Check if a call to an overridden method works.
      exec();
    }
    finally {
      PmEventApi.setThreadEventSource(oldEvenSrc);
    }
  }

  protected abstract void exec();


  public static void setValueAsString(Object eventSource, final PmAttr<?> pmAttr, final String value) {
    new PmEventCallGate(eventSource) {
      @Override
      protected void exec() {
        pmAttr.setValueAsString(value);
      }
    };

    Object oldEvenSrc = PmEventApi.getThreadEventSource();
    try {
      if (eventSource != null) {
        PmEventApi.setThreadEventSource(eventSource);
      }
      pmAttr.setValueAsString(value);
    }
    finally {
      PmEventApi.setThreadEventSource(oldEvenSrc);
    }
  }

  public static <T> void setValue(Object eventSource, PmAttr<T> pmAttr, T value) {
    Object oldEvenSrc = PmEventApi.getThreadEventSource();
    try {
      if (eventSource != null) {
        PmEventApi.setThreadEventSource(eventSource);
      }
      pmAttr.setValue(value);
    }
    finally {
      PmEventApi.setThreadEventSource(oldEvenSrc);
    }
  }

  public static PmCommand doIt(Object eventSource, PmCommand pmCommand) {
    PmCommand executedCommand = null;
    Object oldEvenSrc = PmEventApi.getThreadEventSource();
    try {
      if (eventSource != null) {
        PmEventApi.setThreadEventSource(eventSource);
      }
      executedCommand = pmCommand.doIt();
    }
    finally {
      PmEventApi.setThreadEventSource(oldEvenSrc);
    }

    return executedCommand;
  }

}

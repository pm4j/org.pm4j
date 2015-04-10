package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;

/**
 * A set of command decorators that may be used as a single decorator.<br>
 * It delegates its calls internally to all decorator set items.
 *
 * @author olaf boede
 */
public class PmCommandDecoratorSetImpl implements PmCommandDecorator.WithExceptionHandling, Cloneable {

  /** The set of decorators. Copy-on-write is done on every {@link #addDecorator(PmCommandDecorator)} call. */
  private List<PmCommandDecorator> decorators = Collections.emptyList();

  @Override
  protected PmCommandDecoratorSetImpl clone() {
    try {
      return (PmCommandDecoratorSetImpl) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

  public void addDecorator(PmCommandDecorator d) {
    List<PmCommandDecorator> newDecorators = new ArrayList<PmCommandDecorator>(decorators.size()+1);
    newDecorators.addAll(decorators);
    newDecorators.add(d);
    decorators = Collections.unmodifiableList(newDecorators);
  }

  static boolean execBeforeDo(PmCommand cmd, Collection<PmCommandDecorator> decorators) {
    for (PmCommandDecorator d : decorators) {
      if (!d.beforeDo(cmd)) {
        return false;
      }
    }
    return true;
  }

  static boolean execAfterDo(PmCommand cmd, Collection<PmCommandDecorator> decorators) {
    for (PmCommandDecorator d : decorators) {
      d.afterDo(cmd);
    }
    return true;
  }

  @Override
  public boolean beforeDo(PmCommand cmd) {
    for (PmCommandDecorator d : decorators) {
      if (!d.beforeDo(cmd)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void afterDo(PmCommand cmd) {
    for (PmCommandDecorator d : decorators) {
      d.afterDo(cmd);
    }
  }

  @Override
  public boolean onException(PmCommand cmd, Exception exception) {
    boolean proceedWithStandardExceptionHandling = true;

    for (PmCommandDecorator d : decorators) {
      if (d instanceof PmCommandDecorator.WithExceptionHandling) {
        if (! ((WithExceptionHandling)d).onException(cmd, exception)) {
          proceedWithStandardExceptionHandling = false;
        }
      }
    }

    return proceedWithStandardExceptionHandling;
  }


  public PmCommandDecorator beforeDoReturnVetoDecorator(PmCommand cmd) {
    if (decorators != null) {
      for (PmCommandDecorator d : decorators) {
        if (!d.beforeDo(cmd)) {
          return d;
        }
      }
    }
    return null;
  }

  public Collection<PmCommandDecorator> getDecorators() {
    return decorators;
  }

}

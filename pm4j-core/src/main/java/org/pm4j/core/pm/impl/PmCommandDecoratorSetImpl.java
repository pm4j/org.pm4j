package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;

/**
 * A set of command decorators that may be used as a single decorator.<br>
 * It delegates its calls internally to all decorator set items.
 *
 * @author olaf boede
 */
class PmCommandDecoratorSetImpl implements PmCommandDecorator.WithExceptionHandling {

  private Collection<PmCommandDecorator> decorators = Collections.emptyList();

  public void addDecorator(PmCommandDecorator d) {
    Collection<PmCommandDecorator> newDecorators = new ArrayList<PmCommandDecorator>(decorators.size()+1);
    newDecorators.addAll(decorators);
    newDecorators.add(d);
    decorators = Collections.unmodifiableCollection(newDecorators);
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

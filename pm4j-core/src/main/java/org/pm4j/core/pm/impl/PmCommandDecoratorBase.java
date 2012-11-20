package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;

/**
 * A type safe basic {@link PmCommandDecorator} implementation, providing empty
 * implementations for all interface methods.
 * <p>
 * It may be used as a convenient base class for decorators that need to
 * implement only some methods of the {@link PmCommandDecorator} interface.
 *
 * @author olaf boede
 */
public abstract class PmCommandDecoratorBase<T extends PmCommand> implements PmCommandDecorator {

  @SuppressWarnings("unchecked")
  @Override
  public final boolean beforeDo(PmCommand cmd) {
    return beforeDoImpl((T)cmd);
  }

  protected abstract boolean beforeDoImpl(T cmd);

  @SuppressWarnings("unchecked")
  @Override
  public final void afterDo(PmCommand cmd) {
    afterDoImpl((T)cmd);
  }

  protected abstract void afterDoImpl(T cmd);

}

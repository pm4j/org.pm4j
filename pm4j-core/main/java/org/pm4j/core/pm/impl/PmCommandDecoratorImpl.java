package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;

/**
 * A basic {@link PmCommandDecorator} implementation, providing empty
 * implementations for all interface methods.
 * <p>
 * It may be used as a convenient base class for decorators that need to
 * implement only some methods of the {@link PmCommandDecorator} interface.
 *
 * @author olaf boede
 */
public class PmCommandDecoratorImpl implements PmCommandDecorator {

  @Override
  public boolean beforeDo(PmCommand cmd) {
    return true;
  }

  @Override
  public void afterDo(PmCommand cmd) {
  }

}

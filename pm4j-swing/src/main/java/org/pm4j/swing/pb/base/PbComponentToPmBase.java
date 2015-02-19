package org.pm4j.swing.pb.base;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pb.PbFactoryBase;
import org.pm4j.core.pb.PbViewStylerBase;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;

public abstract class PbComponentToPmBase<VIEW extends Component, PM extends PmObject>
    extends PbFactoryBase<VIEW, Container, PM> {
  
  public PbComponentToPmBase() {
    // TODO olaf: not yet configurable
    addViewStyler(new ViewStylerRedOnError()); 
  }

  @Override
  public void bind(Object componentAsObject, PmObject pm) {
    if (!(componentAsObject instanceof Component)) {
      throw new PmRuntimeException(pm, String.format(
          "Component expected. Got %s.", componentAsObject.getClass()));
    }
    super.bind(componentAsObject, pm);
  }

  // -- subclass helper --

  /**
   * Subclasses may provide here a listener for PM-Events.<br>
   * This listener will be added and removed by the binding base class to the
   * PM.
   * 
   * @param pm
   *          The PM to bind.
   * @return The PM-event listener. May be <code>null</code> if there is nothing
   *         to observe.
   */
  @Override 
  protected PbBinding makeBinding(PM pm) {
    return new Binding();
  }

  /**
   * Base class for listeners that may react on Swing- and PM-events to
   * communicate changes between both sides.
   * 
   * @param <W>
   *          The component type.
   * @param <P>
   *          The PM type.
   */
  public class Binding extends PbFactoryBase<VIEW, Container, PM>.Binding {
    @Override
    protected void onPmEnablementChange(PmEvent event) {
      view.setEnabled(pm.isPmEnabled());
    }
  }

  public class ViewStylerRedOnError extends PbViewStylerBase<VIEW, PM> {
    @Override
    protected void applyStyleImpl(VIEW view, PM pm) {
      view.setForeground(pm.isPmValid() 
          ? Color.BLACK 
          : Color.RED);
    }
  }

}

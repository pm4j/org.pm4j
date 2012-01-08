package org.pm4j.swing.pb.base;

import java.awt.event.FocusListener;

import javax.swing.JComponent;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.swing.pb.PbSwingDefaults;

/**
 * Convenience base class for bindings between SWT {@link JComponent}s and
 * {@link PmAttr}s.
 * 
 * @author olaf boede
 * 
 * @param <VIEW>
 *          Type of the {@link JComponent} to bind.
 * @param <PM>
 *          Type of the {@link PmAttr} to bind.
 */
public abstract class PbJComponentToAttrBase<VIEW extends JComponent, PM extends PmAttr<?>>
    extends PbComponentToPmBase<VIEW, PM> {

  public ValueUpdateEvent valueUpdateEvent = PbSwingDefaults.getInstance().getValueUpdateEvent();

  public PbJComponentToAttrBase() {
    super();
  }

  protected FocusListener makeFocusListener(VIEW component, PM pm) {
    return null;
  }

  @Override
  protected PbBinding makeBinding(PM pm) {
    return new Binding();
  };

  public class Binding extends PbComponentToPmBase<VIEW, PM>.Binding {
    @Override
    protected void onPmTooltipChange(PmEvent event) {
      view.setToolTipText(pm.getPmTooltip());
    }

    @Override
    public void bind() {
      super.bind();
      FocusListener focusListener = makeFocusListener(view, pm);
      if (focusListener != null)
        view.addFocusListener(focusListener);
    }
  }
}

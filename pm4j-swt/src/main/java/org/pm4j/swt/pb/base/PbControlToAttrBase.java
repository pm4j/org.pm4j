package org.pm4j.swt.pb.base;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.pm4j.core.pb.PbViewStylerBase;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.swt.pb.PbSwtDefaults;
import org.pm4j.swt.util.SWTResourceManager;

/**
 * Convenience base class for bindings between SWT {@link Control}s and {@link PmAttr}s.
 * 
 * @author olaf boede
 *
 * @param <VIEW> Type of the {@link Control} to bind.
 * @param <PM>      Type of the {@link PmAttr} to bind.
 */
public abstract class PbControlToAttrBase <VIEW extends Control, PM extends PmAttr<?>>
    extends PbControlToPmBase<VIEW, PM> {

  public ValueUpdateEvent valueUpdateEvent = PbSwtDefaults.getInstance().getValueUpdateEvent();

  public PbControlToAttrBase(int style) {
    super(style);
    // TODO olaf: not yet configurable
    addViewStyler(new ViewStylerRedOnError()); 
  }

  public class Binding extends PbControlToPmBase<VIEW, PM>.Binding
                       implements FocusListener {

    @Override
    public void bind() {
      super.bind();
      view.addFocusListener(this);
    }

    @Override
    public void unbind() {
      super.unbind();
      view.removeFocusListener(this);
    }
    
    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
    }
  }

  public class ViewStylerRedOnError extends PbViewStylerBase<VIEW, PM> {
    @Override
    protected void applyStyleImpl(VIEW view, PM pm) {
      view.setForeground(pm.isPmValid()
                            ? null
                            : SWTResourceManager.getColor(SWT.COLOR_RED));
    }
  }
  
}

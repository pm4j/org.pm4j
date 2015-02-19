package org.pm4j.swt.pb;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.pm4j.core.pm.PmAttrNumber;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.api.PmEventCallGate;
import org.pm4j.swt.pb.base.PbControlToAttrBase;

public class PbSpinner extends PbControlToAttrBase<Spinner, PmAttrNumber<?>> {

	public PbSpinner() {
		this(SWT.SINGLE | SWT.BORDER);
	}

  public PbSpinner(int style) {
    super(style);
  }

	@Override
	public Spinner makeView(Composite shell, PmAttrNumber<?> pm) {
		return new Spinner(shell, swtStyle);
	}

  @Override
  protected PbBinding makeBinding(PmAttrNumber<?> pm) {
    return new Binding();
  }

  /**
   * Handles PM events as well as the SWT selection event.
   */
  public class Binding extends PbControlToAttrBase<Spinner, PmAttrNumber<?>>.Binding
                       implements ModifyListener {

    @Override
    public void bind() {
      super.bind();

      view.setMinimum(pm.getMinLen());
      view.setMaximum(pm.getMaxLen());

      view.addModifyListener(this);
    }

    @Override
    public void unbind() {
      super.unbind();
      view.removeModifyListener(this);
    }

    @Override
    protected void onPmValueChange(PmEvent event) {
      Number n = pm.getValue();
      view.setSelection(n != null
                ? n.intValue()
                : Math.max(view.getMinimum(), 0));
    }

    @Override
    public void focusLost(FocusEvent e) {
      if (valueUpdateEvent == ValueUpdateEvent.FOCUS_LOST) {
        PmEventCallGate.setValueAsString(view, pm, view.getText());
      }
    }

    @Override
    public void modifyText(ModifyEvent e) {
      if (valueUpdateEvent == ValueUpdateEvent.MODIFY) {
        PmEventCallGate.setValueAsString(view, pm, view.getText());
      }
    }
  }

}

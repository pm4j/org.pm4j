package org.pm4j.swt.pb;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.swt.pb.base.PbControlToAttrBase;

public class PbCheckBox extends PbControlToAttrBase<Button, PmAttrBoolean>{

	public PbCheckBox() {
		super(SWT.CHECK);
	}

	@Override
	public Button makeView(Composite shell, PmAttrBoolean pm) {
		return new Button(shell, swtStyle);
	}

	@Override
	protected PbBinding makeBinding(PmAttrBoolean pm) {
    return new Binding();
  }
  
  public class Binding extends PbControlToAttrBase<Button, PmAttrBoolean>.Binding 
                       implements SelectionListener {

    @Override
    public void bind() {
      super.bind();
      view.addSelectionListener(this);
    }
    
    @Override
    public void unbind() {
      super.unbind();
      view.removeSelectionListener(this);
    }

    @Override
    protected void onPmValueChange(PmEvent event) {
      view.setSelection(Boolean.TRUE.equals(pm.getValue()));
    }
    
    @Override
    public void widgetSelected(SelectionEvent e) {
      pm.setValue(view.getSelection());
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      widgetSelected(e);
    }

  }

}

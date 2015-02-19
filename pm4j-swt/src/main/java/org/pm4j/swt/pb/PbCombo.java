package org.pm4j.swt.pb;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmOption;
import org.pm4j.swt.pb.base.PbControlToAttrBase;

public class PbCombo extends PbControlToAttrBase<Combo, PmAttr<?>> {

	public PbCombo() {
		super(SWT.READ_ONLY);
	}

	@Override
	public Combo makeView(Composite parent, PmAttr<?> pm) {
		return new Combo(parent, swtStyle);
	}

	@Override
	protected PbBinding makeBinding(PmAttr<?> pm) {
    return new Binding();
  }

  public class Binding extends PbControlToAttrBase<Combo, PmAttr<?>>.Binding
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
    protected void onPmOptionSetChange(PmEvent event) {
      view.removeAll();
      for (PmOption o : pm.getOptionSet().getOptions()) {
        view.add(o.getPmTitle());
      }
    }
    
    @Override
    protected void onPmValueChange(PmEvent event) {
      PmOption o = pm.getOptionSet().findOptionForIdString(pm.getValueAsString());
      view.setText(o != null 
          ? o.getPmTitle() 
          : "");
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      PmOption o = pm.getOptionSet().findOptionForTitle(view.getText());
      pm.setValueAsString(o != null
          ? o.getIdAsString()
          : null);
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      widgetSelected(e);
    }
  }
  
}

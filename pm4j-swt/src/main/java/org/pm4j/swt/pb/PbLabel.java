package org.pm4j.swt.pb;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.pm4j.core.pb.PbViewStylerBase;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;
import org.pm4j.swt.pb.base.PbCompositeChildToPmBase;
import org.pm4j.swt.util.SWTResourceManager;

public class PbLabel extends PbCompositeChildToPmBase<Label, PmObject> {

	public PbLabel() {
		super(SWT.LEFT);
		addViewStyler(new ViewStyler());
	}

	@Override
	public Label makeView(Composite shell, PmObject pm) {
		return new Label(shell, swtStyle);
	}

  @Override
  protected PbBinding makeBinding(PmObject pm) {
    return new Binding();
  }

  public class Binding extends PbCompositeChildToPmBase<Label, PmObject>.Binding {
 
    @Override
    protected void onPmTitleChange(PmEvent event) {
      view.setText(pm.getPmTitle());
    }
    
    @Override
    protected void onPmTooltipChange(PmEvent event) {
      view.setToolTipText(pm.getPmTooltip());
    }
  }
  
  public class ViewStyler extends PbViewStylerBase<Label, PmObject> {
    @Override
    protected void applyStyleImpl(Label view, PmObject pm) {
      boolean isValid = pm.isPmValid();
      view.setForeground(isValid
                            ? null
                            : SWTResourceManager.getColor(SWT.COLOR_RED));
    }
  }
  
}

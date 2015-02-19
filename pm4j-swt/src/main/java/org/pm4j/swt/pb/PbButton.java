package org.pm4j.swt.pb;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.swt.pb.base.PbControlToPmBase;
import org.pm4j.swt.pb.listener.CommandExecListener;

public class PbButton extends PbControlToPmBase<Button, PmCommand> {

  public PbButton() {
    super(SWT.PUSH);
  }

  @Override
  public Button makeView(Composite parent, PmCommand pm) {
    return new Button(parent, swtStyle);
  }

  @Override
  protected PbBinding makeBinding(PmCommand pm) {
    return new Binding();
  }

  /**
   * Handles PM events as well as the SWT selection event.
   */
  public class Binding extends PbControlToPmBase<Button, PmCommand>.Binding {

    @Override
    public void bind() {
      super.bind();
      view.addSelectionListener(CommandExecListener.INSTANCE); 
    }
    
    @Override
    public void unbind() {
      super.unbind();
      view.removeSelectionListener(CommandExecListener.INSTANCE);
    }

    @Override
    protected void onPmTitleChange(PmEvent event) {
      view.setText(pm.getPmTitle());
    }
  }

}

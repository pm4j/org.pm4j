package org.pm4j.swt.pb.standards;

import org.eclipse.jface.dialogs.InputDialog;
import org.pm4j.standards.PmInputDialog;
import org.pm4j.swt.pb.PbWindowBase;
import org.pm4j.swt.util.SwtUtil;

public class PbInputDialog extends PbWindowBase<InputDialog, PmInputDialog> {

  @Override
  public InputDialog makeView(Object parent, PmInputDialog pm) {
    InputDialog dlg = new InputDialog(
        SwtUtil.getShellFromSWTObj(parent),
        pm.getPmTitle(),
        pm.name.getPmTitle(),
        pm.name.getValue(),
        null);
    return dlg;
  }
  
  @Override
  protected PbBinding makeBinding(PmInputDialog pm) {
    return new Binding();
  }
  
  class Binding extends PbWindowBase<InputDialog, PmInputDialog>.Binding {
    @Override
    public void bind() {
      super.bind();
      view.open();
    }
  }

}

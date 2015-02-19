package org.pm4j.swt.pb.standards;

import org.eclipse.jface.dialogs.MessageDialog;
import org.pm4j.standards.PmConfirmDialog;
import org.pm4j.swt.pb.PbImageRegistry;
import org.pm4j.swt.pb.PbWindowBase;
import org.pm4j.swt.util.SwtUtil;

public class PbConfirmDialog extends PbWindowBase<MessageDialog, PmConfirmDialog>{

  @Override
  public MessageDialog makeView(Object parent, PmConfirmDialog pm) {
    return new MessageDialog(
        SwtUtil.getShellFromSWTObj(parent),
        pm.getPmTitle(),
        PbImageRegistry.findImage(pm.dialogMessage),
        pm.dialogMessage.getPmTitle(),
        MessageDialog.QUESTION,
        new String[] { pm.cmdYes.getPmTitle(), pm.cmdNo.getPmTitle() },
        1);
  }

  @Override
  protected PbBinding makeBinding(PmConfirmDialog pm) {
    return new Binding();
  }

  class Binding extends PbWindowBase<MessageDialog, PmConfirmDialog>.Binding {
    @Override
    public void bind() {
      super.bind();
      int selected = view.open();
      if (selected == 0) {
        pm.cmdYes.doIt();
      }
      else {
        pm.cmdNo.doIt();
      }
    }
  }

}

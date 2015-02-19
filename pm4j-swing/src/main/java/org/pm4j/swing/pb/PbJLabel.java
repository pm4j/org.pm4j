package org.pm4j.swing.pb;

import java.awt.Container;

import javax.swing.JLabel;

import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;
import org.pm4j.swing.pb.base.PbComponentToPmBase;

public class PbJLabel extends PbComponentToPmBase<JLabel, PmObject> {

  public PbJLabel() {
  }

  @Override
  public JLabel makeView(Container parent, PmObject pm) {
    JLabel label = new JLabel();
    parent.add(label);
    return label;
  }

  @Override
  protected PbBinding makeBinding(PmObject pm) {
    return new Binding();
  }

  public class Binding extends PbComponentToPmBase<JLabel, PmObject>.Binding {
    @Override
    protected void onPmTitleChange(PmEvent event) {
      view.setText(pm.getPmTitle());
    }
    
    @Override
    protected void onPmTooltipChange(PmEvent event) {
      view.setToolTipText(pm.getPmTooltip());
    }
  }

}

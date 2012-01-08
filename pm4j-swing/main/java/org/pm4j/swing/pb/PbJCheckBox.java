package org.pm4j.swing.pb;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.swing.pb.base.PbJComponentToAttrBase;

public class PbJCheckBox extends
    PbJComponentToAttrBase<JCheckBox, PmAttrBoolean> {

  public PbJCheckBox() {
  }

  @Override
  public JCheckBox makeView(Container parent, PmAttrBoolean pm) {
    JCheckBox checkBox = new JCheckBox();
    parent.add(checkBox);
    return checkBox;
  }

  @Override
  protected PbBinding makeBinding(PmAttrBoolean pm) {
    return new Binding();
  }

  public class Binding extends
      PbJComponentToAttrBase<JCheckBox, PmAttrBoolean>.Binding implements
      ActionListener {

    @Override
    public void bind() {
      super.bind();
      view.getModel().addActionListener(this);
    }

    @Override
    public void unbind() {
      super.unbind();
      view.getModel().removeActionListener(this);
    }

    @Override
    protected void onPmValueChange(PmEvent event) {
      view.setSelected(Boolean.TRUE.equals(pm.getValue()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      PmEventApi.setThreadEventSource(view);
      pm.setValue(view.isSelected());
    }
  }

}

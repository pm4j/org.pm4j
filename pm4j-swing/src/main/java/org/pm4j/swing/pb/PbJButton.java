package org.pm4j.swing.pb;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.swing.pb.base.PbComponentToPmBase;
import org.pm4j.swing.pb.internal.util.CommandExecListener;

public class PbJButton extends PbComponentToPmBase<JButton, PmCommand> {

  public PbJButton() {
  }

  @Override
  public JButton makeView(Container parent, PmCommand pm) {
    JButton button = new JButton();
    parent.add(button);
    return button;
  }

  @Override
  protected PbBinding makeBinding(PmCommand pm) {
    return new Binding();
  }

  /**
   * Handles PM events as well as the SWT selection event.
   */
  public class Binding extends PbComponentToPmBase<JButton, PmCommand>.Binding implements ActionListener{

    @Override
    public void bind() {
      super.bind();
      view.addActionListener(CommandExecListener.INSTANCE); 
    }
    
    @Override
    public void unbind() {
      super.unbind();
      view.removeActionListener(this); 
    }

    @Override
    protected void onPmTitleChange(PmEvent event) {
      view.setText(pm.getPmTitle());
    }
    
    @Override
    protected void onPmTooltipChange(PmEvent event) {
      view.setToolTipText(pm.getPmTooltip());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      pm.doIt();
    }
  }
}

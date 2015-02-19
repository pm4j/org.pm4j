package org.pm4j.swing.pb.base;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.swing.pb.PbSwingDefaults;
import org.pm4j.swing.pb.PbSwingWidgetFactorySet;

public abstract class PbFormBase<W extends JComponent, PM extends PmObject> extends PbComponentToPmBase<W, PM> {

  /** The configurable set of Swing binders to use. */
  public PbSwingWidgetFactorySet binderSet = PbSwingDefaults.getInstance().getWidgetFactorySet();
  
  protected void bindCommand(JButton button, PmCommand pm) {
    binderSet.pbButton.bind(button, pm);
  }

  protected void bindAttr(JLabel label, JTextField text, PmAttr<?> pmAttr) {
    bindLabel(label, pmAttr);
    binderSet.pbText.bind(text, pmAttr);
  }
  
  protected void bindAttr(JLabel label, JCheckBox button, PmAttrBoolean pmAttr) {
    bindLabel(label, pmAttr);
    binderSet.pbCheckBox.bind(button, pmAttr);
  }

  protected void bindAttr(JLabel label, JComboBox comboBox, PmAttr<?> pmAttr) {
    bindLabel(label, pmAttr);
    binderSet.pbCombo.bind(comboBox, pmAttr);
  }
  
  protected void bindAttr(JLabel label, JList list, PmAttr<?> pmAttr) {
    bindLabel(label, pmAttr);
    binderSet.pbListForOptions.bind(list, pmAttr);
  }
  
  protected void bindLabel(JLabel label, PmObject pm) {
    if (label != null)
      binderSet.pbLabel.bind(label, pm);
  }
}

package org.pm4j.swt.pb.base;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.swt.pb.PbSwtDefaults;
import org.pm4j.swt.pb.PbSwtWidgetFactorySet;

/**
 * Provides functionalities to support binding operations usually used for UI-forms.
 *  
 * @author olaf boede
 *
 * @param <W>  the form widget type to bind.
 * @param <PM> the PM type to present within the form.
 */
public abstract class PbFormBase<W extends Widget, PM extends PmObject> 
          extends PbCompositeChildToPmBase<W, PM> {
  
  /** The configurable set of SWT binders to use. */
  public PbSwtWidgetFactorySet binderSet = PbSwtDefaults.getInstance().getWidgetFactorySet();

  protected void bindCommand(Button button, PmCommand pm) {
    binderSet.pbButton.bind(button, pm);
  }

  protected void bindAttr(Label label, Text text, PmAttr<?> pmAttr) {
    bindLabel(label, pmAttr);
    binderSet.pbText.bind(text, pmAttr);
  }
  
  protected void bindAttr(Label label, Button button, PmAttrBoolean pmAttr) {
    bindLabel(label, pmAttr);
    binderSet.pbCheckBox.bind(button, pmAttr);
  }

  protected void bindAttr(Label label, Combo combo, PmAttr<?> pmAttr) {
    bindLabel(label, pmAttr);
    binderSet.pbCombo.bind(combo, pmAttr);
  }

  protected void bindAttr(Label label, List list, PmAttr<?> pmAttr) {
    bindLabel(label, pmAttr);
    binderSet.pbListForOptions.bind(list, pmAttr);
  }

  protected void bindLabel(Label label, PmObject pm) {
    if (label != null)
      binderSet.pbLabel.bind(label, pm);
  }
}

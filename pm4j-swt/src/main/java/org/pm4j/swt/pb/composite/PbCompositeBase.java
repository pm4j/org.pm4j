package org.pm4j.swt.pb.composite;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.pm4j.core.pb.PbMatcher;
import org.pm4j.core.pm.PmObject;
import org.pm4j.swt.pb.PbSwtDefaults;
import org.pm4j.swt.pb.PbSwtWidgetFactorySet;
import org.pm4j.swt.pb.base.PbCompositeChildToPmBase;

public abstract class PbCompositeBase<C extends Composite, PM extends PmObject>
  extends PbCompositeChildToPmBase<C, PM> {

  /**
   * The set of standard widget bindings used by the <code>bind/build<i>WigetType</i>()</code>
   * methods of this instance.
   */
  private PbSwtWidgetFactorySet widgetFactorySet;

  /**
   * The binding factory matcher used by the <code>bind/buildWiget()</code>
   * methods of this instance.
   */
  private PbMatcher pbMatcher;

  @SuppressWarnings("unchecked")
  @Override
  public void bind(Object view, PmObject pm) {
    super.bind(view, pm);
    ((C)view).pack();
  }

  public Widget buildWidget(Composite parent, PmObject pm) {
    return (Widget)getPbMatcher().getPbFactory(pm).build(parent, pm);
  }

  public void bindWidget(Widget widget, PmObject pm) {
    getPbMatcher().getPbFactory(pm).bind(widget, pm);
  }

  public void bindWidget(Widget widget, Label label, PmObject pm) {
    bindLabel(label, pm);
    getPbMatcher().getPbFactory(pm).bind(widget, pm);
  }

  public void bindLabel(Label label, PmObject pm) {
    getWidgetFactorySet().pbLabel.bind(label, pm);
  }

  public Label buildLabel(Composite parent, PmObject pm) {
    return (Label)getWidgetFactorySet().pbLabel.build(parent, pm);
  }

  public Text buildText(Composite parent, PmObject pm) {
    return (Text)getWidgetFactorySet().pbText.build(parent, pm);
  }

  public void bindText(Text text, PmObject pm) {
    getWidgetFactorySet().pbText.bind(text, pm);
  }

  public void bindButton(Button button, PmObject pm) {
    getWidgetFactorySet().pbButton.bind(button, pm);
  }

  public Button buildButton(Composite shell, PmObject pm) {
    return getWidgetFactorySet().pbButton.build(shell, pm);
  }

  public PbMatcher getPbMatcher() {
    return pbMatcher == null
            ? PbSwtDefaults.getInstance().getMatcher() 
            : pbMatcher;
  }
  
  public void setPbMatcher(PbMatcher pbMatcher) {
    this.pbMatcher = pbMatcher;
  }

  public PbSwtWidgetFactorySet getWidgetFactorySet() {
    return widgetFactorySet == null
            ? PbSwtDefaults.getInstance().getWidgetFactorySet()
            : widgetFactorySet;
  }

  public void setWidgetFactorySet(PbSwtWidgetFactorySet widgetBinderSet) {
    this.widgetFactorySet = widgetBinderSet;
  }

}

package org.pm4j.demo.basic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.pm4j.swt.pb.composite.PbCompositeBase;

public class MyDemoBinder extends PbCompositeBase<MyDemoView, BasicDemoElementPm> {

  @Override
  public MyDemoView makeView(Composite parentComponent, BasicDemoElementPm pm) {
    return new MyDemoView(parentComponent, SWT.NONE);
  }

  @Override
  protected PbBinding makeBinding(BasicDemoElementPm pm) {
    return new Binding() {
  
      @Override
      public void bind() {
        super.bind();
        bindWidget(view.getMyName(), view.getNameLabel(), pm.textFieldShort);
        bindWidget(view.getTextArea(), view.getTextAreaLabel(), pm.textFieldLong);
        bindButton(view.getButton1(), pm.cmdSave);
      }
      
    };
  }
  
}
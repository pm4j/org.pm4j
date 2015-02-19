package org.pm4j.demo.basic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.pm4j.swt.pb.composite.PbGridLayout;
import org.pm4j.swt.testtools.DemoData;
import org.pm4j.swt.testtools.SwtTestShell;


public class MyUiSwtGridLayoutDemo {

	public static void main(String[] args) {
	  SwtTestShell s = new SwtTestShell(250, 350, "SwtGridLayoutBuilder Demo");
	  Composite c = new Composite(s.getShell(), SWT.NONE);
	  c.setLayout(new FillLayout(SWT.VERTICAL));

    new PbGridLayout.AllAttrs().build(c, DemoData.makeDemoPm());

    s.show();
	}
}


//new GridFormBinder().bind(s, DemoData.makeDemoPm());
class GridFormBinder extends PbGridLayout<BasicDemoElementPm> {

  @Override
  protected PbBinding makeBinding(BasicDemoElementPm pm) {
    return new Binding() {

      @Override
      public void bind() {
        super.bind();
        buildRows(view, pm.booleanField, pm.intField);

        buildLabel(view, pm.textFieldLong);
        buildText(view, pm.textFieldLong);

        buildRows(view, pm.color, pm.doubleField);

        buildButton(view, pm.cmdCancel);
        buildButton(view, pm.cmdSave);
      }
      
    };
  }

}


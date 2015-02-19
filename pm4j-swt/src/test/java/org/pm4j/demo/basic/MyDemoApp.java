package org.pm4j.demo.basic;

import org.pm4j.swt.testtools.DemoData;
import org.pm4j.swt.testtools.SwtTestShell;


public class MyDemoApp {

  public static void main(String[] args) {
    SwtTestShell s = new SwtTestShell(500, 350, "Custom View Binding Demo");

    new MyDemoBinder().build(s.getShell(), DemoData.makeDemoPm());

    s.show();
  }

}

package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.pm.impl.DeprPmTabSetImpl;

public class PmTabSetTest {

  private MyTabSet myTabSet;
  private MyCommandDecorator myCommandDecorator;

  @Before
  public void setUp() {
    PmConversation converation = new PmConversationImpl();
    myTabSet = new MyTabSet(converation);
    myCommandDecorator = new MyCommandDecorator();
  }

  @Test
  public void testTabSwitchCommandDecoratorForSwitchTab1ToTab2() {
    myTabSet.addTabSwitchCommandDecorator(myTabSet.tab1, myTabSet.tab2, myCommandDecorator);

    myTabSet.switchToTabPm(myTabSet.tab1, myTabSet.tab2);
    assertEquals(true, myCommandDecorator.beforeDoWasCalled);
    assertEquals(true, myCommandDecorator.afterDoWasCalled);

    myCommandDecorator.clear();

    myTabSet.switchToTabPm(myTabSet.tab2, myTabSet.tab1);
    assertEquals(false, myCommandDecorator.beforeDoWasCalled);
    assertEquals(false, myCommandDecorator.afterDoWasCalled);
  }

  @Test
  public void testTabSwitchCommandDecoratorForSwitchToTab2() {
    myTabSet.addTabSwitchCommandDecorator(null, myTabSet.tab2, myCommandDecorator);

    myTabSet.switchToTabPm(myTabSet.tab1, myTabSet.tab2);
    assertEquals(true, myCommandDecorator.beforeDoWasCalled);
    assertEquals(true, myCommandDecorator.afterDoWasCalled);

    myCommandDecorator.clear();
    myTabSet.switchToTabPm(myTabSet.tab2, myTabSet.tab1);
    assertEquals(false, myCommandDecorator.beforeDoWasCalled);
    assertEquals(false, myCommandDecorator.afterDoWasCalled);

    myCommandDecorator.clear();
    myTabSet.switchToTabPm(null, myTabSet.tab3);
    assertEquals(false, myCommandDecorator.beforeDoWasCalled);
    assertEquals(false, myCommandDecorator.afterDoWasCalled);

    myCommandDecorator.clear();
    myTabSet.switchToTabPm(myTabSet.tab3, myTabSet.tab2);
    assertEquals(true, myCommandDecorator.beforeDoWasCalled);
    assertEquals(true, myCommandDecorator.afterDoWasCalled);
  }


  static class MyCommandDecorator implements PmCommandDecorator {

    private boolean beforeDoWasCalled;
    private boolean afterDoWasCalled;

    @Override
    public boolean beforeDo(PmCommand cmd) {
      beforeDoWasCalled = true;
      return true;
    }

    @Override
    public void afterDo(PmCommand cmd) {
      afterDoWasCalled = true;
    }

    public void clear() {
      beforeDoWasCalled = false;
      afterDoWasCalled = false;
    }
  }

  public static class MyTabSet extends DeprPmTabSetImpl {
    public final PmElement tab1 = new PmElementImpl(this);
    public final PmElement tab2 = new PmElementImpl(this);
    public final PmElement tab3 = new PmElementImpl(this);

    public MyTabSet(PmObject pmParent) {
      super(pmParent);
    }
  }

}

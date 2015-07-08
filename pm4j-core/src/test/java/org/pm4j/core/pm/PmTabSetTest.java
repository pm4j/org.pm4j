package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pm4j.tools.test._PmAssert.assertNoMessagesInConversation;

import org.junit.Test;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.impl.PmTabSetImpl;

/**
 * Tests for {@link PmTabSetImpl}.
 *
 * @author Olaf Boede
 */
public class PmTabSetTest {

  private MyTabSet myTabSet = new MyTabSet(new PmConversationImpl());
  private MyCommandDecorator myCommandDecorator = new MyCommandDecorator();

  @Test
  public void testTabSwitchCommandDecoratorForSwitchTab1ToTab2() {
    myTabSet.addTabSwitchCommandDecorator(myTabSet.tab1, myTabSet.tab2, myCommandDecorator);

    myTabSet.switchToTabPm(myTabSet.tab2);
    assertEquals(true, myCommandDecorator.beforeDoWasCalled);
    assertEquals(true, myCommandDecorator.afterDoWasCalled);

    myCommandDecorator.clear();

    myTabSet.switchToTabPm(myTabSet.tab1);
    assertEquals(false, myCommandDecorator.beforeDoWasCalled);
    assertEquals(false, myCommandDecorator.afterDoWasCalled);
  }

  @Test
  public void testTabSwitchCommandDecoratorForSwitchToTab2() {
    myTabSet.addTabSwitchCommandDecorator(null, myTabSet.tab2, myCommandDecorator);

    myTabSet.switchToTabPm(myTabSet.tab2);
    assertEquals(true, myCommandDecorator.beforeDoWasCalled);
    assertEquals(true, myCommandDecorator.afterDoWasCalled);

    myCommandDecorator.clear();
    myTabSet.switchToTabPm(myTabSet.tab1);
    assertEquals(false, myCommandDecorator.beforeDoWasCalled);
    assertEquals(false, myCommandDecorator.afterDoWasCalled);

    myCommandDecorator.clear();
    myTabSet.switchToTabPm(myTabSet.tab3);
    assertEquals(false, myCommandDecorator.beforeDoWasCalled);
    assertEquals(false, myCommandDecorator.afterDoWasCalled);

    myCommandDecorator.clear();
    myTabSet.switchToTabPm(myTabSet.tab2);
    assertEquals(true, myCommandDecorator.beforeDoWasCalled);
    assertEquals(true, myCommandDecorator.afterDoWasCalled);
  }

  @Test
  public void testAllTabsDisabled() {
    myTabSet.tab1.tabEnabled = false;
    myTabSet.tab2.tabEnabled = false;
    myTabSet.tab3.tabEnabled = false;

    assertEquals("If there is no active tab, the first (inactive) tab will be the current tab.",
                 myTabSet.tab1, myTabSet.getCurrentTabPm());

    assertFalse("Switch to inactive tab shouldn't be possible.",
                myTabSet.switchToTabPm(myTabSet.tab2));
    assertNoMessagesInConversation(myTabSet);
  }

  @Test
  public void testNavigateToFirstTab_DisableAllTabs_FireAllChangedEvent() {
    assertEquals(myTabSet.tab1, myTabSet.getCurrentTabPm());

    assertTrue(myTabSet.switchToTabPm(myTabSet.tab2));

    myTabSet.tab1.tabEnabled=false;
    myTabSet.tab2.tabEnabled=false;
    myTabSet.tab3.tabEnabled=false;

    PmEventApi.broadcastPmEvent(myTabSet, PmEvent.ALL_CHANGE_EVENTS);

    assertEquals(myTabSet.tab1, myTabSet.getCurrentTabPm());
    assertNoMessagesInConversation(myTabSet);
  }

  @Test
  public void testFirstTabDisabled() {
    myTabSet.tab1.tabEnabled = false;

    assertEquals("If the first tab is disabled, the second one will be the initial current tab.",
                 myTabSet.tab2, myTabSet.getCurrentTabPm());

    assertFalse("Switch to inactive tab shouldn't be possible.",
                myTabSet.switchToTabPm(myTabSet.tab1));
    assertTrue("Switch to active tab should be possible.",
        myTabSet.switchToTabPm(myTabSet.tab3));
    assertNoMessagesInConversation(myTabSet);
  }

  @Test
  public void testLeaveDisabledTabOnResetCurrentTabCall() {
    assertEquals(myTabSet.tab1, myTabSet.getCurrentTabPm());
    myTabSet.tab1.tabEnabled = false;

    myTabSet.resetCurrentTabPmIfInactive();
    assertEquals(myTabSet.tab2, myTabSet.getCurrentTabPm());
    assertNoMessagesInConversation(myTabSet);
  }

  @Test
  public void testLeaveDisabledTabOnAllChangedEvent() {
    assertEquals(myTabSet.tab1, myTabSet.getCurrentTabPm());
    myTabSet.tab1.tabEnabled = false;

    PmEventApi.broadcastPmEvent(myTabSet, PmEvent.ALL_CHANGE_EVENTS);

    assertEquals(myTabSet.tab2, myTabSet.getCurrentTabPm());
    assertNoMessagesInConversation(myTabSet);
  }

  @Test
  public void testGetTabIndex() {
    assertEquals(0, myTabSet.getTabIndex(myTabSet.tab1));
    assertEquals(1, myTabSet.getTabIndex(myTabSet.tab2));
    assertEquals(2, myTabSet.getTabIndex(myTabSet.tab3));
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

  public static class MyTabSet extends PmTabSetImpl {
    public final MyTab tab1 = new MyTab(this);
    public final MyTab tab2 = new MyTab(this);
    public final MyTab tab3 = new MyTab(this);

    public MyTabSet(PmObject pmParent) {
      super(pmParent);
    }
  }

  /** Each instance that wants to be handled as a tab needs to implement
   * the interface {@link PmTab}. */
  public static class MyTab extends PmObjectBase implements PmTab {
    boolean tabEnabled = true;

    public MyTab(PmObject pmParent) {
      super(pmParent);
    }

    @Override
    protected boolean isPmEnabledImpl() {
      return tabEnabled;
    }
  }

}

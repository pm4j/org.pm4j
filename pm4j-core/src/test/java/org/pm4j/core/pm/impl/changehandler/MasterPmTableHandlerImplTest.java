package org.pm4j.core.pm.impl.changehandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.pm4j.tools.test._PmAssert.assertChanged;
import static org.pm4j.tools.test._PmAssert.assertNotChanged;
import static org.pm4j.tools.test._PmAssert.setValue;
import static org.pm4j.tools.test._PmTableAssert.assertColStrings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmInitApi;
import org.pm4j.core.pm.impl.changehandler.MasterPmTableHandlerImplTestDlg.Bean;


/**
 * Tests for {@link MasterPmTableHandlerImpl},
 *
 * @author Olaf Boede
 */
public class MasterPmTableHandlerImplTest {

  private MasterPmTableHandlerImplTestDlg dlg = PmInitApi.initPmTree(new MasterPmTableHandlerImplTestDlg());

  @Before
  public void setUp() {
    Assert.assertNotNull(dlg.masterTable.getMasterRowPmBean());
    assertEquals("a", dlg.masterTable.getMasterRowPmBean().toString());

    // An additional (now superflous) call.
    // Stays in the test to check if additional events will appear.
    selectMasterRow(0);

    assertEquals("a", dlg.masterTable.getMasterRowPmBean().toString());
    assertEquals("a", dlg.detailsArea.getPmBean().toString());

    assertEquals("[]", dlg.detailsAreaHandler.detailsCalls.before.toString());
    assertEquals("[[null, a]]", dlg.detailsAreaHandler.detailsCalls.after.toString());
    assertEquals("onPmDataExchange=1 onPmValueChange=1", dlg.detailsArea.eventCalls.toString());
    assertEquals("afterMsChange=1", dlg.detailsAreaHandler.detailsCalls.toString());
    assertEquals("afterMsChange=2", dlg.detailsTableHandler.detailsCalls.toString());

    assertColStrings("a, b", dlg.masterTable.name);
    assertColStrings("a.1", dlg.detailsTable.name);
    assertColStrings("a.1.1", dlg.detailsDetailsTable.name);

    assertEquals(1, dlg.masterTable.eventCalls.selectionPropertyListener.getNumOfPropertyChangesCalls());
    assertEquals(1, dlg.detailsTable.eventCalls.selectionPropertyListener.getNumOfPropertyChangesCalls());
    assertEquals(0, dlg.detailsDetailsTable.eventCalls.selectionPropertyListener.getNumOfPropertyChangesCalls());
    assertEquals("afterMsChange=2", dlg.detailsDetailsTableHandler.detailsCalls.toString());

    dlg.cleanCallCounter();
  }

  @Test
  public void selectSecondAndFirstRowAgain() {
    selectMasterRow(1);

    assertColStrings("b.1", dlg.detailsTable.name);
    assertColStrings("b.1.1", dlg.detailsDetailsTable.name);
    assertEquals("b", dlg.masterTable.getMasterRowPmBean().toString());
    assertEquals("b", dlg.detailsArea.getPmBean().toString());
    assertEquals("[[a, b]]", dlg.detailsAreaHandler.detailsCalls.before.toString());
    assertEquals("onPmDataExchange=1 onPmValueChange=1", dlg.detailsArea.eventCalls.toString());
    assertEquals("", dlg.detailsTable.eventCalls.toString());
    assertEquals("beforeMsChange=1 afterMsChange=1", dlg.detailsAreaHandler.detailsCalls.toString());
    assertEquals("beforeMsChange=1 afterMsChange=1", dlg.detailsTableHandler.detailsCalls.toString());

    selectMasterRow(0);

    assertColStrings("a.1", dlg.detailsTable.name);
    assertColStrings("a.1.1", dlg.detailsDetailsTable.name);
    assertEquals("a", dlg.masterTable.getMasterRowPmBean().toString());
    assertEquals("a", dlg.detailsArea.getPmBean().toString());
    assertColStrings("a.1", dlg.detailsTable.name);

    assertEquals("[[a, b], [b, a]]", dlg.detailsAreaHandler.detailsCalls.before.toString());
    assertEquals("onPmDataExchange=2 onPmValueChange=2", dlg.detailsArea.eventCalls.toString());
    assertEquals("", dlg.detailsTable.eventCalls.toString());
    assertEquals("beforeMsChange=2 afterMsChange=2", dlg.detailsAreaHandler.detailsCalls.toString());
    assertEquals("beforeMsChange=2 afterMsChange=2", dlg.detailsTableHandler.detailsCalls.toString());
  }

  @Test
  public void testAddMasterRowAddDetailsDeleteMasterRow() {
    Bean newMasterBean = new Bean("M");
    dlg.masterTable.getPmPageableBeanCollection().getModificationHandler().addItem(newMasterBean);

    assertColStrings("a, b, M", dlg.masterTable.name);
    assertColStrings("a.1", dlg.detailsTable.name);
    assertColStrings("a.1.1", dlg.detailsDetailsTable.name);
    assertEquals("{added: 1, updated: 0, removed: 0}", dlg.masterTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{}", dlg.detailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertEquals("", dlg.detailsTable.eventCalls.toString());

    dlg.cleanCallCounter();
    dlg.masterTable.getPmPageableBeanCollection().getSelectionHandler().select(true, newMasterBean);
    dlg.detailsTable.getPmPageableBeanCollection().getModificationHandler().addItem(new Bean("M.1"));
    selectDetailsRow(0);
    dlg.detailsDetailsTable.getPmPageableBeanCollection().getModificationHandler().addItem(new Bean("M.1.1"));

    assertColStrings("a, b, M", dlg.masterTable.name);
    assertColStrings("M.1", dlg.detailsTable.name);
    assertColStrings("M.1.1", dlg.detailsDetailsTable.name);
    assertEquals("[[a, M]]", dlg.detailsTableHandler.detailsCalls.before.toString());
    assertEquals("onPmValueChange=1", dlg.detailsTable.eventCalls.toString());
    assertEquals("beforeMsChange=1 afterMsChange=1", dlg.detailsAreaHandler.detailsCalls.toString());
    assertEquals("beforeMsChange=1 afterMsChange=1", dlg.detailsTableHandler.detailsCalls.toString());

    dlg.cleanCallCounter();
    dlg.detailsTable.getPmPageableBeanCollection().getModificationHandler().addItem(new Bean("D"));

    assertColStrings("a, b, M", dlg.masterTable.name);
    assertColStrings("M.1, D", dlg.detailsTable.name);
    assertColStrings("M.1.1", dlg.detailsDetailsTable.name);
    assertEquals("{added: 1, updated: 0, removed: 0}", dlg.masterTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{M={added: 2, updated: 0, removed: 0}}", dlg.detailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertEquals("onPmValueChange=1", dlg.detailsTable.eventCalls.toString());
    assertEquals("", dlg.detailsAreaHandler.detailsCalls.toString());
    assertEquals("", dlg.detailsTableHandler.detailsCalls.toString());
    assertEquals("ItemSetSelection[M]", dlg.masterTable.getPmPageableBeanCollection().getSelection().toString());

    dlg.cleanCallCounter();
    dlg.masterTable.getPmPageableBeanCollection().getModificationHandler().removeSelectedItems();

    assertColStrings("a, b", dlg.masterTable.name);
    assertColStrings("a.1", dlg.detailsTable.name);
    assertColStrings("a.1.1", dlg.detailsDetailsTable.name);
    assertEquals("{}", dlg.masterTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{}", dlg.detailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    // TODO 154638: should be [[null, a]]. It's too much noise, but finally somehow ok.
    assertEquals("[[null, null], [null, a]]", dlg.detailsAreaHandler.detailsCalls.after.toString());
    assertEquals("[[null, a]]", dlg.detailsTableHandler.detailsCalls.after.toString());
    assertEquals("onPmValueChange=1", dlg.detailsTable.eventCalls.toString());
    assertEquals(1, dlg.detailsTable.eventCalls.valueChangeListener.getEventCount());
    assertEquals(1L, dlg.detailsTable.getTotalNumOfPmRows());

    dlg.cleanCallCounter();
    selectMasterRow(0);
    dlg.masterTable.getPmPageableBeanCollection().getModificationHandler().removeSelectedItems();

    assertColStrings("b", dlg.masterTable.name);
    assertColStrings("b.1", dlg.detailsTable.name);
    assertColStrings("b.1.1", dlg.detailsDetailsTable.name);

    dlg.cleanCallCounter();
    selectMasterRow(0);
    dlg.masterTable.getPmPageableBeanCollection().getModificationHandler().removeSelectedItems();

    assertColStrings("", dlg.masterTable.name);
    assertColStrings("", dlg.detailsTable.name);
    assertColStrings("", dlg.detailsDetailsTable.name);
    assertEquals(0, dlg.masterTable.getTotalNumOfPmRows());
    assertEquals("{}", dlg.detailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertEquals("{}", dlg.detailsDetailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
  }

  @Test
  public void testDeleteAndDependendChanges() {
    assertColStrings("a, b", dlg.masterTable.name);
    assertColStrings("a.1", dlg.detailsTable.name);
    assertColStrings("a.1.1", dlg.detailsDetailsTable.name);

    setValue(dlg.detailsDetailsTable.getRowPms().get(0).name, "a.1.1'");

    assertEquals("{added: 0, updated: 1, removed: 0}", dlg.masterTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{added: 0, updated: 1, removed: 0}", dlg.detailsTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{added: 0, updated: 1, removed: 0}", dlg.detailsDetailsTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{a={added: 0, updated: 1, removed: 0}}", dlg.detailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertEquals("{a.1={added: 0, updated: 1, removed: 0}}", dlg.detailsDetailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertChanged(dlg.detailsDetailsTable, dlg.detailsTable, dlg.masterTable);

    selectMasterRow(1);

    assertEquals("{added: 0, updated: 1, removed: 0}", dlg.masterTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{}", dlg.detailsTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{}", dlg.detailsDetailsTable.getPmPageableBeanCollection().getModifications().toString());
    assertColStrings("b.1", dlg.detailsTable.name);
    assertColStrings("b.1.1", dlg.detailsDetailsTable.name);
    assertChanged(dlg.masterTable);
    assertNotChanged(dlg.detailsDetailsTable, dlg.detailsTable);

    setValue(dlg.detailsDetailsTable.getRowPms().get(0).name, "b.1.1'");

    assertEquals("{added: 0, updated: 2, removed: 0}", dlg.masterTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{added: 0, updated: 1, removed: 0}", dlg.detailsTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{added: 0, updated: 1, removed: 0}", dlg.detailsDetailsTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{a={added: 0, updated: 1, removed: 0}, b={added: 0, updated: 1, removed: 0}}", dlg.detailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertEquals("{a.1={added: 0, updated: 1, removed: 0}, b.1={added: 0, updated: 1, removed: 0}}", dlg.detailsDetailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertChanged(dlg.detailsDetailsTable, dlg.detailsTable, dlg.masterTable);

    assertTrue(dlg.masterTable.getPmPageableBeanCollection().getModificationHandler().removeSelectedItems());

    assertColStrings("a", dlg.masterTable.name);
    assertColStrings("a.1", dlg.detailsTable.name);
    assertColStrings("a.1.1'", dlg.detailsDetailsTable.name);
    assertEquals("{added: 0, updated: 1, removed: 1}", dlg.masterTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{added: 0, updated: 1, removed: 0}", dlg.detailsTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{added: 0, updated: 1, removed: 0}", dlg.detailsDetailsTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{a={added: 0, updated: 1, removed: 0}}", dlg.detailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertEquals("{a.1={added: 0, updated: 1, removed: 0}}", dlg.detailsDetailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertChanged(dlg.detailsDetailsTable, dlg.detailsTable, dlg.masterTable);

    assertTrue(dlg.masterTable.getPmPageableBeanCollection().getModificationHandler().removeSelectedItems());

    assertColStrings("", dlg.masterTable.name);
    assertColStrings("", dlg.detailsTable.name);
    assertColStrings("", dlg.detailsDetailsTable.name);

    assertEquals("{added: 0, updated: 0, removed: 2}", dlg.masterTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{}", dlg.detailsTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{}", dlg.detailsDetailsTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{}", dlg.detailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertEquals("{}", dlg.detailsDetailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertChanged(dlg.masterTable);
    assertNotChanged(dlg.detailsDetailsTable, dlg.detailsTable);
  }


  @Test
  public void testDeleteAllMasterRows() {
    dlg.cleanCallCounter();
    selectMasterRow(1);

    assertColStrings("a, b", dlg.masterTable.name);
    assertColStrings("b.1", dlg.detailsTable.name);
    assertColStrings("b.1.1", dlg.detailsDetailsTable.name);

    setValue(dlg.detailsDetailsTable.getRowPms().get(0).name, "b.1.1'");

    assertColStrings("b.1.1'", dlg.detailsDetailsTable.name);
    assertEquals("{b={added: 0, updated: 1, removed: 0}}", dlg.detailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertEquals("{b.1={added: 0, updated: 1, removed: 0}}", dlg.detailsDetailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertEquals(1L, dlg.detailsTable.getPmPageableBeanCollection().getSelection().getSize());
    assertEquals(1, dlg.masterTable.eventCalls.selectionPropertyListener.getNumOfPropertyChangesCalls());
    assertEquals(2, dlg.detailsTable.eventCalls.selectionPropertyListener.getNumOfPropertyChangesCalls());
    assertEquals("beforeMsChange=1 afterMsChange=1", dlg.detailsTableHandler.detailsCalls.toString());
    assertEquals(0, dlg.detailsDetailsTable.eventCalls.selectionPropertyListener.getNumOfPropertyChangesCalls());

    dlg.cleanCallCounter();
    assertTrue(dlg.masterTable.getPmPageableBeanCollection().getModificationHandler().removeSelectedItems());

    assertColStrings("a", dlg.masterTable.name);
    assertColStrings("a.1", dlg.detailsTable.name);
    assertColStrings("a.1.1", dlg.detailsDetailsTable.name);

    setValue(dlg.detailsDetailsTable.getRowPms().get(0).name, "a.1.1'");

    assertColStrings("a.1.1'", dlg.detailsDetailsTable.name);
    assertEquals("{a={added: 0, updated: 1, removed: 0}}", dlg.detailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertEquals("{a.1={added: 0, updated: 1, removed: 0}}", dlg.detailsDetailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertEquals(2, dlg.masterTable.eventCalls.selectionPropertyListener.getNumOfPropertyChangesCalls());
    assertEquals(2, dlg.detailsTable.eventCalls.selectionPropertyListener.getNumOfPropertyChangesCalls());

    dlg.cleanCallCounter();
    assertTrue(dlg.masterTable.getPmPageableBeanCollection().getModificationHandler().removeSelectedItems());

    assertColStrings("", dlg.masterTable.name);
    assertColStrings("", dlg.detailsTable.name);
    assertColStrings("", dlg.detailsDetailsTable.name);
    assertEquals("[[a, null]]", dlg.detailsTableHandler.detailsCalls.after.toString());
    assertEquals(1, dlg.detailsTable.eventCalls.selectionPropertyListener.getNumOfPropertyChangesCalls());
    assertEquals("[[null, null]]", dlg.detailsDetailsTableHandler.detailsCalls.after.toString());
  }

  @Test
  public void testAllChangedEvent() {
    PmEventApi.broadcastPmEvent(dlg, PmEvent.ALL_CHANGE_EVENTS);
    assertEquals("onPmDataExchange=1 onPmValueChange=1", dlg.detailsArea.eventCalls.toString());
    assertEquals("onPmDataExchange=1 onPmValueChange=1", dlg.detailsTable.eventCalls.toString());
  }

  @Test
  public void testSelectRow2AndAllChangedEvent() {
    selectMasterRow(1);
    assertEquals("onPmDataExchange=1 onPmValueChange=1", dlg.detailsArea.eventCalls.toString());
    assertEquals("", dlg.detailsTable.eventCalls.toString());

    PmEventApi.broadcastPmEvent(dlg, PmEvent.ALL_CHANGE_EVENTS);
    assertEquals("onPmDataExchange=3 onPmValueChange=3", dlg.detailsArea.eventCalls.toString());
    assertEquals("onPmDataExchange=1 onPmValueChange=1", dlg.detailsTable.eventCalls.toString());
  }

  // TODO: A table utility is missing here
  private void selectMasterRow(int index) {
    dlg.masterTable.getPmPageableBeanCollection().getSelectionHandler().select(true, dlg.masterTableBeans.get(index));
  }

  private void selectDetailsRow(int index) {
    dlg.detailsTable.getPmPageableCollection().getSelectionHandler().select(true, dlg.detailsTable.getRowPms().get(index));
  }
}

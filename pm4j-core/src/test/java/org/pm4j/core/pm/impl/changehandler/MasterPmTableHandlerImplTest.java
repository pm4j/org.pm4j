package org.pm4j.core.pm.impl.changehandler;

import static org.junit.Assert.assertEquals;
import static org.pm4j.tools.test._PmTableAssert.assertColStrings;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmInitApi;
import org.pm4j.core.pm.impl.changehandler.MasterPmTableHandlerImplTestDlg.Bean;
import org.pm4j.tools.test._PmTableAssert;


/**
 * Tests for {@link MasterPmTableHandlerImpl},
 *
 * @author Olaf Boede
 */
public class MasterPmTableHandlerImplTest {

  private MasterPmTableHandlerImplTestDlg dlg = PmInitApi.initPmTree(new MasterPmTableHandlerImplTestDlg());

  @Before
  public void setUp() {
    assertEquals("", dlg.detailsArea.eventCalls.toString());
    assertEquals("", dlg.detailsTable.eventCalls.toString());

    // FIXME oboede: automatic single select is not yet ensured.
    assertEquals(null, dlg.masterTable.getMasterRowPmBean());
    selectMasterRow(0);

    assertEquals("a", dlg.masterTable.getMasterRowPmBean().toString());
    assertEquals("a", dlg.detailsArea.getPmBean().toString());
    assertColStrings("a, b", dlg.masterTable.name);
    assertColStrings("a.1", dlg.detailsTable.name);

    // FIXME oboede: should have been called on selection of 'a'.
    assertEquals("[]", dlg.detailsAreaHandler.detailsCalls.before.toString());
    assertEquals("onPmDataExchange=1 onPmValueChange=1", dlg.detailsArea.eventCalls.toString());
    assertEquals("onPmDataExchange=1 onPmValueChange=1", dlg.detailsTable.eventCalls.toString());

    dlg.detailsArea.eventCalls.clear();
    dlg.detailsTable.eventCalls.clear();
    assertEquals("", dlg.detailsArea.eventCalls.toString());
    assertEquals("", dlg.detailsTable.eventCalls.toString());
  }

  @Test
  public void selectSecondAndFirstRowAgain() {
    selectMasterRow(1);
    assertEquals("b", dlg.masterTable.getMasterRowPmBean().toString());
    assertEquals("b", dlg.detailsArea.getPmBean().toString());
    assertColStrings("b.1", dlg.detailsTable.name);

    assertEquals("[[a, b]]", dlg.detailsAreaHandler.detailsCalls.before.toString());
    assertEquals("onPmDataExchange=1 onPmValueChange=1", dlg.detailsArea.eventCalls.toString());
    assertEquals("onPmDataExchange=1 onPmValueChange=1", dlg.detailsTable.eventCalls.toString());

    selectMasterRow(0);
    assertEquals("a", dlg.masterTable.getMasterRowPmBean().toString());
    assertEquals("a", dlg.detailsArea.getPmBean().toString());
    assertColStrings("a.1", dlg.detailsTable.name);

    assertEquals("[[a, b], [b, a]]", dlg.detailsAreaHandler.detailsCalls.before.toString());
    assertEquals("onPmDataExchange=2 onPmValueChange=2", dlg.detailsArea.eventCalls.toString());
    assertEquals("onPmDataExchange=2 onPmValueChange=2", dlg.detailsTable.eventCalls.toString());
  }

  @Test
  public void testAddMasterRowAddDetailsDeleteMasterRow() {
    Bean newMasterBean = new Bean("M");
    dlg.masterTable.getPmPageableBeanCollection().getModificationHandler().addItem(newMasterBean);

    assertEquals("Modifications{added: 1, updated: 0, removed: 0}", dlg.masterTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{}", dlg.detailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertEquals("", dlg.detailsTable.eventCalls.toString());

    dlg.masterTable.getPmPageableBeanCollection().getSelectionHandler().select(true, newMasterBean);
    assertEquals("[[a, M]]", dlg.detailsTableHandler.detailsCalls.before.toString());
    assertEquals("onPmDataExchange=1 onPmValueChange=1", dlg.detailsTable.eventCalls.toString());

    dlg.detailsTable.getPmPageableBeanCollection().getModificationHandler().addItem(new Bean("D"));
    assertEquals("Modifications{added: 1, updated: 0, removed: 0}", dlg.masterTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{M=Modifications{added: 1, updated: 0, removed: 0}}", dlg.detailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertEquals("onPmDataExchange=1 onPmValueChange=2", dlg.detailsTable.eventCalls.toString());

    dlg.masterTable.getPmPageableBeanCollection().getModificationHandler().removeSelectedItems();
    assertEquals("Modifications{}", dlg.masterTable.getPmPageableBeanCollection().getModifications().toString());
    assertEquals("{}", dlg.detailsTableHandler.getMasterBeanToDetailsModificationsMap().toString());
    assertEquals("onPmDataExchange=2 onPmValueChange=3", dlg.detailsTable.eventCalls.toString());
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
    assertEquals("onPmDataExchange=1 onPmValueChange=1", dlg.detailsTable.eventCalls.toString());

    PmEventApi.broadcastPmEvent(dlg, PmEvent.ALL_CHANGE_EVENTS);
    assertEquals("onPmDataExchange=2 onPmValueChange=2", dlg.detailsArea.eventCalls.toString());
    assertEquals("onPmDataExchange=2 onPmValueChange=2", dlg.detailsTable.eventCalls.toString());
  }

  // TODO: A table utility is missing here
  private void selectMasterRow(int index) {
    dlg.masterTable.getPmPageableBeanCollection().getSelectionHandler().select(true, dlg.masterTableBeans.get(index));
  }
}

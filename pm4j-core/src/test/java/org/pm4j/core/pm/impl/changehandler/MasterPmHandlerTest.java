package org.pm4j.core.pm.impl.changehandler;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.impl.PmInitApi;

public class MasterPmHandlerTest {

  private MasterPmHandlerTestDlg dlg = PmInitApi.ensurePmInitialization(new MasterPmHandlerTestDlg());

  @Before
  public void setUp() {
    // FIXME oboede: automatic single select is not yet ensured.
    assertEquals(null, dlg.masterTable.getMasterRowPmBean());
    dlg.masterTable.getPmPageableCollection().getSelectionHandler().select(true, dlg.masterTable.getRowPms().get(0));
    assertEquals("a", dlg.masterTable.getMasterRowPmBean().toString());
    assertEquals("a", dlg.detailsArea.getPmBean().toString());

    // FIXME oboede: should have been called on selection of 'a'.
    assertEquals("[]", dlg.detailsAreaHandler.beforeCalls.toString());
  }


  @Test
  public void selectSecondAndThirdRow() {
    assertEquals("a", dlg.masterTable.getMasterRowPmBean().toString());
    assertEquals("a", dlg.detailsArea.getPmBean().toString());

    assertEquals("b", dlg.masterTableBeans.get(1).toString());
    dlg.masterTable.getPmPageableBeanCollection().getSelectionHandler().select(true, dlg.masterTableBeans.get(1));
    assertEquals("b", dlg.masterTable.getMasterRowPmBean().toString());
    assertEquals("b", dlg.detailsArea.getPmBean().toString());

    assertEquals("[[a, b]]", dlg.detailsAreaHandler.beforeCalls.toString());

    dlg.masterTable.getPmPageableBeanCollection().getSelectionHandler().select(true, dlg.masterTableBeans.get(0));
    assertEquals("a", dlg.masterTable.getMasterRowPmBean().toString());
    assertEquals("a", dlg.detailsArea.getPmBean().toString());

    assertEquals("[[a, b], [b, a]]", dlg.detailsAreaHandler.beforeCalls.toString());
  }

}

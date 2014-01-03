package org.pm4j.core.pm.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.core.pm.PmObject;

/**
 * White box tests for {@link PmTableImpl}.
 *
 * @author OBOEDE
 */
public class PmTableImplTest {

  @Test
  public void testGetPmRowBeanClass() {
    TablePm tpm = new TablePm(new PmConversationImpl());
    assertEquals(RowBean.class, tpm.getPmRowBeanClass());
  }

  static class RowBean {
  }

  static class RowBeanPm extends PmBeanImpl<RowBean> {

  }

  static class TablePm extends PmTableImpl<RowBeanPm, RowBean> {

    public TablePm(PmObject pmParent) {
      super(pmParent);
    }
  }
}

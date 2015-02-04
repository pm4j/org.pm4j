package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmTableCfg.RowsToValidate;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl;
import org.pm4j.core.pm.impl.PmTableImpl;
import org.pm4j.tools.test._PmAssert;

/**
 * Tests {@link PmTableImpl.TableValidator}.
 *
 * @author Olaf Boede
 */
public class PmTableValidationTest {

  @Test
  public void testValidateAllRows() {
    TablePm t = new TablePm(new PmConversationImpl()) {
      @Override
      protected Validator makePmValidator() {
        return new TableValidator(RowsToValidate.ALL);
      }
    };

    _PmAssert.validateSuccessful(t);
    assertEquals("[a, b, c]", t.validatedRows.toString());
  }

  @Test
  public void testValidateRowsOnPage() {
    TablePm t = new TablePm(new PmConversationImpl()) {
      @Override
      protected Validator makePmValidator() {
        return new TableValidator(RowsToValidate.CURRENT_PAGE);
      }
    };

    _PmAssert.validateSuccessful(t);
    assertEquals("[a, b]", t.validatedRows.toString());
  }

  @Test
  public void testValidateAddedRows() {
    TablePm t = new TablePm(new PmConversationImpl()) {
      @Override
      protected Validator makePmValidator() {
        return new TableValidator(RowsToValidate.ADDED);
      }
    };

    _PmAssert.validateSuccessful(t);
    assertEquals(0, t.validatedRows.size());

    t.getPmPageableBeanCollection().getModificationHandler().addItem(new RowBean("added"));
    _PmAssert.validateSuccessful(t);
    assertEquals("[added]", t.validatedRows.toString());
  }

  @Test
  public void testValidateUpdatedRows() {
    TablePm t = new TablePm(new PmConversationImpl()) {
      @Override
      protected Validator makePmValidator() {
        return new TableValidator(RowsToValidate.UPDATED);
      }
    };

    _PmAssert.validateSuccessful(t);
    assertEquals(0, t.validatedRows.size());

    _PmAssert.setValue(t.getRowPms().get(0).name, "updated name");
    _PmAssert.validateSuccessful(t);
    assertEquals("[updated name]", t.validatedRows.toString());
  }

  @Test
  public void testValidateNoRows() {
    TablePm t = new TablePm(new PmConversationImpl()) {
      @Override
      protected Validator makePmValidator() {
        return new TableValidator();
      }
    };

    _PmAssert.validateSuccessful(t);
    assertEquals("[]", t.validatedRows.toString());

    // perform some changes to demonstrate that they are not validated.
    _PmAssert.setValue(t.getRowPms().get(0).name, "updated name");
    t.getPmPageableBeanCollection().getModificationHandler().addItem(new RowBean("added"));
    _PmAssert.validateSuccessful(t);
    assertEquals("[]", t.validatedRows.toString());
  }

  @Test
  public void testDefaultValidation() {
    TablePm t = new TablePm(new PmConversationImpl()) {
      @Override
      protected boolean isDeprValidation() {
        return false;
      }
    };

    _PmAssert.validateSuccessful(t);
    assertEquals("[]", t.validatedRows.toString());
    t.validatedRows.clear();

    // perform some changes to demonstrate that they are not validated.
    t.getPmPageableBeanCollection().getModificationHandler().addItem(new RowBean("added"));
    _PmAssert.setValue(t.getRowPms().get(0).name, "updated name");

    _PmAssert.validateSuccessful(t);
    assertEquals("[updated name, added]", t.validatedRows.toString());
  }

  // -- test infrastructure --

  private List<RowBean> rowBeans = new ArrayList<RowBean>(Arrays.asList(
      new RowBean("a"),
      new RowBean("b"),
      new RowBean("c")
  ));

  @PmFactoryCfg(beanPmClasses=RowPm.class)
  public class TablePm extends PmTableImpl<RowPm, RowBean> {

    public final PmTableCol name = new PmTableColImpl(this);

    /** An internal row validation counter for this test. */
    List<RowPm> validatedRows = new ArrayList<RowPm>();

    /** Defines a page size of two items. */
    public TablePm(PmObject pmParent) {
      super(pmParent);
      setNumOfPageRowPms(2);
    }

    /** We use here an in-memory data table.
     * The table represents the items of the collection provided by this method. */
    @Override
    protected Collection<RowBean> getPmBeansImpl() {
      return rowBeans;
    }
  }

  @PmBeanCfg(beanClass=RowBean.class)
  public static class RowPm extends PmBeanImpl<RowBean> {
    public final PmAttrString name = new PmAttrStringImpl(this);

    @Override
    public void pmValidate() {
      super.pmValidate();
      ((TablePm)getPmParent()).validatedRows.add(this);
    }

    /** for debugging */
    @Override
    public String toString() {
      return getPmBean().toString();
    }
  }

  public static class RowBean {
    public String name;

    public RowBean(String name) {
      this.name = name;
    }

    /** for debugging */
    @Override
    public String toString() {
      return name;
    }
  }

}

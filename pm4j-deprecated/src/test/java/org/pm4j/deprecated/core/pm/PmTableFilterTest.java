package org.pm4j.deprecated.core.pm;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.deprecated.core.pm.DeprPmTableCol;
import org.pm4j.deprecated.core.pm.annotation.DeprFilterByCfg;
import org.pm4j.deprecated.core.pm.annotation.DeprPmTableColCfg;
import org.pm4j.deprecated.core.pm.filter.DeprFilterByDefinition;
import org.pm4j.deprecated.core.pm.filter.DeprFilterItem;
import org.pm4j.deprecated.core.pm.filter.DeprPmFilterItem;
import org.pm4j.deprecated.core.pm.filter.DeprPmFilterSet;
import org.pm4j.deprecated.core.pm.filter.impl.DeprCompOpStringContains;
import org.pm4j.deprecated.core.pm.filter.impl.DeprCompOpStringEquals;
import org.pm4j.deprecated.core.pm.filter.impl.DeprCompOpStringNotContains;
import org.pm4j.deprecated.core.pm.filter.impl.DeprCompOpStringNotEquals;
import org.pm4j.deprecated.core.pm.filter.impl.DeprFilterByPmAttrValue;
import org.pm4j.deprecated.core.pm.filter.impl.DeprFilterByPmAttrValueLocalized;
import org.pm4j.deprecated.core.pm.filter.impl.DeprFilterItemFilter;
import org.pm4j.deprecated.core.pm.filter.impl.DeprPmFilterSetDefaultImpl;
import org.pm4j.deprecated.core.pm.impl.DeprPmTableColImpl;
import org.pm4j.deprecated.core.pm.impl.DeprPmTableImpl;
import org.pm4j.deprecated.core.pm.pageable.DeprPageablePmsForBeans;


public class PmTableFilterTest {

  @PmFactoryCfg(beanPmClasses=RowPm.class)
  public static class TablePm extends DeprPmTableImpl<RowPm> {

    /** A column with an annotation based filter definition. */
    @DeprPmTableColCfg(filterBy=@DeprFilterByCfg())
    public final DeprPmTableCol name = new DeprPmTableColImpl(this);

    /** A column with a method based filter defintion. */
    public final DeprPmTableCol description = new DeprPmTableColImpl(this) {
      @Override
      protected DeprFilterByDefinition getFilterByDefinitionImpl() {
        return new DeprFilterByPmAttrValueLocalized(this);
      }
    };

    /** A column with a filter annotation that defines . */
    @DeprPmTableColCfg(
        filterBy=@DeprFilterByCfg(
            /** Filters by comparing the row attribute value against the entered filter value. */
            value=DeprFilterByPmAttrValue.class,
            /** Uses an integer attribute to enter the compare-to value. */
            valueAttrPm=PmAttrIntegerImpl.class
        ) )
    public final DeprPmTableCol counter = new DeprPmTableColImpl(this);

    /** ctor */
    public TablePm(PmObject pmParent) { super(pmParent); }
  }



  private TablePm myTablePm;
  private List<RowBean> editedRowBeanList;

  @Before
  public void setUp() {
    editedRowBeanList = new ArrayList<PmTableFilterTest.RowBean>(Arrays.asList(
        new RowBean("a", "an 'a'", 1),
        new RowBean("b", "a 'b'", 2),
        new RowBean("c", "a 'c'", 3)
    ));

    myTablePm = new TablePm(new PmConversationImpl());
    myTablePm.setPageableCollection(new DeprPageablePmsForBeans<RowPm, RowBean>(myTablePm, editedRowBeanList), false);
  }

  @Test
  public void testUnfilteredTable() {
    assertEquals("The table displays 3 rows for 3 beans.", 3, myTablePm.getRows().size());
  }

  @Test
  public void testNameColumnValueAsStringFilter() {
    assertEquals("The name column has a single filter definition.", 1, myTablePm.name.getFilterByDefinitions().size());
    DeprFilterByDefinition fd = myTablePm.name.getFilterByDefinitions().iterator().next();

    assertEquals("The filter has the same name as the column", myTablePm.name.getPmName(), fd.getName());
    assertEquals("The filter has the same title as the column", myTablePm.name.getPmTitle(), fd.getTitle());

    // Create a simple filter item, as the user can do in a filter UI.
    DeprFilterItem filterItem = new DeprFilterItem();
    filterItem.setFilterBy(fd);
    filterItem.setCompOp(new DeprCompOpStringEquals(myTablePm.name));
    filterItem.setFilterByValue("a");

    assertEquals("The unfiltered table displays 3 rows for 3 beans.", 3, myTablePm.getRows().size());
    myTablePm.setFilter("f", new DeprFilterItemFilter(filterItem));
    assertEquals("The filtered table displays 1 row.", 1, myTablePm.getRows().size());
    assertEquals("The filter result item has the name 'a'.", "a", myTablePm.getRows().get(0).name.getValue());

    filterItem.setCompOp(new DeprCompOpStringNotEquals(myTablePm.name));
    myTablePm.setFilter("f", new DeprFilterItemFilter(filterItem));
    assertEquals("The inverse filtered table displays 2 row.", 2, myTablePm.getRows().size());
  }

  @Test
  public void testDescrColumnValueAsStringFilter() {
    assertEquals("The description column has a single filter definition.", 1, myTablePm.description.getFilterByDefinitions().size());
    DeprFilterByDefinition fd = myTablePm.description.getFilterByDefinitions().iterator().next();

    assertEquals("The filter has the same name as the column", myTablePm.description.getPmName(), fd.getName());
    assertEquals("The filter has the same title as the column", myTablePm.description.getPmTitle(), fd.getTitle());

    // Create a simple filter item, as the user can do in a filter UI.
    DeprFilterItem filterItem = new DeprFilterItem();
    filterItem.setFilterBy(fd);
    filterItem.setCompOp(new DeprCompOpStringContains(myTablePm.name));
    filterItem.setFilterByValue("'a'");

    assertEquals("The unfiltered table displays 3 rows for 3 beans.", 3, myTablePm.getRows().size());
    myTablePm.setFilter("f", new DeprFilterItemFilter(filterItem));
    assertEquals("The filtered table displays 1 row.", 1, myTablePm.getRows().size());
    assertEquals("The filter result item has the name 'a'.", "a", myTablePm.getRows().get(0).name.getValue());

    filterItem.setCompOp(new DeprCompOpStringNotContains(myTablePm.name));
    myTablePm.setFilter("f", new DeprFilterItemFilter(filterItem));
    assertEquals("The inverse filtered table displays 2 row.", 2, myTablePm.getRows().size());
  }

  @Test
  public void testFilterPm() {
    DeprPmFilterSet pmFilterSet = new DeprPmFilterSetDefaultImpl(myTablePm);
    ((DeprPmFilterSetDefaultImpl)pmFilterSet).setNumOfFilterConditionLines(3);

    assertEquals("AND", pmFilterSet.getCombinedBy().getValueAsString());

    assertEquals("Three filter lines are configured.", 3, pmFilterSet.getFilterItems().getValue().size());
    DeprPmFilterItem item = pmFilterSet.getFilterItems().getValue().get(0);
    assertEquals("There are two columns to filter by and a null-option.", 4, item.getFilterBy().getOptionSet().getSize().intValue());

    item.getFilterBy().setValueAsString("name");
    item.getCompOp().setValueAsString(DeprCompOpStringEquals.NAME);

    assertEquals(true, item.getFilterByValue().isPmVisible());
    item.getFilterByValue().setValueAsString("a");

    assertEquals("The entered filter criteria should have been written to the filter item.",
        "name", item.getPmBean().getFilterBy().getName());
    assertEquals("The entered filter criteria should have been written to the filter item.",
        DeprCompOpStringEquals.NAME, item.getPmBean().getCompOp().getName());
    assertEquals("The entered filter criteria should have been written to the filter item.",
        "a", item.getPmBean().getFilterByValue());
    assertEquals("The values with the entered criteria is the first in the filter item list.",
        item, pmFilterSet.getFilterItems().getValue().get(0));

    assertEquals(CommandState.EXECUTED, pmFilterSet.getCmdApply().doIt().getCommandState());
    assertEquals("The filtered table displays 1 row.", 1, myTablePm.getRows().size());

    assertEquals(CommandState.EXECUTED, pmFilterSet.getCmdClear().doIt().getCommandState());
    assertEquals("The clear command does not affect the active filter.", 1, myTablePm.getRows().size());

    assertEquals(CommandState.EXECUTED, pmFilterSet.getCmdApply().doIt().getCommandState());
    assertEquals("The apply command makes the filter change active.", 3, myTablePm.getRows().size());
  }

  @PmBeanCfg(beanClass=RowBean.class)
  public static class RowPm extends PmBeanBase<RowBean> {
    public final PmAttrString name = new PmAttrStringImpl(this);
    public final PmAttrString description = new PmAttrStringImpl(this);
    public final PmAttrInteger counter = new PmAttrIntegerImpl(this);
  }

  public static class RowBean {
    public String name;
    public String description;
    public Integer counter;

    public RowBean(String name, String description, int counter) {
      this.name = name;
      this.description = description;
      this.counter = counter;
    }
  }

}

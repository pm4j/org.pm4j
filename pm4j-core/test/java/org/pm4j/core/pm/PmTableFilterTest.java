package org.pm4j.core.pm;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmFilterCfg;
import org.pm4j.core.pm.annotation.PmTableColCfg;
import org.pm4j.core.pm.filter.FilterByDefinition;
import org.pm4j.core.pm.filter.FilterItem;
import org.pm4j.core.pm.filter.PmFilterItem;
import org.pm4j.core.pm.filter.PmFilterSet;
import org.pm4j.core.pm.filter.impl.CompOpStringContains;
import org.pm4j.core.pm.filter.impl.CompOpStringEquals;
import org.pm4j.core.pm.filter.impl.CompOpStringNotContains;
import org.pm4j.core.pm.filter.impl.CompOpStringNotEquals;
import org.pm4j.core.pm.filter.impl.FilterByPmAttrValueLocalized;
import org.pm4j.core.pm.filter.impl.FilterItemFilter;
import org.pm4j.core.pm.filter.impl.PmFilterSetDefaultImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl;
import org.pm4j.core.pm.impl.PmTableImpl;
import org.pm4j.core.pm.pageable.PageablePmsForBeans;


public class PmTableFilterTest {

  /** A table with some column filter definitions. */
  @PmFactoryCfg(beanPmClasses=RowPm.class)
  public static class TablePm extends PmTableImpl<RowPm> {
    public TablePm(PmObject pmParent) { super(pmParent); }

    public final PmTableCol name = new PmTableColImpl(this) {
      protected FilterByDefinition getFilterByDefinitionImpl() {
        return new FilterByPmAttrValueLocalized(this);
      };
    };

    @PmTableColCfg(filterBy=@PmFilterCfg())
    public final PmTableCol description = new PmTableColImpl(this);
  }



  private TablePm myTablePm;
  private List<RowBean> editedRowBeanList;

  @Before
  public void setUp() {
    editedRowBeanList = new ArrayList<PmTableFilterTest.RowBean>(Arrays.asList(
        new RowBean("a", "an 'a'"),
        new RowBean("b", "a 'b'"),
        new RowBean("c", "a 'c'")
    ));

    myTablePm = new TablePm(new PmConversationImpl());
    myTablePm.setPageableCollection(new PageablePmsForBeans<RowPm, RowBean>(myTablePm, editedRowBeanList), false);
  }

  @Test
  public void testUnfilteredTable() {
    assertEquals("The table displays 3 rows for 3 beans.", 3, myTablePm.getRows().size());
  }

  @Test
  public void testNameColumnValueAsStringFilter() {
    assertEquals("The name column has a single filter definition.", 1, myTablePm.name.getFilterByDefinitions().size());
    FilterByDefinition fd = myTablePm.name.getFilterByDefinitions().iterator().next();

    assertEquals("The filter has the same name as the column", myTablePm.name.getPmName(), fd.getName());
    assertEquals("The filter has the same title as the column", myTablePm.name.getPmTitle(), fd.getTitle());

    // Create a simple filter item, as the user can do in a filter UI.
    FilterItem filterItem = new FilterItem();
    filterItem.setFilterBy(fd);
    filterItem.setCompOp(new CompOpStringEquals(myTablePm.name));
    filterItem.setFilterByValue("a");

    assertEquals("The unfiltered table displays 3 rows for 3 beans.", 3, myTablePm.getRows().size());
    myTablePm.setFilter("f", new FilterItemFilter(filterItem));
    assertEquals("The filtered table displays 1 row.", 1, myTablePm.getRows().size());
    assertEquals("The filter result item has the name 'a'.", "a", myTablePm.getRows().get(0).name.getValue());

    filterItem.setCompOp(new CompOpStringNotEquals(myTablePm.name));
    myTablePm.setFilter("f", new FilterItemFilter(filterItem));
    assertEquals("The inverse filtered table displays 2 row.", 2, myTablePm.getRows().size());
  }

  @Test
  public void testDescrColumnValueAsStringFilter() {
    assertEquals("The description column has a single filter definition.", 1, myTablePm.description.getFilterByDefinitions().size());
    FilterByDefinition fd = myTablePm.description.getFilterByDefinitions().iterator().next();

    assertEquals("The filter has the same name as the column", myTablePm.description.getPmName(), fd.getName());
    assertEquals("The filter has the same title as the column", myTablePm.description.getPmTitle(), fd.getTitle());

    // Create a simple filter item, as the user can do in a filter UI.
    FilterItem filterItem = new FilterItem();
    filterItem.setFilterBy(fd);
    filterItem.setCompOp(new CompOpStringContains(myTablePm.name));
    filterItem.setFilterByValue("'a'");

    assertEquals("The unfiltered table displays 3 rows for 3 beans.", 3, myTablePm.getRows().size());
    myTablePm.setFilter("f", new FilterItemFilter(filterItem));
    assertEquals("The filtered table displays 1 row.", 1, myTablePm.getRows().size());
    assertEquals("The filter result item has the name 'a'.", "a", myTablePm.getRows().get(0).name.getValue());

    filterItem.setCompOp(new CompOpStringNotContains(myTablePm.name));
    myTablePm.setFilter("f", new FilterItemFilter(filterItem));
    assertEquals("The inverse filtered table displays 2 row.", 2, myTablePm.getRows().size());
  }

  @Test
  public void testFilterPm() {
    PmFilterSet pmFilterSet = new PmFilterSetDefaultImpl(myTablePm);
    ((PmFilterSetDefaultImpl)pmFilterSet).setNumOfFilterConditionLines(3);

    assertEquals("Three filter lines are configured.", 3, pmFilterSet.getFilterItems().getValue().size());
    PmFilterItem item = pmFilterSet.getFilterItems().getValue().get(0);
    assertEquals("There are two columns to filter by and a null-option.", 3, item.getFilterBy().getOptionSet().getSize().intValue());

    item.getFilterBy().setValueAsString("name");
    item.getCompOp().setValueAsString(CompOpStringEquals.NAME);

    assertEquals(true, item.getFilterByValue().isPmVisible());
    item.getFilterByValue().setValueAsString("a");

    assertEquals("The entered filter criteria should have been written to the filter item.",
        "name", item.getPmBean().getFilterBy().getName());
    assertEquals("The entered filter criteria should have been written to the filter item.",
        CompOpStringEquals.NAME, item.getPmBean().getCompOp().getName());
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
  }

  public static class RowBean {
    public String name;
    public String description;

    public RowBean(String name, String description) {
      this.name = name;
      this.description = description;
    }
  }

}

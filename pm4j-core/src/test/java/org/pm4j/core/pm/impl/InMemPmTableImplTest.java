package org.pm4j.core.pm.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmAttrDate;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTab;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmTableCfg;
import org.pm4j.core.pm.annotation.PmTableColCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;

/**
 * Test for an in memory table.
 * 
 * @author MHELLER
 */
public class InMemPmTableImplTest {   
  
  static class Bean {
    
    private int id;
    
    private String val1;
    
    private Integer val2;
    
    private Date val3;
    
    public Bean(int id, String val1, Integer val2, Date val3) {
      this.id = id;
      this.val1 = val1;
      this.val2 = val2;
      this.val3 = val3;
    }
    
    @Override
    public String toString() {
      return "[" + val1 + ", " + val2 + ", " + val3 + "]";
    }
  }

  @PmBeanCfg(beanClass = Bean.class)
  public static class RowBeanPm extends PmBeanBase<Bean> {

    public final PmAttrString val1 = new PmAttrStringImpl(this);

    public final PmAttrInteger val2 = new PmAttrIntegerImpl(this);

    public final PmAttrDate val3 = new PmAttrDateImpl(this);
  }

  @PmFactoryCfg(beanPmClasses = RowBeanPm.class)  
  static class InMemTableBasePm extends PmTableImpl<RowBeanPm, Bean> {
    
    @PmTitleCfg(title = "Val1")
    public final PmTableCol val1 = new PmTableColImpl(this);
    
    @PmTitleCfg(title = "Val2")
    public final PmTableCol val2 = new PmTableColImpl(this);
    
    @PmTitleCfg(title = "Val3")
    public final PmTableCol val3 = new PmTableColImpl(this);

    public InMemTableBasePm(PmObject pmParent) {
      super(pmParent);      
    } 
  }
  
  @PmTableCfg(sortable = true, initialSortCols = "val1, val2, val3")
  static class InMemTable1Pm extends InMemTableBasePm {
    
    public InMemTable1Pm(PmObject pmParent) {
      super(pmParent);      
    } 
  }
  
  @PmTableCfg(sortable = true, initialSortCols = "val2, val3, val1")
  static class InMemTable2Pm extends InMemTableBasePm {
    
    public InMemTable2Pm(PmObject pmParent) {
      super(pmParent);      
    } 
  }
  
  @PmTableCfg(sortable = true, initialSortCols = "val3, val1, val2")
  static class InMemTable3Pm extends InMemTableBasePm {
    
    public InMemTable3Pm(PmObject pmParent) {
      super(pmParent);      
    } 
  }
  
  @PmTableCfg(sortable = true, initialSortCols = "val1 desc, val2 desc, val3")
  static class InMemTable4Pm extends InMemTableBasePm {
    
    public InMemTable4Pm(PmObject pmParent) {
      super(pmParent);      
    } 
  }
  
  @PmTableCfg(sortable = true, initialSortCols = "val1, val2 desc, val3 desc")
  static class InMemTable5Pm extends InMemTableBasePm {
    
    public InMemTable5Pm(PmObject pmParent) {
      super(pmParent);      
    } 
  }
  
  private List<Bean> entities;
  
  @Before
  public void setUp() {
    int id = 1;
    Calendar cal = Calendar.getInstance();
    cal.set(2015, Calendar.JUNE, 5, 0, 0, 0);
    Date d1 = cal.getTime();
    cal.set(2020, Calendar.SEPTEMBER, 20, 0, 0, 0);
    Date d2 = cal.getTime();
    
    entities = new ArrayList<Bean>();
    entities.add(new Bean(id++, "B", 2, d1));
    entities.add(new Bean(id++, "A", 2, d2));
    entities.add(new Bean(id++, "A", 2, d1));
    entities.add(new Bean(id++, "B", 1, d2));
    entities.add(new Bean(id++, "A", 1, d2));
    entities.add(new Bean(id++, "B", 1, d1));
    entities.add(new Bean(id++, "B", 2, d1));
    entities.add(new Bean(id++, "A", 1, d2));
  }
  
  @Test
  public void shouldHaveSortOrder1() { 
    
    int[] expectedSortOrderIds = new int[] {5, 8, 3, 2, 6, 4, 1, 7};
    
    InMemTable1Pm table = new InMemTable1Pm(new PmConversationImpl()) {
      @Override
      protected Collection<Bean> getPmBeansImpl() {
        return entities;
      }
    };
    
    shouldHaveExpectedSortOrder(table, expectedSortOrderIds);
  }
  
  @Test
  public void shouldHaveSortOrder2() { 
    int[] expectedSortOrderIds = new int[] {6, 5, 8, 4, 3, 1, 7, 2};
    
    InMemTable2Pm table = new InMemTable2Pm(new PmConversationImpl()) {
      @Override
      protected Collection<Bean> getPmBeansImpl() {
        return entities;
      }
    };
    
    shouldHaveExpectedSortOrder(table, expectedSortOrderIds);
  }
  
  @Test
  public void shouldHaveSortOrder3() { 
    int[] expectedSortOrderIds = new int[] {3, 6, 1, 7, 5, 8, 2, 4};
    
    InMemTable3Pm table = new InMemTable3Pm(new PmConversationImpl()) {
      @Override
      protected Collection<Bean> getPmBeansImpl() {
        return entities;
      }
    };
    
    shouldHaveExpectedSortOrder(table, expectedSortOrderIds);
  }
  
  @Test
  public void shouldHaveSortOrder4() { 
    int[] expectedSortOrderIds = new int[] {1, 7, 6, 4, 3, 2, 5, 8};
    
    InMemTable4Pm table = new InMemTable4Pm(new PmConversationImpl()) {
      @Override
      protected Collection<Bean> getPmBeansImpl() {
        return entities;
      }
    };
    
    shouldHaveExpectedSortOrder(table, expectedSortOrderIds);
  }
  
  @Test
  public void shouldHaveSortOrder5() { 
    int[] expectedSortOrderIds = new int[] {2, 3, 5, 8, 1, 7, 4, 6};
    
    InMemTable5Pm table = new InMemTable5Pm(new PmConversationImpl()) {
      @Override
      protected Collection<Bean> getPmBeansImpl() {
        return entities;
      }
    };
    
    shouldHaveExpectedSortOrder(table, expectedSortOrderIds);
  }
  
  private void shouldHaveExpectedSortOrder(InMemTableBasePm table, int[] expectedSortOrderIds) {
    
    int i = 0;
    PageableCollection<Bean> beans = table.getPmPageableBeanCollection();
    Iterator<Bean> it = beans.iterator();

    while(it.hasNext()) {
      assertEquals(expectedSortOrderIds[i++], it.next().id);
    }
  }
  
}

package org.pm4j.common.pageable.querybased;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.common.pageable.TestBean;
import org.pm4j.common.pageable.querybased.idquery.IdQueryServiceFake;
import org.pm4j.common.pageable.querybased.pagequery.PageQueryServiceFake;
import org.pm4j.common.query.CompOpEquals;
import org.pm4j.common.query.CompOpGt;
import org.pm4j.common.query.QueryExprCompare;

public class QueryServiceUtilTest {
  
  final IdQueryServiceFake<TestBean, Integer> idQueryService = new IdQueryServiceFake<TestBean, Integer>();
  final PageQueryServiceFake<TestBean, Integer> pageQueryService = new PageQueryServiceFake<TestBean, Integer>();
  
  @Before
  public void setUp() {
    for (int i=0; i<4; ++i) {
      TestBean b = new TestBean(Integer.toString(i));
      b.setId(i);
      idQueryService.save(b);
      pageQueryService.save(b);
    }
  }
  
  @Test
  public void testFindEqItemOnIdQueryService() {
    assertEquals("2", QueryServiceUtil.findEqItem(idQueryService, TestBean.ATTR_NAME, "2").toString());
  }
  
  @Test
  public void testFindEqItemOnPageQueryService() {
    assertEquals("2", QueryServiceUtil.findEqItem(pageQueryService, TestBean.ATTR_NAME, "2").toString());
  }

  @Test
  public void testFindAllItemsOnIdQueryService() {
    assertEquals("[0, 1, 2, 3]", QueryServiceUtil.findItems(idQueryService, null, 100).toString());
  }
  
  @Test
  public void testFindAllItemsOnPageQueryService() {
    assertEquals("[0, 1, 2, 3]", QueryServiceUtil.findItems(pageQueryService, null, 100).toString());
  }

  @Test
  public void testGetItemOnIdQueryService() {
    assertEquals("2", QueryServiceUtil.getItem(idQueryService, new QueryExprCompare(TestBean.ATTR_NAME, CompOpEquals.class, "2")).toString());
  }
  
  @Test
  public void testGetItemOnPageQueryService() {
    assertEquals("2", QueryServiceUtil.getItem(pageQueryService, new QueryExprCompare(TestBean.ATTR_NAME, CompOpEquals.class, "2")).toString());
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testFindItemThrowsExceptionIfMoreThanOneItemFound() {
    QueryServiceUtil.findItem(idQueryService, new QueryExprCompare(TestBean.ATTR_ID, CompOpGt.class, 0));
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testGetItemThrowsExceptionIfNoItemFound() {
    QueryServiceUtil.getItem(idQueryService, new QueryExprCompare(TestBean.ATTR_NAME, CompOpEquals.class, "Unknown"));
  }
  

  
}

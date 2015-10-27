package org.pm4j.common.query.inmem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pm4j.common.query.CompOpEquals;
import org.pm4j.common.query.CompOpGe;
import org.pm4j.common.query.CompOpGt;
import org.pm4j.common.query.CompOpLe;
import org.pm4j.common.query.CompOpLike;
import org.pm4j.common.query.CompOpLt;
import org.pm4j.common.query.CompOpNotEquals;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.query.QueryExprAnd;
import org.pm4j.common.query.QueryExprCompare;
import org.pm4j.common.query.QueryExprInMemCondition;
import org.pm4j.common.query.QueryExprNot;
import org.pm4j.common.query.QueryExprOr;
import org.pm4j.common.query.SortOrder;

public class InMemEvaluationTest {

  class Bean {
    public final String s;
    public final int i;
    public Bean(String s, int i) {
      this.s = s;
      this.i = i;
    }
    public Bean(String s) {
      this(s, 0);
    }
    public Bean(int i) {
      this("", i);
    }
    @Override
    public String toString() {
      return s+i;
    }
  }

  private Bean bean = new Bean("hi", 3);
  private InMemQueryEvaluator<Bean> ctxt = new InMemQueryEvaluator<Bean>();
  private QueryAttr attrS = new QueryAttr("s", String.class);
  private QueryAttr attrI = new QueryAttr("i", String.class);


  @Test
  public void testEvalSortOrder() {
    List<Bean> list = Arrays.asList(new Bean("a", 2), new Bean("b"), new Bean("a", 1));
    List<Bean> sorted = ctxt.sort(list, new SortOrder(attrS, attrI));
    
    assertEquals("[a1, a2, b0]", sorted.toString());
  }

  @Test
  public void testEvalSortOrderInverted() {
    List<Bean> list = Arrays.asList(new Bean("a", 1), new Bean("b"), new Bean("a", 2));
    List<Bean> sorted = ctxt.sort(list, new SortOrder(attrS, attrI).getReverseSortOrder());
    
    assertEquals("[b0, a2, a1]", sorted.toString());
  }


  @Test
  public void testIt() {
    assertTrue(ctxt.evaluate(bean, new QueryExprCompare(attrS, CompOpLt.class, "x")));
    assertFalse(ctxt.evaluate(bean, new QueryExprCompare(attrS, CompOpLt.class, "a")));
    assertTrue(ctxt.evaluate(bean,
        new QueryExprAnd(new QueryExprCompare(attrI, CompOpEquals.class, 3),
                      new QueryExprCompare(attrS, CompOpNotEquals.class, "y")) ));
  }

  @Test
  public void testAndFilter() {
    assertTrue("true", ctxt.evaluate(bean, new QueryExprAnd(trueCond())));
    assertTrue("true AND true", ctxt.evaluate(bean, new QueryExprAnd(trueCond(), trueCond())));
    assertTrue("true AND true AND true", ctxt.evaluate(bean, new QueryExprAnd(trueCond(), trueCond(), trueCond())));

    assertFalse("false", ctxt.evaluate(bean, new QueryExprAnd(falseCond())));
    assertFalse("true AND false", ctxt.evaluate(bean, new QueryExprAnd(trueCond(), falseCond())));
    assertFalse("false AND false", ctxt.evaluate(bean, new QueryExprAnd(falseCond(), falseCond())));
  }

  @Test
  public void testOrFilter() {
    assertTrue("true", ctxt.evaluate(bean, new QueryExprOr(trueCond())));
    assertTrue("true OR true", ctxt.evaluate(bean, new QueryExprOr(trueCond(), trueCond())));
    assertTrue("true OR true OR true", ctxt.evaluate(bean, new QueryExprOr(trueCond(), trueCond(), trueCond())));

    assertFalse("false", ctxt.evaluate(bean, new QueryExprOr(falseCond())));
    assertTrue("true OR false", ctxt.evaluate(bean, new QueryExprOr(trueCond(), falseCond())));
    assertTrue("false OR true", ctxt.evaluate(bean, new QueryExprOr(falseCond(), trueCond())));
    assertFalse("false OR false", ctxt.evaluate(bean, new QueryExprOr(falseCond(), falseCond())));
    assertTrue("false OR false OR true", ctxt.evaluate(bean, new QueryExprOr(falseCond(), falseCond(), trueCond())));
  }

  @Test
  public void testCompOpGt() {
    Bean bean = new Bean(3);
    assertFalse("The value 3 is not greater than 4.", ctxt.evaluate(bean, new QueryExprCompare(attrI, CompOpGt.class, 4)));
    assertFalse("The value 3 is not greater than 3.", ctxt.evaluate(bean, new QueryExprCompare(attrI, CompOpGt.class, 3)));
    assertTrue("The value 3 is greater than 2.", ctxt.evaluate(bean, new QueryExprCompare(attrI, CompOpGt.class, 2)));
    assertTrue("The value 3 is greater than null.", ctxt.evaluate(bean, new QueryExprCompare(attrI, CompOpGt.class, null)));
  }

  @Test
  public void testCompOpGe() {
    Bean bean = new Bean(3);
    assertFalse("The value 3 is not greater or equal 4.", ctxt.evaluate(bean, new QueryExprCompare(attrI, CompOpGe.class, 4)));
    assertTrue("The value 3 is greater or equal 3.", ctxt.evaluate(bean, new QueryExprCompare(attrI, CompOpGe.class, 3)));
    assertTrue("The value 3 is greater or equal 2.", ctxt.evaluate(bean, new QueryExprCompare(attrI, CompOpGe.class, 2)));
    assertTrue("The value 3 is greater or equal null.", ctxt.evaluate(bean, new QueryExprCompare(attrI, CompOpGe.class, null)));
  }

  @Test
  public void testCompOpLe() {
    Bean bean = new Bean(3);
    assertTrue("The value 3 is less or equal 4.", ctxt.evaluate(bean, new QueryExprCompare(attrI, CompOpLe.class, 4)));
    assertTrue("The value 3 is less or equal 3.", ctxt.evaluate(bean, new QueryExprCompare(attrI, CompOpLe.class, 3)));
    assertFalse("The value 3 is not less or equal 2.", ctxt.evaluate(bean, new QueryExprCompare(attrI, CompOpLe.class, 2)));
    assertFalse("The value 3 is not less or equal null.", ctxt.evaluate(bean, new QueryExprCompare(attrI, CompOpLe.class, null)));
  }

  @Test
  public void testCompOpLike() {
    Bean bean = new Bean("hello? ..[.[x]");
    assertTrue("Equal is also 'like'.", ctxt.evaluate(bean, new QueryExprCompare(attrS, CompOpLike.class, "hello? ..[.[x]")));

    assertTrue("Start wild card string match", ctxt.evaluate(bean, new QueryExprCompare(attrS, CompOpLike.class, "%lo? ..[.[x]")));
    assertTrue("Middle wild card string match", ctxt.evaluate(bean, new QueryExprCompare(attrS, CompOpLike.class, "h%lo? ..[.[x]")));
    assertTrue("Multi wild card string match", ctxt.evaluate(bean, new QueryExprCompare(attrS, CompOpLike.class, "%h%l%x%")));
    assertFalse("Start wild card string mismatch", ctxt.evaluate(bean, new QueryExprCompare(attrS, CompOpLike.class, "%alo? ..[.[x]")));

    assertTrue("Start wild card character match", ctxt.evaluate(bean, new QueryExprCompare(attrS, CompOpLike.class, "_ello? ..[.[x]")));
    assertTrue("Middle wild card character match", ctxt.evaluate(bean, new QueryExprCompare(attrS, CompOpLike.class, "h_llo? ..[.[x]")));
    assertTrue("Multi wild card character match", ctxt.evaluate(bean, new QueryExprCompare(attrS, CompOpLike.class, "h_ll_? ..[.[x]")));
    assertFalse("Start wild card character mismatch", ctxt.evaluate(bean, new QueryExprCompare(attrS, CompOpLike.class, "_ealo? ..[.[x]")));
  }

  @Test
  public void testNotFilter() {
    assertFalse("not true", ctxt.evaluate(bean, new QueryExprNot(trueCond())));
    assertTrue("not false", ctxt.evaluate(bean, new QueryExprNot(falseCond())));
  }

  @Test
  public void testComplexFilter() {
    QueryExpr e = new QueryExprAnd(
        new QueryExprAnd(trueCond(), trueCond()),
        new QueryExprOr(new QueryExprNot(falseCond()))
        );

    assertTrue("(true AND true) AND (NOT false)", ctxt.evaluate(bean, e));

    e = new QueryExprOr(
        new QueryExprNot(trueCond()),
        new QueryExprAnd(trueCond(), trueCond())
        );
    assertTrue("(NOT true) OR (true AND true)", ctxt.evaluate(bean, e));
  }

  @Test
  public void testInMemCondition() {
    QueryExpr oddTestExpr = new QueryExprInMemCondition<Integer>() {
      @Override
      public boolean eval(Integer item) {
        return item.intValue() % 2 != 0;
      }
    };

    assertTrue("One is odd.", ctxt.evaluate(1, oddTestExpr));
    assertFalse("Two is not odd.", ctxt.evaluate(2, oddTestExpr));
  }


  private QueryExpr trueCond() {
    return new QueryExprCompare(attrS, CompOpEquals.class, "hi");
  }

  private QueryExpr falseCond() {
    return new QueryExprNot(trueCond());
  }

}

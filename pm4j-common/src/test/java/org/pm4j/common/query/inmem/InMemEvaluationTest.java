package org.pm4j.common.query.inmem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pm4j.common.query.CompOpEquals;
import org.pm4j.common.query.CompOpGe;
import org.pm4j.common.query.CompOpGt;
import org.pm4j.common.query.CompOpLe;
import org.pm4j.common.query.CompOpLike;
import org.pm4j.common.query.CompOpLt;
import org.pm4j.common.query.CompOpNotEquals;
import org.pm4j.common.query.FilterAnd;
import org.pm4j.common.query.FilterCompare;
import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.FilterNot;
import org.pm4j.common.query.FilterOr;
import org.pm4j.common.query.QueryAttr;

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
  }

  private Bean bean = new Bean("hi", 3);
  private InMemQueryEvaluator<Bean> ctxt = new InMemQueryEvaluator<Bean>();
  private QueryAttr attrS = new QueryAttr("s", String.class);
  private QueryAttr attrI = new QueryAttr("i", String.class);


  @Test
  public void testIt() {
    assertTrue(ctxt.evaluate(bean, new FilterCompare(attrS, CompOpLt.class, "x")));
    assertFalse(ctxt.evaluate(bean, new FilterCompare(attrS, CompOpLt.class, "a")));
    assertTrue(ctxt.evaluate(bean,
        new FilterAnd(new FilterCompare(attrI, CompOpEquals.class, 3),
                      new FilterCompare(attrS, CompOpNotEquals.class, "y")) ));
  }

  @Test
  public void testAndFilter() {
    assertTrue("true", ctxt.evaluate(bean, new FilterAnd(trueCond())));
    assertTrue("true AND true", ctxt.evaluate(bean, new FilterAnd(trueCond(), trueCond())));
    assertTrue("true AND true AND true", ctxt.evaluate(bean, new FilterAnd(trueCond(), trueCond(), trueCond())));

    assertFalse("false", ctxt.evaluate(bean, new FilterAnd(falseCond())));
    assertFalse("true AND false", ctxt.evaluate(bean, new FilterAnd(trueCond(), falseCond())));
    assertFalse("false AND false", ctxt.evaluate(bean, new FilterAnd(falseCond(), falseCond())));
  }

  @Test
  public void testOrFilter() {
    assertTrue("true", ctxt.evaluate(bean, new FilterOr(trueCond())));
    assertTrue("true OR true", ctxt.evaluate(bean, new FilterOr(trueCond(), trueCond())));
    assertTrue("true OR true OR true", ctxt.evaluate(bean, new FilterOr(trueCond(), trueCond(), trueCond())));

    assertFalse("false", ctxt.evaluate(bean, new FilterOr(falseCond())));
    assertTrue("true OR false", ctxt.evaluate(bean, new FilterOr(trueCond(), falseCond())));
    assertTrue("false OR true", ctxt.evaluate(bean, new FilterOr(falseCond(), trueCond())));
    assertFalse("false OR false", ctxt.evaluate(bean, new FilterOr(falseCond(), falseCond())));
    assertTrue("false OR false OR true", ctxt.evaluate(bean, new FilterOr(falseCond(), falseCond(), trueCond())));
  }

  @Test
  public void testCompOpGt() {
    Bean bean = new Bean(3);
    assertFalse("The value 3 is not greater than 4.", ctxt.evaluate(bean, new FilterCompare(attrI, CompOpGt.class, 4)));
    assertFalse("The value 3 is not greater than 3.", ctxt.evaluate(bean, new FilterCompare(attrI, CompOpGt.class, 3)));
    assertTrue("The value 3 is greater than 2.", ctxt.evaluate(bean, new FilterCompare(attrI, CompOpGt.class, 2)));
    assertTrue("The value 3 is greater than null.", ctxt.evaluate(bean, new FilterCompare(attrI, CompOpGt.class, null)));
  }

  @Test
  public void testCompOpGe() {
    Bean bean = new Bean(3);
    assertFalse("The value 3 is not greater or equal 4.", ctxt.evaluate(bean, new FilterCompare(attrI, CompOpGe.class, 4)));
    assertTrue("The value 3 is greater or equal 3.", ctxt.evaluate(bean, new FilterCompare(attrI, CompOpGe.class, 3)));
    assertTrue("The value 3 is greater or equal 2.", ctxt.evaluate(bean, new FilterCompare(attrI, CompOpGe.class, 2)));
    assertTrue("The value 3 is greater or equal null.", ctxt.evaluate(bean, new FilterCompare(attrI, CompOpGe.class, null)));
  }

  @Test
  public void testCompOpLe() {
    Bean bean = new Bean(3);
    assertTrue("The value 3 is less or equal 4.", ctxt.evaluate(bean, new FilterCompare(attrI, CompOpLe.class, 4)));
    assertTrue("The value 3 is less or equal 3.", ctxt.evaluate(bean, new FilterCompare(attrI, CompOpLe.class, 3)));
    assertFalse("The value 3 is not less or equal 2.", ctxt.evaluate(bean, new FilterCompare(attrI, CompOpLe.class, 2)));
    assertFalse("The value 3 is not less or equal null.", ctxt.evaluate(bean, new FilterCompare(attrI, CompOpLe.class, null)));
  }

  @Test
  public void testCompOpLike() {
    Bean bean = new Bean("hello? ..[.[x]");
    assertTrue("Equal is also 'like'.", ctxt.evaluate(bean, new FilterCompare(attrS, CompOpLike.class, "hello? ..[.[x]")));

    assertTrue("Start wild card string match", ctxt.evaluate(bean, new FilterCompare(attrS, CompOpLike.class, "%lo? ..[.[x]")));
    assertTrue("Middle wild card string match", ctxt.evaluate(bean, new FilterCompare(attrS, CompOpLike.class, "h%lo? ..[.[x]")));
    assertTrue("Multi wild card string match", ctxt.evaluate(bean, new FilterCompare(attrS, CompOpLike.class, "%h%l%x%")));
    assertFalse("Start wild card string mismatch", ctxt.evaluate(bean, new FilterCompare(attrS, CompOpLike.class, "%alo? ..[.[x]")));

    assertTrue("Start wild card character match", ctxt.evaluate(bean, new FilterCompare(attrS, CompOpLike.class, "_ello? ..[.[x]")));
    assertTrue("Middle wild card character match", ctxt.evaluate(bean, new FilterCompare(attrS, CompOpLike.class, "h_llo? ..[.[x]")));
    assertTrue("Multi wild card character match", ctxt.evaluate(bean, new FilterCompare(attrS, CompOpLike.class, "h_ll_? ..[.[x]")));
    assertFalse("Start wild card character mismatch", ctxt.evaluate(bean, new FilterCompare(attrS, CompOpLike.class, "_ealo? ..[.[x]")));
  }

  @Test
  public void testNotFilter() {
    assertFalse("not true", ctxt.evaluate(bean, new FilterNot(trueCond())));
    assertTrue("not false", ctxt.evaluate(bean, new FilterNot(falseCond())));
  }

  @Test
  public void testComplexFilter() {
    FilterExpression e = new FilterAnd(
        new FilterAnd(trueCond(), trueCond()),
        new FilterOr(new FilterNot(falseCond()))
        );

    assertTrue("(true AND true) AND (NOT false)", ctxt.evaluate(bean, e));

    e = new FilterOr(
        new FilterNot(trueCond()),
        new FilterAnd(trueCond(), trueCond())
        );
    assertTrue("(NOT true) OR (true AND true)", ctxt.evaluate(bean, e));
  }


  private FilterExpression trueCond() {
    return new FilterCompare(attrS, CompOpEquals.class, "hi");
  }

  private FilterExpression falseCond() {
    return new FilterNot(trueCond());
  }

}

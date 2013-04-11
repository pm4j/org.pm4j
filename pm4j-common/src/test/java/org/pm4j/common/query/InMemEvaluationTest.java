package org.pm4j.common.query;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pm4j.common.query.inmem.InMemQueryEvaluator;

public class InMemEvaluationTest {

  class Bean {
    public String s;
    public int i;
    public Bean(String s, int i) {
      this.s = s;
      this.i = i;
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

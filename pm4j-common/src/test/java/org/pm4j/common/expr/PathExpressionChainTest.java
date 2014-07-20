package org.pm4j.common.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.pm4j.common.expr.PathExpressionChain.parse;

import org.junit.Test;

public class PathExpressionChainTest {

  Bean singleBean = new Bean();
  Bean oneLevelBean = new Bean(new Bean());
  Bean twoLevelBean = new Bean(new Bean(new Bean()));

  @Test
  public void testConcat() {
    assertEquals("Hello world!", parse("'Hello' + ' ' + 'world!'").getValue(null).toString());
  }

  @Test
  public void testGetValue() {
    twoLevelBean.child.child.i = 1;
    assertEquals(Integer.valueOf(1), parse("child.child.i").getValue(twoLevelBean));
  }

  @Test
  public void testAssignValue() {
    parse("child.i").setValue(oneLevelBean, 3);
    assertEquals(3, oneLevelBean.child.i);

  }

  @Test
  public void testAssignValueOnOptionalChain() {
    parse("(o)child.(o)child.i").setValue(twoLevelBean, 3);
    assertEquals(3, twoLevelBean.child.child.i);

  }

  @Test
  public void testAssignValueOnOptionalMissingChild() {
    parse("(o)child.i").setValue(singleBean, 3);
  }

  @Test
  public void testAssignValueOnMandatoryMissingChild() {
    try {
      parse("child.i").setValue(singleBean, 3);
      fail("Should throw an exception.");
    } catch (ExprExecExeption ex) {
      assertEquals("Mandatory expression returns 'null'.\n" +
          "Expression: child\n" +
          "Execution history:\n" +
          "  Start value: class org.pm4j.common.expr.PathExpressionChainTest$Bean\n" +
          "  child -> null",
          ex.getMessage());
    }
  }

  @Test
  public void testToString() {
    assertEquals("child.i", PathExpressionChain.parse("child.i").toString());
  }

  class Bean {
    public int i;
    public Bean child;
    public Bean() {}
    public Bean(Bean child) { this.child = child; }
  }
}

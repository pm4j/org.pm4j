package org.pm4j.common.expr;

import org.junit.Assert;
import org.junit.Test;

public class PathExpressionChainTest {

  @Test
  public void testConcat() {
    Expression e = PathExpressionChain.parse("'Hello' + ' ' + 'world!'", true);
    Assert.assertEquals("Hello world!", e.exec(new ExprExecCtxt(null)).toString());
  }

}

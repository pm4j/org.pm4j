package org.pm4j.common.expr;

import org.junit.Assert;
import org.junit.Test;
import org.pm4j.common.expr.parser.ParseCtxt;

public class PathExpressionChainTest {

  @Test
  public void testConcat() {
    Expression e = PathExpressionChain.parse(new ParseCtxt("'Hello' + ' ' + 'world!'"));
    Assert.assertEquals("Hello world!", e.exec(new ExprExecCtxt(null)).toString());
  }

}

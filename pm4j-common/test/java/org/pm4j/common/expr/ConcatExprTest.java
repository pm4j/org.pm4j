package org.pm4j.common.expr;

import org.junit.Assert;
import org.junit.Test;
import org.pm4j.common.expr.parser.ParseCtxt;

public class ConcatExprTest {

  @Test
  public void testIt2() {
    Expression e = PathExpressionChain.parse("'Hello' + ' ' + 'world!'", true);
    Assert.assertEquals("Hello world!", e.exec(new ExprExecCtxt(null)).toString());
  }

}

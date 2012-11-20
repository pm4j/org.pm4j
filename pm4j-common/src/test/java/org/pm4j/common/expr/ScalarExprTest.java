package org.pm4j.common.expr;

import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.PathExpressionChain;
import org.pm4j.common.expr.ScalarExpr;
import org.pm4j.common.expr.parser.ParseCtxt;

import junit.framework.TestCase;


public class ScalarExprTest extends TestCase {

  public void testNegativeDoubleDefault() {
    assertEquals(new Double("-123.4"), parseAndGet("-123.4"));
  }

  public void testFloat() {
    assertEquals(new Float("-123.4"), parseAndGet("-123.4f"));
  }

  public void testInteger() {
    assertEquals(new Integer("123"), parseAndGet("123"));
  }

  public void testNegativeInteger() {
    assertEquals(new Integer("-23"), parseAndGet("-23"));
  }

  public void testLong() {
    assertEquals(new Long("-23"), parseAndGet("-23l"));
  }

  public void testDouble() {
    assertEquals(new Double("-23"), parseAndGet("-23.0"));
  }

  public void testBooleanTrue() {
    assertEquals(Boolean.TRUE, parseAndGet("true"));
  }

  public void testBooleanWithTrueStartingOtherWord() {
    assertNull("A word starting with 'true' should not be parsed as a boolean.",
               ScalarExpr.parse(new ParseCtxt("trueTrue")));
  }

  public void testFunctionCallWithIntArg() {
    assertEquals(new Integer("-22"),
                 PathExpressionChain.parse(new ParseCtxt("fnAddOne(-23)")).exec(new ExprExecCtxt(this)));
  }

  public void testFunctionCallWithStringArg() {
    assertEquals("Hello world!",
                 PathExpressionChain.parse(new ParseCtxt("sayHello('world')")).exec(new ExprExecCtxt(this)));
  }

  public void testFunctionCallWithNullArg() {
    assertEquals("Hello null!",
                 PathExpressionChain.parse(new ParseCtxt("sayHello(null)")).exec(new ExprExecCtxt(this)));
  }


  private Object parseAndGet(String s) {
    return ScalarExpr.parse(new ParseCtxt(s)).exec(new ExprExecCtxt(null));
  }

  public Integer fnAddOne(int i) {
    return i+1;
  }

  public String sayHello(String s) {
    return "Hello " + s + "!";
  }
}

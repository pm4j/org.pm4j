package org.pm4j.core.pm.impl.expr.parser;

import org.pm4j.core.pm.impl.expr.parser.ParseCtxt;

import junit.framework.TestCase;

public class ParseCtxtTest extends TestCase {

  public void testReadNameString() {
    assertEquals("hello", new ParseCtxt("hello").readNameString());
    assertEquals("hEllo", new ParseCtxt("hEllo").readNameString());
    assertEquals(null, new ParseCtxt(" hello").readNameString());
    assertEquals("heLlo1_23", new ParseCtxt("heLlo1_23").readNameString());
    assertEquals("heLlo1", new ParseCtxt("heLlo1=23").readNameString());
    assertEquals(null, new ParseCtxt("11").readNameString());
    assertEquals(null, new ParseCtxt("").readNameString());
    assertEquals("_23x", new ParseCtxt("_23x").readNameString());
  }

  public void testSkipBlanks() {
    ParseCtxt c = new ParseCtxt("  hello");
    c.skipBlanks();
    assertEquals(2, c.getPos());
    assertEquals("hello", c.readNameString());
  }
}

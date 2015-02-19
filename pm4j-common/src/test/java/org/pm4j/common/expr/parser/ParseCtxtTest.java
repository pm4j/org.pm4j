package org.pm4j.common.expr.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ParseCtxtTest {

  @Test
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

  @Test
  public void testSkipBlanks() {
    ParseCtxt c = new ParseCtxt(" \t\t  hello");
    c.skipBlanks();
    assertEquals(5, c.getPos());
    assertEquals("hello", c.readNameString());
  }
  
  @Test
  public void testReadCharsAndAdvanceUntil() {
    ParseCtxt c = new ParseCtxt("(as:alias,o ) hello");

    c.readChar('(');
    String chars = c.readCharsAndAdvanceUntil(' ', ',', ')');
    assertEquals(9, c.getPos());
    assertEquals("as:alias", chars);

    c.readChar(',');
    chars = c.readCharsAndAdvanceUntil(' ', ',', ')');
    assertEquals(11, c.getPos());
    assertEquals("o", chars);
  }
  
  @Test
  public void isSpaceCharRecognizedTabs() {
    assertTrue("tab not recognized as space", ParseCtxt.isSpace('\t'));
  }
 
}

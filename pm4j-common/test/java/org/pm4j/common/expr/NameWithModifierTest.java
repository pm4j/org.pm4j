package org.pm4j.common.expr;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.common.expr.NameWithModifier;
import org.pm4j.common.expr.NameWithModifier.Modifier;
import org.pm4j.common.expr.parser.ParseCtxt;

public class NameWithModifierTest {

  @Test
  public void testParse() {
    NameWithModifier n = NameWithModifier.parseNameAndModifier(new ParseCtxt("hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(false, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(false, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(false, n.getModifiers().contains(Modifier.REPEATED));

    n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(o)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(true, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(false, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(false, n.getModifiers().contains(Modifier.REPEATED));

    n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(x)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(false, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(true, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(false, n.getModifiers().contains(Modifier.REPEATED));

    n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(x,o)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(true, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(true, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(false, n.getModifiers().contains(Modifier.REPEATED));

    n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(x,o,*)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(true, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(true, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(true, n.getModifiers().contains(Modifier.REPEATED));

  }

}

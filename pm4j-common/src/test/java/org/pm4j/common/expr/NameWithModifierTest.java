package org.pm4j.common.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.pm4j.common.expr.NameWithModifier.Modifier;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.common.expr.parser.ParseException;

@SuppressWarnings("deprecation")
public class NameWithModifierTest {

  @Test
  public void asTokenByItselfIsRecocognized() {
    NameWithModifier n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(as:myAlias)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(false, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(false, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(true, n.getModifiers().contains(Modifier.ALIAS));
    assertEquals("myAlias", n.getAlias());
  }

  @Test
  public void asTokenInbetweenOtherTagsIsRecocognized() {
    NameWithModifier n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(x,o,as:myAlias)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(true, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(true, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(true, n.getModifiers().contains(Modifier.ALIAS));
    assertEquals("myAlias", n.getAlias());
  }

  @Test
  public void incompleteAsModifierErrorMessage() {
    try {
      NameWithModifier.parseNameAndModifier(new ParseCtxt("(as:"));
      fail("Incomplete expression should fail.");
    } catch (ParseException e) {
      assertEquals("Unable to parse '(as:':\n" +
                   "Unexpected end of expression. One of the following characters expected: {',', ')'}\n" +
                   "Parse position: 4",
                   e.getMessage());
    }
  }

  @Test
  public void blanksAreSkipped() {
    NameWithModifier n = NameWithModifier.parseNameAndModifier(new ParseCtxt("( \t x  ,  o  ,  as  \t:  myAlias  )hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(true, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(true, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(true, n.getModifiers().contains(Modifier.ALIAS));
    assertEquals("myAlias", n.getAlias());
  }

  @Test
  public void asPathWithoutTokenRecocognized() {
    NameWithModifier n = NameWithModifier.parseNameAndModifier(new ParseCtxt("hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(false, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(false, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(false, n.getModifiers().contains(Modifier.ALIAS));
  }

  @Test
  public void oTokenAloneIsRecocognized() {
    NameWithModifier     n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(o)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(true, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(false, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(false, n.getModifiers().contains(Modifier.ALIAS));
  }

  @Test
  public void xTokenAloneIsRecocognized() {
    NameWithModifier     n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(x)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(false, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(true, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(false, n.getModifiers().contains(Modifier.ALIAS));
  }

  @Test
  public void xosTokenAreRecocognized() {
    NameWithModifier
    n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(x,o)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(true, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(true, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(false, n.getModifiers().contains(Modifier.ALIAS));
  }

  @Test
  public void xoTokenArRecocognized() {
    NameWithModifier n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(x,o)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(true, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(true, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(false, n.getModifiers().contains(Modifier.ALIAS));
  }

}

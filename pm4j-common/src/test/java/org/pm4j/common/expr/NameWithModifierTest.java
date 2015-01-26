package org.pm4j.common.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.common.expr.NameWithModifier.Modifier;
import org.pm4j.common.expr.parser.ParseCtxt;

public class NameWithModifierTest {

  @Test
  public void asTokenByItselfIsRecocognized() {    
    NameWithModifier n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(as:myAlias)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(false, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(false, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(false, n.getModifiers().contains(Modifier.REPEATED));
    assertEquals("myAlias", n.getAlias());
  }
  
  @Test
  public void asTokenInbetweenOtherTagsIsRecocognized() {    
    NameWithModifier n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(x,o,as:myAlias,*)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(true, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(true, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(true, n.getModifiers().contains(Modifier.REPEATED));
    assertEquals("myAlias", n.getAlias());
  }
  
  @Test
  public void blanksAreSkipped() {    
    NameWithModifier n = NameWithModifier.parseNameAndModifier(new ParseCtxt("( \t x  ,  o  ,  as  \t:  myAlias  ,  *  )hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(true, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(true, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(true, n.getModifiers().contains(Modifier.REPEATED));
    assertEquals("myAlias", n.getAlias());
  }
    
  @Test
  public void asPathWithoutTokenRecocognized() {    
    NameWithModifier n = NameWithModifier.parseNameAndModifier(new ParseCtxt("hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(false, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(false, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(false, n.getModifiers().contains(Modifier.REPEATED));
  }
    
  @Test
  public void oTokenAloneIsRecocognized() {    
    NameWithModifier     n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(o)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(true, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(false, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(false, n.getModifiers().contains(Modifier.REPEATED));
  }
    
  @Test
  public void xTokenAloneIsRecocognized() {    
    NameWithModifier     n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(x)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(false, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(true, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(false, n.getModifiers().contains(Modifier.REPEATED));
  }
    
  @Test
  public void xosTokenAreRecocognized() {    
    NameWithModifier     
    n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(x,o)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(true, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(true, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(false, n.getModifiers().contains(Modifier.REPEATED));
  }
    
  @Test
  public void xoAndAsterixTokenArRecocognized() {    
    NameWithModifier n = NameWithModifier.parseNameAndModifier(new ParseCtxt("(x,o,*)hallo"));
    assertEquals("hallo", n.getName());
    assertEquals(true, n.getModifiers().contains(Modifier.OPTIONAL));
    assertEquals(true, n.getModifiers().contains(Modifier.EXISTS_OPTIONALLY));
    assertEquals(true, n.getModifiers().contains(Modifier.REPEATED));
  }

}

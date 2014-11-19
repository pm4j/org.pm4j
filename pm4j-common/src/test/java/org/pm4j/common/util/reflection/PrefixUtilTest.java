package org.pm4j.common.util.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;


public class PrefixUtilTest {

  @SuppressWarnings("unused")
  private class TestBean {
    
    private String stringProp;
    private Boolean booleanInstanceProp;
    private boolean booleanPrimitiveProp;
    private String setterAndGetterWithoutPrefix;
    
    public String getStringProp() {
      return stringProp;
    }
    
    public void setStringProp(String stringProp) {
      this.stringProp = stringProp;
    }
    
    public Boolean isBooleanInstanceProp() {
      return booleanInstanceProp;
    }
    
    public void setBooleanInstanceProp(Boolean booleanInstanceProp) {
      this.booleanInstanceProp = booleanInstanceProp;
    }
    
    public boolean isBooleanPrimitiveProp() {
      return booleanPrimitiveProp;
    }
    
    public void setBooleanPrimitiveProp(boolean booleanPrimitiveProp) {
      this.booleanPrimitiveProp = booleanPrimitiveProp;
    }
    
    public String setterAndGetterWithoutPrefix() {
      return setterAndGetterWithoutPrefix;
    }

    public void setterAndGetterWithoutPrefix(String value) {
      setterAndGetterWithoutPrefix = value;
    }
}

  @Test
  public void testGetterSetterForStringProperty() {
    assertEquals("stringProp", PrefixUtil.propNameForGetter("getStringProp"));
    assertTrue("isGetterName(\"getStringProp\") failed", PrefixUtil.hasGetterPrefix("getStringProp"));
    assertEquals("setStringProp", PrefixUtil.addMethodNamePrefix("set", "stringProp"));
    
    Method getter = PrefixUtil.findGetterForName(TestBean.class, "getStringProp", String.class);
    assertEquals("getStringProp", getter.getName());
    assertTrue("isGetter() failed", PrefixUtil.hasGetterPrefix(getter));

    Method setter = PrefixUtil.findSetterForGetter(getter);
    assertEquals("setStringProp", setter.getName());
    assertFalse("isGetter() failed", PrefixUtil.hasGetterPrefix(setter));
        
    Method getterAgain = PrefixUtil.findGetterForSetter(setter);
    assertEquals(getter, getterAgain);
    
    String getterName = PrefixUtil.getterNameForSetter("setStringProp", String.class);
    assertEquals("getStringProp", getterName);
    
    String setterName = PrefixUtil.setterNameForGetter("getStringProp");
    assertEquals("setStringProp", setterName);   
  }
  
  @Test
  public void testSetterAndGetterWithoutPrefix() {
    assertEquals("setterAndGetterWithoutPrefix", PrefixUtil.propNameForGetter("setterAndGetterWithoutPrefix"));
    assertFalse("isGetterName(\"setterAndGetterWithoutPrefix\") failed", PrefixUtil.hasGetterPrefix("setterAndGetterWithoutPrefix"));
    
    Method getter = PrefixUtil.findGetterForName(TestBean.class, "setterAndGetterWithoutPrefix", String.class);
    assertEquals("setterAndGetterWithoutPrefix", getter.getName());
    assertFalse("isGetter() failed", PrefixUtil.hasGetterPrefix(getter));
    
    Method setter = PrefixUtil.findSetterForGetter(getter);
    assertEquals("setterAndGetterWithoutPrefix", setter.getName());
    assertFalse("isGetter() failed", PrefixUtil.hasGetterPrefix(setter));
        
    Method getterAgain = PrefixUtil.findGetterForSetter(setter);
    assertEquals(getter, getterAgain);
    
    String getterName = PrefixUtil.getterNameForSetter("setterAndGetterWithoutPrefix", String.class);
    assertEquals("setterAndGetterWithoutPrefix", getterName);
    
    String setterName = PrefixUtil.setterNameForGetter("setterAndGetterWithoutPrefix");
    assertEquals("setterAndGetterWithoutPrefix", setterName);   
  }
  
  @Test
  public void testGetterSetterForBooleanInstanceProperty() {
    assertEquals("booleanInstanceProp", PrefixUtil.propNameForGetter("isBooleanInstanceProp"));
    assertTrue("isGetterName(\"isBooleanInstanceProp\") failed", PrefixUtil.hasGetterPrefix("isBooleanInstanceProp"));
    assertEquals("setBooleanInstanceProp", PrefixUtil.addMethodNamePrefix("set", "booleanInstanceProp"));
    
    Method getter = PrefixUtil.findGetterForName(TestBean.class, "isBooleanInstanceProp", Boolean.class);
    assertEquals("isBooleanInstanceProp", getter.getName());
    assertTrue("isGetter() failed", PrefixUtil.hasGetterPrefix(getter));

    Method setter = PrefixUtil.findSetterForGetter(getter);
    assertEquals("setBooleanInstanceProp", setter.getName());
    assertFalse("isGetter() failed", PrefixUtil.hasGetterPrefix(setter));
        
    Method getterAgain = PrefixUtil.findGetterForSetter(setter);
    assertEquals(getter, getterAgain);
    
    String getterName = PrefixUtil.getterNameForSetter("setBooleanInstanceProp", Boolean.class);
    assertEquals("isBooleanInstanceProp", getterName);
    
    String setterName = PrefixUtil.setterNameForGetter("isBooleanInstanceProp");
    assertEquals("setBooleanInstanceProp", setterName);   
  }
 
  @Test
  public void testGetterSetterForBooleanPrimitiveProperty() {
    assertEquals("booleanPrimitiveProp", PrefixUtil.propNameForGetter("isBooleanPrimitiveProp"));
    assertTrue("isGetterName(\"getStringProp\") failed", PrefixUtil.hasGetterPrefix("isBooleanPrimitiveProp"));
    assertEquals("setBooleanPrimitiveProp", PrefixUtil.addMethodNamePrefix("set", "booleanPrimitiveProp"));
    
    Method getter = PrefixUtil.findGetterForName(TestBean.class, "isBooleanPrimitiveProp", boolean.class);
    assertEquals("isBooleanPrimitiveProp", getter.getName());
    assertTrue("isGetter() failed", PrefixUtil.hasGetterPrefix(getter));

    Method setter = PrefixUtil.findSetterForGetter(getter);
    assertEquals("setBooleanPrimitiveProp", setter.getName());
    assertFalse("isGetter() failed", PrefixUtil.hasGetterPrefix(setter));
        
    Method getterAgain = PrefixUtil.findGetterForSetter(setter);
    assertEquals(getter, getterAgain);
    
    String getterName = PrefixUtil.getterNameForSetter("setBooleanPrimitiveProp", boolean.class);
    assertEquals("isBooleanPrimitiveProp", getterName);
    
    String setterName = PrefixUtil.setterNameForGetter("isBooleanPrimitiveProp");
    assertEquals("setBooleanPrimitiveProp", setterName);   
  }
  
}

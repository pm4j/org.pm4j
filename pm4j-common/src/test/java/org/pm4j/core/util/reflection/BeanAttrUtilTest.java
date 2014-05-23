package org.pm4j.core.util.reflection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.common.util.reflection.BeanAttrUtil;

public class BeanAttrUtilTest {

  @Test
  public void testResolveDottedAttrPath() {
    MyBeanA a = new MyBeanA();
    a.refToB = new MyBeanB();

    assertEquals("hello", BeanAttrUtil.resolveReflectionPath(a, "refToB.myString"));
  }

  @Test
  public void testResolveDottedAttrPathForAnonymousClasses() {
    MyBeanA a = new MyBeanA() {};
    a.refToB = new MyBeanB() {};

    assertEquals("hello", BeanAttrUtil.resolveReflectionPath(a, "refToB.myString"));
  }

  public static class MyBeanA {
    public MyBeanB refToB;
  }

  public static class MyBeanB {
    public String myString = "hello";
  }
}

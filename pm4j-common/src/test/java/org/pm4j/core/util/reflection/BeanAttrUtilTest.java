package org.pm4j.core.util.reflection;

import junit.framework.TestCase;

public class BeanAttrUtilTest extends TestCase {

  public void testResolveDottedAttrPath() {
    MyBeanA a = new MyBeanA();
    a.refToB = new MyBeanB();

    assertEquals("hello", BeanAttrUtil.resolveReflectionPath(a, "refToB.myString"));
  }

  public static class MyBeanA {
    public MyBeanB refToB;
  }

  public static class MyBeanB {
    public String myString = "hello";
  }
}

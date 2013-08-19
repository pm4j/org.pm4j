package org.pm4j.core.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class ClassUtilTest extends TestCase {

  public static class MyBase {
    public String s0;
    public String s1;
    public String getS1() {
      return s1;
    }
  }
  public static class MySub extends MyBase {
    public String s2;
    public String s3;
    public String getS2() {
      return s2;
    }
    public String getS3a() {
      return s3;
    }
    public String getS3b() {
      return s3;
    }
    public MySub() {
      s0="0";
      s1="1";
      s2="2";
      s3="3";
    }
  }

  public void testFindPublicGetterByValueRef() throws Exception {
    MySub instance = new MySub();

    Method m1 = ClassUtil.findPublicGetterByValueRef(instance, instance.s1);
    assertEquals("getS1", m1.getName());
    assertEquals(instance.s1, m1.invoke(instance, new Object[]{}));
    Method m2 = ClassUtil.findPublicGetterByValueRef(instance, instance.s2);
    assertEquals("getS2", m2.getName());
    assertEquals(instance.s2, m2.invoke(instance, new Object[]{}));
  }

  public void testFindPublicFieldByValueRef() throws Exception {
    MySub instance = new MySub();

    Field f1 = ClassUtil.findPublicFieldByValueRef(instance, instance.s1);
    assertEquals("s1", f1.getName());
    assertEquals(instance.s1, f1.get(instance));
    Field f2 = ClassUtil.findPublicFieldByValueRef(instance, instance.s2);
    assertEquals("s2", f2.getName());
    assertEquals(instance.s2, f2.get(instance));
  }

  public void testFindAttrNameByValueRef() throws Exception {
    MySub instance = new MySub();

    assertEquals("s0", ClassUtil.findPublicAttrName(instance, instance.s0));
    assertEquals("s1", ClassUtil.findPublicAttrName(instance, instance.s1));
    assertEquals("s2", ClassUtil.findPublicAttrName(instance, instance.s2));

    Set<String> forbiddenGetterSet = new HashSet<String>();
    forbiddenGetterSet.add("getS3a");

    // the public field will be found before the getter getS3b.
    assertEquals("s3", ClassUtil.findPublicAttrName(instance, instance.s3, forbiddenGetterSet));

  }

  public static interface B<T> { }
  public static class C implements B<Integer> { }
  public static class D extends C { }
  public static class E<T> implements B<T> { }
  public static class F extends E<Float> { }


  public void testFindFirstGenericParameterOfInterface() {
//    assertEquals(Integer.class, ClassUtil.findFirstGenericParameterOfInterface(C.class, B.class));
//    assertEquals(Integer.class, ClassUtil.findFirstGenericParameterOfInterface(D.class, B.class));
//    assertEquals(Float.class, ClassUtil.findFirstGenericParameterOfInterface(F.class, B.class));

  }



}

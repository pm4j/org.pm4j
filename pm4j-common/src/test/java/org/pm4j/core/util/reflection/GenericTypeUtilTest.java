package org.pm4j.core.util.reflection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.common.util.reflection.GenericTypeUtil;

public class GenericTypeUtilTest {

  public static class B<T1, T2>{};
  public static class C extends B<String, Long> {};

  @Test
  public void testFindFirstGenericParameter() {
    assertEquals(String.class, GenericTypeUtil.resolveGenericArgument(B.class, C.class, 0));
    assertEquals(Long.class, GenericTypeUtil.resolveGenericArgument(B.class, C.class, 1));
  }

}

package org.pm4j.common.query.experimental;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pm4j.common.query.CompOp;
import org.pm4j.common.query.CompOpEquals;
import org.pm4j.common.query.CompOpGt;
import org.pm4j.common.query.CompOpLike;

public class CompOpCompatibilityCheckerTest {

  private CompOpCompatibilityChecker checker = CompOpCompatibilityUtil.registerDefaultCompOpValueMatches(new CompOpCompatibilityChecker());

  @Test
  public void testCompOpApplicableForValueClass() {
    assertTrue(checker.isCompOpApplicableForValueClass(CompOpEquals.class, Object.class));
    assertTrue(checker.isCompOpApplicableForValueClass(CompOpLike.class, String.class));
    assertFalse(checker.isCompOpApplicableForValueClass(CompOpLike.class, Object.class));
    assertTrue(checker.isCompOpApplicableForValueClass(CompOpGt.class, Short.class));
  }

  @Test
  public void testFindCompOpsForValueClass() {
    assertCompOpClassesForValueClass("CompOpEquals, CompOpIsNull, CompOpNotEquals, CompOpNotNull", Object.class);
    assertCompOpClassesForValueClass("CompOpEquals, CompOpGe, CompOpGt, CompOpIsNull, CompOpLe, CompOpLt, CompOpNotEquals, CompOpNotNull", Enum.class);
    assertCompOpClassesForValueClass("CompOpEquals, CompOpGe, CompOpGt, CompOpIsNull, CompOpLe, CompOpLt, CompOpNotEquals, CompOpNotNull", Long.class);
    assertCompOpClassesForValueClass("CompOpContains, CompOpEquals, CompOpGe, CompOpGt, CompOpIsNull, CompOpLe, CompOpLike, CompOpLt, CompOpNotContains, CompOpNotEquals, CompOpNotNull, CompOpStartsWith", String.class);
  }


  private void assertCompOpClassesForValueClass(String expected, Class<?> valueClass) {
    StringBuilder sb = new StringBuilder();
    for (Class<CompOp> c : checker.findCompsForValueClass(valueClass)) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(c.getSimpleName());
    }
    assertEquals(expected, sb.toString());
  }
}

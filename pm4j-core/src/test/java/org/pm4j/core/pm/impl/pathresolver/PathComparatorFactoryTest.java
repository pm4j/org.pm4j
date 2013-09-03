package org.pm4j.core.pm.impl.pathresolver;

import junit.framework.TestCase;

import org.pm4j.common.expr.Expression.SyntaxVersion;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PathComparatorFactoryTest extends TestCase {

  public static class A {
    public String s1, s2;
    public A child;

    public A(String s1, String s2) {
      this.s1 = s1;
      this.s2 = s2;
    }
    public A(A child) {
      this.child = child;
    }
  }

  private PmObject pm = new PmConversationImpl();
  private A a1a = new A(new A("-a1-", "a"));
  private A a1b = new A(new A("-a1-", "b"));
  private A a2 = new A(new A("-a2-", "1"));

  // XXX olaf: split in a method per test case
  public void testComparePath() {
    assertEquals(-1, parse("child.s1").getComparator(pm).compare(a1a, a2));
    assertEquals(1, parse("child.s1 desc").getComparator(pm).compare(a1a, a2));
    assertEquals(0, parse("child.s1").getComparator(pm).compare(a1a, a1b));
    assertEquals(-1, parse("child.s1, child.s2").getComparator(pm).compare(a1a, a1b));
    assertEquals(1, parse("child.s1, child.s2 desc").getComparator(pm).compare(a1a, a1b));
    assertEquals(1, parse("s1, child.s1, child.s2 desc").getComparator(pm).compare(a1a, a1b));
  }

  private PathComparatorFactory parse(String s) {
    return PathComparatorFactory.parse(s, SyntaxVersion.VERSION_2);
  }

}

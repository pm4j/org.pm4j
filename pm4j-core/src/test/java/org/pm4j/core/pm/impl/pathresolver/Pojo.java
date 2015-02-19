package org.pm4j.core.pm.impl.pathresolver;

/**
 * Class needed to illustrate Path resolving tests.
 * 
 */
public class Pojo {
  public String name;
  public Pojo   sub;

  public Pojo(String name, Pojo sub) {
    this.name = name;
    this.sub = sub;
  }

  public Pojo(String name) {
    this(name, null);
  }

  public Pojo getSubMethod() {
    return sub;
  }

  public String addAPlus(String s) {
    return s + "+";
  }

  public static Pojo make(String... names) {
    Pojo pojo = null;
    Pojo lastSub = null;
    for (String s : names) {
      Pojo p = new Pojo(s);
      if (pojo == null) {
        pojo = p;
      }

      if (lastSub != null) {
        lastSub.sub = p;
      }

      lastSub = p;
    }
    return pojo;
  }

}

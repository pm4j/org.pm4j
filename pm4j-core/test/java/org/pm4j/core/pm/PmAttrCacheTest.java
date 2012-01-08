package org.pm4j.core.pm;

import junit.framework.TestCase;

import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg.CacheMode;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmElementBase;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmAttrCacheTest extends TestCase {

  // -- Domain model --

  public static class MyPojo {
    public String s;

    // Has getters and setters to allow xPath access (used by valuePath annotation in MyPojoPm).
    public String getS() { return s; }
    public void setS(String s) { this.s = s; }
  }

  // -- Presentation model --

  @PmBeanCfg(beanClass=MyPojo.class)
  public static class MyPojoPm extends PmBeanBase<MyPojo> {
    public final PmAttrString s = new PmAttrStringImpl(this);

    @PmCacheCfg(value=CacheMode.ON) @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sCached = new PmAttrStringImpl(this);

    @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sCachedByClassSpec = new MyCachedAttrClass(this);

    /** The attribut class wants to cache but the atttribute declaration switches off. */
    @PmCacheCfg(all=CacheMode.OFF) @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sClassCacheSwitchedOff = new MyCachedAttrClass(this);
  }

  @PmCacheCfg(all=CacheMode.ON)
  public static class MyCachedAttrClass extends PmAttrStringImpl {
    public MyCachedAttrClass(PmElementBase pmParentBean) {
      super(pmParentBean);
    }
  };

  // -- Tests --

  public void testCache() {
    PmConversation session = new PmConversationImpl(MyPojoPm.class);
    MyPojo p = new MyPojo();
    MyPojoPm pPm = PmFactoryApi.getPmForBean(session, p);

    p.s = "abc";

    assertEquals(p.s, pPm.s.getValue());
    assertEquals(p.s, pPm.sCached.getValue());
    assertEquals(p.s, pPm.sCachedByClassSpec.getValue());
    assertEquals(p.s, pPm.sClassCacheSwitchedOff.getValue());

    p.s = "123";
    assertEquals(p.s, pPm.s.getValue());
    assertEquals("abc", pPm.sCached.getValue());
    assertEquals("abc", pPm.sCachedByClassSpec.getValue());
    assertEquals(p.s, pPm.sClassCacheSwitchedOff.getValue());

    PmCacheApi.clearCachedPmValues(pPm);
    assertEquals(p.s, pPm.s.getValue());
    assertEquals(p.s, pPm.sCached.getValue());
    assertEquals(p.s, pPm.sCachedByClassSpec.getValue());
    assertEquals(p.s, pPm.sClassCacheSwitchedOff.getValue());
  }

}

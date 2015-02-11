package org.pm4j.core.pm;

import org.junit.Test;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg2;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Cache;
import org.pm4j.core.pm.annotation.PmCacheCfg2.CacheMode;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Clear;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Observe;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.impl.*;

import static org.junit.Assert.assertEquals;

public class PmAttrCacheTest2 {

  @Test
  public void testSimpleAttributeCache() {
    MyPojo p = new MyPojo();
    MyPojoPm pPm = new MyPojoPm(new PmConversationImpl(), p);

    p.s = "abc";

    assertEquals(p.s, pPm.s.getValue());
    assertEquals(p.s, pPm.sCached.getValue());
    assertEquals(p.s, pPm.sCachedByClassSpec.getValue());
    assertEquals(p.s, pPm.sClassCacheSwitchedOff.getValue());
    assertEquals(p.s, pPm.sCachedButClearNever.getValue());
    assertEquals(p.s, pPm.sCachedAndClearedOnValueChange.getValue());

    p.s = "123";
    assertEquals(p.s, pPm.s.getValue());
    assertEquals("abc", pPm.sCached.getValue());
    assertEquals("abc", pPm.sCachedByClassSpec.getValue());
    assertEquals(p.s, pPm.sClassCacheSwitchedOff.getValue());
    assertEquals("abc", pPm.sCachedButClearNever.getValue());
    assertEquals("abc", pPm.sCachedAndClearedOnValueChange.getValue());

    PmCacheApi.clearPmCache(pPm);
    assertEquals(p.s, pPm.s.getValue());
    assertEquals(p.s, pPm.sCached.getValue());
    assertEquals(p.s, pPm.sCachedByClassSpec.getValue());
    assertEquals(p.s, pPm.sClassCacheSwitchedOff.getValue());
    assertEquals("abc", pPm.sCachedButClearNever.getValue());
    assertEquals(p.s, pPm.sCachedAndClearedOnValueChange.getValue());
  }

  @Test
  public void testParentDefinedValueCacheModeAppliedToChildren() {
    MyPojo p = new MyPojo();
    MyPojoPm pPm = new MyPojoPmWithParentDefinedValueCacheModeCascaded(new PmConversationImpl(), p);

    p.s = "abc";

    assertEquals(p.s, pPm.s.getValue());
    assertEquals(p.s, pPm.sCached.getValue());
    assertEquals(p.s, pPm.sCachedByClassSpec.getValue());
    assertEquals(p.s, pPm.sClassCacheSwitchedOff.getValue());
    assertEquals(p.s, pPm.sCachedButClearNever.getValue());

    p.s = "123";
    assertEquals("The inherited cache declaration should work.", "abc", pPm.s.getValue());
    assertEquals("abc", pPm.sCached.getValue());
    assertEquals("abc", pPm.sCachedByClassSpec.getValue());
    assertEquals("The local cache declaration overrides the inherited one.", p.s, pPm.sClassCacheSwitchedOff.getValue());
    assertEquals("abc", pPm.sCachedButClearNever.getValue());

    PmCacheApi.clearPmCache(pPm);
    assertEquals(p.s, pPm.s.getValue());
    assertEquals(p.s, pPm.sCached.getValue());
    assertEquals(p.s, pPm.sCachedByClassSpec.getValue());
    assertEquals(p.s, pPm.sClassCacheSwitchedOff.getValue());
    assertEquals("The cache should not be cleared.", "abc", pPm.sCachedButClearNever.getValue());
  }

  @Test
  public void testParentDefinedValueCacheModeNotAppliedToChildren() {
    MyPojo p = new MyPojo();
    MyPojoPm pPm = new MyPojoPmWithParentDefinedValueCacheModeNotCascaded(new PmConversationImpl(), p);

    p.s = "abc";

    assertEquals(p.s, pPm.s.getValue());
    assertEquals(p.s, pPm.sCached.getValue());
    assertEquals(p.s, pPm.sCachedByClassSpec.getValue());
    assertEquals(p.s, pPm.sClassCacheSwitchedOff.getValue());
    assertEquals(p.s, pPm.sCachedButClearNever.getValue());

    p.s = "123";
    assertEquals("The inherited cache declaration should not be applied to the children.", p.s, pPm.s.getValue());
    assertEquals("abc", pPm.sCached.getValue());
    assertEquals("abc", pPm.sCachedByClassSpec.getValue());
    assertEquals("The local cache declaration overrides the inherited one.", p.s, pPm.sClassCacheSwitchedOff.getValue());
    assertEquals("abc", pPm.sCachedButClearNever.getValue());

    PmCacheApi.clearPmCache(pPm);
    assertEquals(p.s, pPm.s.getValue());
    assertEquals(p.s, pPm.sCached.getValue());
    assertEquals(p.s, pPm.sCachedByClassSpec.getValue());
    assertEquals(p.s, pPm.sClassCacheSwitchedOff.getValue());
    assertEquals("abc", pPm.sCachedButClearNever.getValue());
  }

  @Test
  public void testSetBackingValueClearsCacheOfAttr() {
    MyPojo p = new MyPojo();
    MyPojoPm pPm = new MyPojoPm(new PmConversationImpl(), p);

    p.s = "INITIAL";
    assertEquals("INITIAL", pPm.sCached.getValue());
    assertEquals("INITIAL", pPm.sCachedByClassSpec.getValue());

    PmAttrUtil.resetBackingValueToDefault(pPm.sCached);
    assertEquals(null, pPm.sCached.getValue());
    assertEquals(null, p.s);
    assertEquals("A set backing value call only resets the cache of the used attribute.",
                 "INITIAL", pPm.sCachedByClassSpec.getValue());
  }

  @Test
  public void testClearedOnValueChangeInHierarcy() {
    MyPojo p = new MyPojo();
    MyPojoPm pPm = new MyPojoPm(new PmConversationImpl(), p);
    PmInitApi.initPmTree(pPm);

    p.s = "INITIAL";
    assertEquals("INITIAL", pPm.s.getValue());
    assertEquals("INITIAL", pPm.sCachedAndClearedOnValueChangeInHierarcy.getValue());

    p.s = "BEAN_VALUE_CHANGED";
    assertEquals("INITIAL", pPm.sCachedAndClearedOnValueChangeInHierarcy.getValue());

    pPm.tab.stringInTab.setValue("changed");
    assertEquals("BEAN_VALUE_CHANGED", pPm.sCachedAndClearedOnValueChangeInHierarcy.getValue());
  }

  @Test
  public void testClearedOnValueChange() {
    MyPojo p = new MyPojo();
    MyPojoPm pPm = new MyPojoPm(new PmConversationImpl(), p);

    p.s = "INITIAL";
    assertEquals("INITIAL", pPm.s.getValue());
    assertEquals("INITIAL", pPm.sCachedAndClearedOnValueChange.getValue());

    // we can't track bean value changes only PM value changes
    p.s = "BEAN_VALUE_CHANGED";
    assertEquals("BEAN_VALUE_CHANGED", pPm.s.getValue());
    assertEquals("INITIAL", pPm.sCachedAndClearedOnValueChange.getValue());

    // change s and see if the cache is cleared
    pPm.s.setValue("PM_VALUE_CHANGED");
    assertEquals("PM_VALUE_CHANGED", pPm.s.getValue());
    assertEquals("PM_VALUE_CHANGED", pPm.sCachedAndClearedOnValueChange.getValue());

    // change s2 and see if the cache is cleared
    p.s = "BEAN_VALUE_CHANGED_AGAIN";
    pPm.s2.setValue("PM_VALUE_CHANGED");
    assertEquals("BEAN_VALUE_CHANGED_AGAIN", pPm.s.getValue());
    assertEquals("BEAN_VALUE_CHANGED_AGAIN", pPm.sCachedAndClearedOnValueChange.getValue());
  }

  @Test
  public void testMixedAnnotations() {
    try {
      PmInitApi.initPmTree(new MyPmWithMixedAnnotations(new PmConversationImpl()));
    } catch (PmRuntimeException e) {
      assertEquals(PmRuntimeException.class, e.getCause().getClass());
    }

    try {
      PmInitApi.initPmTree(new MyPmWithMixedAnnotations2(new PmConversationImpl()));
    } catch (PmRuntimeException e) {
      assertEquals(PmRuntimeException.class, e.getCause().getClass());
    }
  }

  // -- Domain model --

  public static class MyPojo {
    public String s;

    public String s2;

    // Has getters and setters to allow xPath access (used by valuePath annotation in MyPojoPm).
    public String getS() { return s; }
    public void setS(String s) { this.s = s; }
    public String getS2() { return s2; }
    public void setS2(String s2) { this.s2 = s2; }
  }

  // -- Presentation models --

  @PmBeanCfg(beanClass=MyPojo.class)
  public static class MyPojoPm extends PmBeanBase<MyPojo> {

    public final PmAttrString s = new PmAttrStringImpl(this);

    public final PmAttrString s2 = new PmAttrStringImpl(this);

    public final MyTab tab = new MyTab(this);

    @PmCacheCfg2(@Cache(property=CacheKind.ALL))
    @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sCached = new PmAttrStringImpl(this);

    @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sCachedWithInvalidationListener = new PmAttrStringImpl(this);

    @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sCachedByClassSpec = new MyCachedAttrClass(this);

    /** The attribute class wants to cache but the attribute declaration switches off. */
    @PmCacheCfg2(@Cache(property = CacheKind.ALL, mode=CacheMode.OFF))
    @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sClassCacheSwitchedOff = new MyCachedAttrClass(this);

    /** The attribute is cached but the cache is never cleared. */
    @PmCacheCfg2(@Cache(property = CacheKind.ALL, clear=Clear.NEVER))
    @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sCachedButClearNever = new MyCachedAttrClass(this);

    @PmCacheCfg2(@Cache(property = CacheKind.ALL, clearOn=@Observe(pm={"pmParent.s", "pmParent.s2"})))
    @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sCachedAndClearedOnValueChange = new PmAttrStringImpl(this);

    @PmCacheCfg2(@Cache(property = CacheKind.ALL, clearOn=@Observe(pm={"pmParent.tab"}, observePmTree=true)))
    @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sCachedAndClearedOnValueChangeInHierarcy = new PmAttrStringImpl(this);

    public MyPojoPm(PmObject pmParent, MyPojo myPojo) {
      super(pmParent, myPojo);
    }
  }

  @PmCacheCfg2(@Cache(property = CacheKind.ALL, cascade=true))
  public static class MyPojoPmWithParentDefinedValueCacheModeCascaded extends MyPojoPm {
    public MyPojoPmWithParentDefinedValueCacheModeCascaded(PmObject pmParent, MyPojo myPojo) {
      super(pmParent, myPojo);
    }
  }

  @PmCacheCfg2(@Cache(property = CacheKind.ALL))
  public static class MyPojoPmWithParentDefinedValueCacheModeNotCascaded extends MyPojoPm {
    public MyPojoPmWithParentDefinedValueCacheModeNotCascaded(PmObject pmParent, MyPojo myPojo) {
      super(pmParent, myPojo);
    }
  }

  @PmCacheCfg2(@Cache(property = CacheKind.ALL))
  public static class MyCachedAttrClass extends PmAttrStringImpl {
    public MyCachedAttrClass(PmElementBase pmParentBean) {
      super(pmParentBean);
    }
  };

  @PmCacheCfg2(@Cache(property = CacheKind.ALL))
  public static class MyPmWithMixedAnnotations extends PmBeanBase<MyPojo> {

    public MyPmWithMixedAnnotations(PmObject pmParent) {
      super(pmParent);
    }

    @PmCacheCfg(title=org.pm4j.core.pm.annotation.PmCacheCfg.CacheMode.OFF)
    public final PmAttrString s = new PmAttrStringImpl(this);
  }

  @PmCacheCfg(all=org.pm4j.core.pm.annotation.PmCacheCfg.CacheMode.ON)
  public static class MyPmWithMixedAnnotations2 extends PmBeanBase<MyPojo> {

    public MyPmWithMixedAnnotations2(PmObject pmParent) {
      super(pmParent);
    }

    @PmCacheCfg2(@Cache(property = CacheKind.TITLE, mode = CacheMode.OFF))
    public final PmAttrString s = new PmAttrStringImpl(this);
  }

  public static class MyTab extends PmObjectBase implements PmTab {
    public MyTab(PmObject pmParent) {
      super(pmParent);
    }

    public final PmAttrString stringInTab = new PmAttrStringImpl(this);
  }

}

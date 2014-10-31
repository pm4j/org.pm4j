package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg2;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Aspect;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Cache;
import org.pm4j.core.pm.annotation.PmCacheCfg2.CacheMode;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Clear;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmAttrUtil;
import org.pm4j.core.pm.impl.PmBeanImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementBase;

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

    p.s = "123";
    assertEquals(p.s, pPm.s.getValue());
    assertEquals("abc", pPm.sCached.getValue());
    assertEquals("abc", pPm.sCachedByClassSpec.getValue());
    assertEquals(p.s, pPm.sClassCacheSwitchedOff.getValue());
    // assertEquals(p.s, pPm.sCachedButClearNever.getValue()); TODO SDOLKE wrong assertion ?!?

    PmCacheApi.clearPmCache(pPm);
    assertEquals(p.s, pPm.s.getValue());
    assertEquals(p.s, pPm.sCached.getValue());
    assertEquals(p.s, pPm.sCachedByClassSpec.getValue());
    assertEquals(p.s, pPm.sClassCacheSwitchedOff.getValue());
    // assertEquals(p.s, pPm.sCachedButClearNever.getValue()); TODO SDOLKE wrong assertion ?!?
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
    // assertEquals(p.s, pPm.sCachedButClearNever.getValue()); TODO SDOLKE wrong assertion ?!?

    PmCacheApi.clearPmCache(pPm);
    assertEquals(p.s, pPm.s.getValue());
    assertEquals(p.s, pPm.sCached.getValue());
    assertEquals(p.s, pPm.sCachedByClassSpec.getValue());
    assertEquals(p.s, pPm.sClassCacheSwitchedOff.getValue());
    // assertEquals(p.s, pPm.sCachedButClearNever.getValue()); TODO SDOLKE wrong assertion ?!?
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

  // -- Domain model --

  public static class MyPojo {
    public String s;

    // Has getters and setters to allow xPath access (used by valuePath annotation in MyPojoPm).
    public String getS() { return s; }
    public void setS(String s) { this.s = s; }
  }

  // -- Presentation models --

  @PmBeanCfg(beanClass=MyPojo.class)
  public static class MyPojoPm extends PmBeanImpl<MyPojo> {
    
    public final PmAttrString s = new PmAttrStringImpl(this);

    @PmCacheCfg2(@Cache(aspect=Aspect.ALL))
    @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sCached = new PmAttrStringImpl(this);

    // Cache -> Cached, ClearOn or ClearedBy

//    @PmCacheCfg2(title=@Cached(clearedBy=@Clear(pm="s", change=ValueChangeKind.VALUE)))

//    /** Caches the result of {@link PmObject#isPmEnabled()} */
//    @PmCacheCfg2(enabled=@Cached)

//    /** Caches the result of {@link PmAttr#getValue()} */
//    @PmCacheCfg2()

//    /** Caches the result of {@link PmAttr#getValue()} */
//    @PmCacheCfg2(value=@Cached)

//    /**
//     * Caches the result of {@link PmAttr#getOptionSet()}.<br>
//     * The cache gets cleared if the value of the PM 's' gets changed.
//     */
//    @PmCacheCfg2(optionSet=@Cached(
//                 clearedBy=@Clear(pm="s", change=ValueChangeKind.VALUE)))

//    /**
//     * Caches the result of {@link PmAttr#getOptionSet()}.<br>
//     * The cache gets cleared if
//     * <ul>
//     *  <li>the value of the PM 's.x.y' gets changed or</li>
//     *  <li>the filter of the table 'someTable' gets changed.</li>
//     * </ul>
//     */
//    @PmCacheCfg2(
//        optionSet=@Cached(
//            clearedBy={
//                @Clear(pm={"s.x.y", "b"}),
//                @Clear(pm="someTable", change=ValueChangeKind.FILTER)
//        })
//    )

    @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sCachedWithInvalidationListener = new PmAttrStringImpl(this);

    @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sCachedByClassSpec = new MyCachedAttrClass(this);

    /** The attribute class wants to cache but the attribute declaration switches off. */
    @PmCacheCfg2(@Cache(aspect = Aspect.ALL, mode=CacheMode.OFF))
    @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sClassCacheSwitchedOff = new MyCachedAttrClass(this);

    /** The attribute is cached but the cache is never cleared. */
    @PmCacheCfg2(@Cache(aspect = Aspect.ALL, clear=Clear.NEVER))
    @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sCachedButClearNever = new MyCachedAttrClass(this);

    public MyPojoPm(PmObject pmParent, MyPojo myPojo) {
      super(pmParent, myPojo);
    }
  }

  @PmCacheCfg2(@Cache(aspect = Aspect.ALL, cascade=true))
  public static class MyPojoPmWithParentDefinedValueCacheModeCascaded extends MyPojoPm {
    public MyPojoPmWithParentDefinedValueCacheModeCascaded(PmObject pmParent, MyPojo myPojo) {
      super(pmParent, myPojo);
    }
  }

  @PmCacheCfg2(@Cache(aspect = Aspect.ALL))
  public static class MyPojoPmWithParentDefinedValueCacheModeNotCascaded extends MyPojoPm {
    public MyPojoPmWithParentDefinedValueCacheModeNotCascaded(PmObject pmParent, MyPojo myPojo) {
      super(pmParent, myPojo);
    }
  }

  @PmCacheCfg2(@Cache(aspect = Aspect.ALL))
  public static class MyCachedAttrClass extends PmAttrStringImpl {
    public MyCachedAttrClass(PmElementBase pmParentBean) {
      super(pmParentBean);
    }
  };

}

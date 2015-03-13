package org.pm4j.core.pm.impl;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmCacheCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg.CacheMode;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.tools.test._RecordingPmEventListener;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class PmBean2WithDeprCacheTest {

  private Bean bean = new Bean("InitialBean");
  
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void readBeanPmWithGetterLogic() {
    TestPmBase pm = new TestPmBase().provideTestBean(bean);
    assertSame(bean, pm.getPmBean());
    assertEquals(1, pm.callCount_getPmBeanImpl);
    assertSame(bean, pm.getPmBean());
    assertEquals(2, pm.callCount_getPmBeanImpl);
  }

  // TODO: check exception message using @Rule s
  @Test(expected=PmRuntimeException.class)
  public void assignBeanToUncachedPmBeanThrowsException() {
    TestPmBase pm = new TestPmBase();
    pm.setPmBean(new Bean("NewBean"));
  }

  @Test
  public void readBeanPmCached() {
    expectedException.expectMessage("@PmCacheCfg annotation is no longer supported. Please use @PmCacheCfg2");
    expectedException.expect(PmRuntimeException.class);
    
    TestPmBase pm = new TestPmCached().provideTestBean(bean);
    assertSame(bean, pm.getPmBean());
    assertEquals(1, pm.callCount_getPmBeanImpl);
    assertSame(bean, pm.getPmBean());
    assertEquals(1, pm.callCount_getPmBeanImpl);

    PmCacheApi.clearPmCache(pm, CacheKind.VALUE);
    assertSame(bean, pm.getPmBean());
    assertEquals(2, pm.callCount_getPmBeanImpl);
  }

  // TODO oboede: should we allow that without fixing the cache forever?
  @Test(expected=PmRuntimeException.class) @Ignore
  public void assignBeanToCachedPmBean() {
    TestPmBase pm = new TestPmCached();
    pm.setPmBean(new Bean("NewBean"));
  }

  @Test @Ignore
  public void readBeanPmCachedNeverCleared() {
    TestPmBase pm = new TestPmCachedNeverCleared().provideTestBean(bean);
    assertSame(bean, pm.getPmBean());
    assertEquals(1, pm.callCount_getPmBeanImpl);
    assertSame(bean, pm.getPmBean());
    assertEquals(1, pm.callCount_getPmBeanImpl);

    PmCacheApi.clearPmCache(pm, CacheKind.VALUE);
    assertSame(bean, pm.getPmBean());
    assertEquals(1, pm.callCount_getPmBeanImpl);
  }

  @Test
  public void assignBeanToFixCachedPmBean() {
    
    expectedException.expectMessage("@PmCacheCfg annotation is no longer supported. Please use @PmCacheCfg2");
    expectedException.expect(PmRuntimeException.class);
    TestPmBase pm = new TestPmCachedNeverCleared();
    pm.setPmBean(new Bean("NewBean"));
    assertEquals("NewBean", pm.getPmBean().s);
    assertEquals("NewBean", pm.s.getValue());
  }

  @Test
  public void eventPropagationOnDeferredSubPmBeanUncached() {
    expectedException.expectMessage("@PmCacheCfg annotation is no longer supported. Please use @PmCacheCfg2");
    expectedException.expect(PmRuntimeException.class);
    
    TestPm pm = new TestPm();

    pm.uncached.provideTestBean(bean);

    BroadcastPmEventProcessor.broadcastAllChangeEvent(pm, 0);
    assertEquals("No value change broadcast for unused, not initialized PMs.", 0, pm.valueChangeListener.getCallCount());
    assertEquals("No value change broadcast for unused, not initialized PMs.", 0, pm.uncached.valueChangeListener.getCallCount());
    assertEquals("No value change broadcast for unused, not initialized PMs.", 0, pm.uncached.sValueChangeListener.getCallCount());
    assertEquals("No getter called.", 0, pm.uncached.callCount_getPmBeanImpl);

    PmInitApi.initPmTree(pm);
    BroadcastPmEventProcessor.broadcastAllChangeEvent(pm, 0);
    assertEquals("Each sub-PM gets informed.", 1, pm.valueChangeListener.getCallCount());
    assertEquals("Each sub-PM gets informed.", 1, pm.uncached.valueChangeListener.getCallCount());
    assertEquals("Each sub-PM gets informed.", 1, pm.uncached.sValueChangeListener.getCallCount());
    assertEquals("No getter called.", 0, pm.uncached.callCount_getPmBeanImpl);

    pm.uncached.postponeEvents = true;
    pm.uncached.provideTestBean(null);
    BroadcastPmEventProcessor.broadcastAllChangeEvent(pm, PmEvent.VALUE_CHANGE_TO_NULL);
    assertEquals("A to-null change gets always propagated.", 2, pm.valueChangeListener.getCallCount());
    assertEquals("A to-null change gets always propagated.", 2, pm.uncached.valueChangeListener.getCallCount());
    assertEquals("A to-null change gets always propagated.", 2, pm.uncached.sValueChangeListener.getCallCount());
    assertEquals("No getter called.", 0, pm.uncached.callCount_getPmBeanImpl);

    BroadcastPmEventProcessor.broadcastAllChangeEvent(pm, 0);
    assertEquals("Main PM has is not deferred. Event immediately fired.", 3, pm.valueChangeListener.getCallCount());
    assertEquals("Broadcast will be deferred till next getPmBean() call.", 2, pm.uncached.valueChangeListener.getCallCount());
    assertEquals("Broadcast will be deferred till next getPmBean() call.", 2, pm.uncached.sValueChangeListener.getCallCount());
    assertEquals("No getter called.", 0, pm.uncached.callCount_getPmBeanImpl);

    assertNull(pm.uncached.getPmBean());
    assertEquals("Main PM listener count stays as it is.", 3, pm.valueChangeListener.getCallCount());
    assertEquals("Deferred event propagated in getPmBean() call.", 3, pm.uncached.valueChangeListener.getCallCount());
    assertEquals("Deferred event propagated in getPmBean() call.", 3, pm.uncached.sValueChangeListener.getCallCount());
    assertEquals("Main PM unchanged if a sub-PM fires deferred events.", 3, pm.valueChangeListener.getCallCount());
    assertEquals("Getter called.", 1, pm.uncached.callCount_getPmBeanImpl);
  }

  @Test
  public void eventPropagationOnDeferredMainPmBean() {
    TestPmBase pm = new TestPmBase().provideTestBean(bean);

    BroadcastPmEventProcessor.broadcastAllChangeEvent(pm, 0);
    assertEquals("No value change broadcast for unused, not initialized PMs.", 0, pm.valueChangeListener.getCallCount());

    PmInitApi.initPmTree(pm);
    BroadcastPmEventProcessor.broadcastAllChangeEvent(pm, 0);
    assertEquals("Initialized PMs get informed.", 1, pm.valueChangeListener.getCallCount());

    pm.postponeEvents = true;
    pm.provideTestBean(null);
    BroadcastPmEventProcessor.broadcastAllChangeEvent(pm, PmEvent.VALUE_CHANGE_TO_NULL);
    assertEquals("A to-null change gets always propagated.", 2, pm.valueChangeListener.getCallCount());

    BroadcastPmEventProcessor.broadcastAllChangeEvent(pm, 0);
    assertEquals("Events are deferred.", 2, pm.valueChangeListener.getCallCount());
    assertNull(pm.getPmBean());
    assertEquals("Deferred events are fired on getPmBean()", 3, pm.valueChangeListener.getCallCount());
    pm.provideTestBean(bean);
    assertSame(bean, pm.getPmBean());
    assertEquals("A getter that provides another bean value does not automatically lead to a value change event.",
                 3, pm.valueChangeListener.getCallCount());
  }

  // -- Test infrastructure --

  public static class Bean {
    public String s;

    public Bean() { this(null); }
    public Bean(String s) { this.s = s; }
  }

  public static class TestPmBase extends PmBeanImpl2<Bean>{
    public final PmAttrString s = new PmAttrStringImpl(this);

    // test instrumentation:
    int callCount_getPmBeanImpl = 0;
    Bean b;
    boolean postponeEvents;
    _RecordingPmEventListener valueChangeListener = new _RecordingPmEventListener();
    _RecordingPmEventListener sValueChangeListener = new _RecordingPmEventListener();


    public TestPmBase() {
      this(new PmConversationImpl());
    }

    public TestPmBase(PmObject parentPm) {
      super(parentPm);
      PmEventApi.addPmEventListener(this, PmEvent.VALUE_CHANGE, valueChangeListener);
      PmEventApi.addPmEventListener(s, PmEvent.VALUE_CHANGE, sValueChangeListener);
    }

    @Override
    protected Bean getPmBeanImpl() {
      ++callCount_getPmBeanImpl;
      return b;
    }

    @Override
    protected boolean hasDeferredPmEventHandling() {
      return postponeEvents;
    }

    public TestPmBase provideTestBean(Bean b) {
      this.b = b;
      return this;
    }

  }

  public static class TestPm extends TestPmBase {
    public final TestPmBase uncached = new TestPmBase(this);

    @PmCacheCfg(value=CacheMode.ON)
    public final TestPmBase cached = new TestPmBase(this);

    @PmCacheCfg(value=CacheMode.ON, clear=PmCacheCfg.Clear.NEVER)
    public final TestPmBase cachedClearNever = new TestPmBase(this);

  }

  @PmCacheCfg(value=CacheMode.ON)
  public static class TestPmCached extends TestPmBase {
  }

  @PmCacheCfg(value=CacheMode.ON, clear=PmCacheCfg.Clear.NEVER)
  public static class TestPmCachedNeverCleared extends TestPmBase {
  }

}

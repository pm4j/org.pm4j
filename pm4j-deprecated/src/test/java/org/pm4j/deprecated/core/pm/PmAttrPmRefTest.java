package org.pm4j.deprecated.core.pm;

import java.util.Collection;
import java.util.Map;

import junit.framework.TestCase;

import org.pm4j.common.converter.string.StringConverter;
import org.pm4j.common.converter.string.StringConverterCtxt;
import org.pm4j.common.converter.string.StringConverterParseException;
import org.pm4j.common.util.collection.MapUtil;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.deprecated.core.pm.impl.DeprPmAttrPmRefImpl;

public class PmAttrPmRefTest extends TestCase {

  // -- Domain model --

  public static class A {
    public B refToB;
    public String s;
  }

  public static class B {
    public Integer i;
    public String s;

    public B(int i, String s) {
      this.i = i;
      this.s = s;
    }

    @Override
    public String toString() {
      return "" + i;
    }
  }

  // -- Service layer --

  public static class MyServiceLayer {

    private Map<Integer, B> dataMap = MapUtil.makeLinkedMap(
        1, new B(1, "one"),
        2, new B(2, "two"));

    public Collection<B> getAllBs() {
      return dataMap.values();
    }
  }

  // -- Presentation model --

  /**
   * Don't forget to specify a key attribute when the PM will be used in
   * option lists.<br>
   * This specification isn't required only when the default key attribute
   * name 'id' is used within the domain class.
   */
  @PmBeanCfg(beanClass=B.class, key="i")
  public static class BPm extends PmBeanBase<B> {
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);
    public final PmAttrString s = new PmAttrStringImpl(this);
  }

  @PmBeanCfg(beanClass=A.class)
  public static class APm extends PmBeanBase<A> {

    @PmFactoryCfg(beanPmClasses=BPm.class)

    public final DeprPmAttrPmRef<BPm> refToB = new DeprPmAttrPmRefImpl<BPm, B>(this) {
      @Override
      @PmOptionCfg(id="i", title="s", value="null", backingValue="this", nullOption=NullOption.NO)
      public Iterable<?> getOptionValues() {
        return myServiceLayer.getAllBs();
      }

      // FIXME oboede: the default converter does not work in this case.
      protected org.pm4j.common.converter.string.StringConverter<BPm> getStringConverterImpl() {
        return new StringConverter<PmAttrPmRefTest.BPm>() {
          @Override
          public String valueToString(StringConverterCtxt ctxt, BPm v) {
            return v.getPmBean().toString();
          }

          @Override
          public BPm stringToValue(StringConverterCtxt ctxt, String s) throws StringConverterParseException {
            PmOption o = getOptionSet().findOptionForIdString(s);
            return o != null ? (BPm)(Object)PmFactoryApi.getPmForBean(refToB, o.getBackingValue()) : null;
          }
        };
      };

      @PmInject private MyServiceLayer myServiceLayer;
    };
  }

  // -- Tests --

  public void testRef() {
    PmConversation session = new PmConversationImpl(APm.class);
    session.setPmNamedObject("myServiceLayer", new MyServiceLayer());

    A a = new A();
    APm aPm = PmFactoryApi.getPmForBean(session, a);

    assertNull(aPm.refToB.getValue());
    assertEquals("An extra null option because the value is null.", "[, one, two]", aPm.refToB.getOptionSet().getOptions().toString());

    aPm.refToB.setValueAsString("1");
    assertEquals("1", aPm.refToB.getValueAsString());
    assertEquals("one", aPm.refToB.getValue().s.getValue());
    assertEquals("one", a.refToB.s);

  }
}

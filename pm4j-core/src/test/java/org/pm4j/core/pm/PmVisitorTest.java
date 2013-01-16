package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.api.PmVisitorApi.VisitCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.DefaultVisitCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.VisitHint;
import org.pm4j.core.pm.api.PmVisitorApi.VisitResult;
import org.pm4j.core.pm.impl.PmAttrPmListImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.pm.impl.PmLabelImpl;

public class PmVisitorTest {

  private final MyRootPm pm = new MyRootPm(new PmConversationImpl());

  
  private final VisitCallBack visitAllCallBack = new DefaultVisitCallBack() {
    @Override
    public VisitResult visit(PmObject pm) {
      storeVisit(pm);
      return VisitResult.CONTINUE;
    }

  };

  private final List<String> calls = new ArrayList<String>();

  private void storeVisit(PmObject pm) {
    calls.add(pm.getPmRelativeName());
  }

  private final VisitCallBack visitAllStrings = new DefaultVisitCallBack() {
    @Override
    public VisitResult visit(PmObject pm) {
      if (pm instanceof PmAttrString) {
        storeVisit(pm);
      }
      return VisitResult.CONTINUE;
    }
  };

  private final VisitCallBack visitChildByRelativeName = new DefaultVisitCallBack() {
    @Override
    public VisitResult visit(PmObject pm) {

      if ("myChPm1".equals(pm.getPmRelativeName())) {
        storeVisit(pm);
        return VisitResult.STOP_VISIT;
      }
      return VisitResult.CONTINUE;
    }
  };

  private final VisitCallBack visitSkipChildren = new DefaultVisitCallBack() {
    @Override
    public VisitResult visit(PmObject pm) {

      if (pm.getPmRelativeName().contains("myChPm")) {
        storeVisit(pm);
        return VisitResult.SKIP_CHILDREN;
      }
      return VisitResult.CONTINUE;
    }
  };

  @Before
  public void beforeTest() {
    calls.clear();
  }

  @Test
  public void testSkipFactoryPms() {
    PmObject stopObject = PmVisitorApi.visit(pm, visitAllCallBack, VisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS);
    String expected = "[pmVisitorTest_MyRootPm, myChPm1, myChPm1_disabled, myChPm1_convImpl, myChPm1_readOnly, myChPm1_invisible, myChPm2, myChPm2_disabled, myChPm2_convImpl, myChPm2_readOnly, myChPm2_invisible, myPmList]";
    assertEquals(expected, calls.toString());
    assertEquals(null, stopObject);
  }

  @Test
  public void testSkipReadOnly() {
    PmObject stopObject = PmVisitorApi.visit(pm, visitAllCallBack, VisitHint.SKIP_READ_ONLY,
        VisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS);
    String expected = "[pmVisitorTest_MyRootPm, myChPm1, myChPm1_disabled, myChPm1_convImpl, myChPm1_invisible, myChPm2, myChPm2_disabled, myChPm2_convImpl, myChPm2_invisible, myPmList]";
    assertEquals(expected, calls.toString());
    assertEquals(null, stopObject);
  }

  @Test
  public void testSkipInvisible() {
    PmObject stopObject = PmVisitorApi.visit(pm, visitAllCallBack, VisitHint.SKIP_INVISIBLE,
        VisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS);
    String expected = "[pmVisitorTest_MyRootPm, myChPm1, myChPm1_disabled, myChPm1_convImpl, myChPm1_readOnly, myChPm2, myChPm2_disabled, myChPm2_convImpl, myChPm2_readOnly, myPmList]";
    assertEquals(expected, calls.toString());
    assertEquals(null, stopObject);
  }

  @Test
  public void testSkipDisabled() {
    PmObject stopObject = PmVisitorApi.visit(pm, visitAllCallBack, VisitHint.SKIP_DISABLED,
        VisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS);
    String expected = "[pmVisitorTest_MyRootPm, myChPm1, myChPm1_convImpl, myChPm1_readOnly, myChPm1_invisible, myChPm2, myChPm2_convImpl, myChPm2_readOnly, myChPm2_invisible, myPmList]";
    assertEquals(expected, calls.toString());
    assertEquals(null, stopObject);
  }

  @Test
  public void testSkipConversation() {
    PmObject stopObject = PmVisitorApi.visit(pm, visitAllCallBack, VisitHint.SKIP_CONVERSATION,
        VisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS);
    String expected = "[pmVisitorTest_MyRootPm, myChPm1, myChPm1_disabled, myChPm1_readOnly, myChPm1_invisible, myChPm2, myChPm2_disabled, myChPm2_readOnly, myChPm2_invisible, myPmList]";
    assertEquals(expected, calls.toString());
    assertEquals(null, stopObject);
  }

  @Test
  public void testVisitSameType() {
    PmObject stopObject = PmVisitorApi.visit(pm, visitAllStrings, VisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS);
    String expected = "[myChPm1_invisible, myChPm2_invisible]";
    assertEquals(expected, calls.toString());
    assertEquals(null, stopObject);
  }

  @Test
  public void testFilterOnePm() {
    PmObject stopObject = PmVisitorApi.visit(pm, visitChildByRelativeName, VisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS);
    assertEquals("[myChPm1]", calls.toString());
    assertEquals(pm.myChPm1, stopObject);
  }

  @Test
  public void testSkipChildren() {
    PmObject stopObject = PmVisitorApi.visit(pm, visitSkipChildren);
    assertEquals("[myChPm1, myChPm2]", calls.toString());
    assertEquals(null, stopObject);
  }
  
  @Test
  public void testConstructor() {
    PmObject stopObject = PmVisitorApi.visit(pm, visitSkipChildren);
    assertEquals("[myChPm1, myChPm2]", calls.toString());
    assertEquals(null, stopObject);
  }

  @Test(expected = AssertionError.class)
  public void testNullSafePm() {
    PmVisitorApi.visit(null, visitSkipChildren, VisitHint.SKIP_DISABLED);
  }

  @Test(expected = AssertionError.class)
  public void testNullSafeCallBack() {
    PmVisitorApi.visit(pm, null, VisitHint.SKIP_DISABLED);
  }

  @Test(expected = AssertionError.class)
  public void testNullSafeVisitHint() {
    PmVisitorApi.visit(pm, visitSkipChildren, (VisitHint[]) null);
  }

  @Test
  public void testGeneratedChildren() {
    PmObject stopObject = PmVisitorApi.visit(pm, visitAllCallBack);
    String expected = "[pmVisitorTest_MyRootPm, myChPm1, myChPm1_disabled, myChPm1_convImpl, myChPm1_readOnly, myChPm1_invisible, myChPm2, myChPm2_disabled, myChPm2_convImpl, myChPm2_readOnly, myChPm2_invisible, myPmList, pmVisitorTest_MyBeanPm, s, pmVisitorTest_MyBeanPm, s]";
    assertEquals(expected, calls.toString());
    assertEquals(null, stopObject);
  }

  public static class MyRootPm extends PmElementImpl {
    public final MyPmChild myChPm1 = new MyPmChild(this);
    public final MyPmChild myChPm2 = new MyPmChild(this);
    @PmFactoryCfg(beanPmClasses = MyBeanPm.class)
    public final PmAttrPmList<MyBeanPm> myPmList = new PmAttrPmListImpl<MyBeanPm, MyBean>(this) {
      protected java.util.Collection<MyBean> getBackingValueImpl() {
        return Arrays.asList(new MyBean("hello"), new MyBean("world"));
      }
    };

    public MyRootPm(PmObject pmParent) {
      super(pmParent);
      this.myPmList.getValue();
    }
  }

  public static class MyPmChild extends PmElementImpl {
    public final PmLabel disabled = new PmLabelImpl(this) {
      public boolean isPmEnabled() {
        return false;
      };
    };
    public final PmConversation convImpl = new PmConversationImpl(this);
    public final PmLabel readOnly = new PmLabelImpl(this) {
      protected boolean isPmReadonlyImpl() {
        return true;
      };
    };

    public final PmAttrString invisible = new PmAttrStringImpl(this) {
      protected boolean isPmVisibleImpl() {
        return false;
      };
    };

    public MyPmChild(PmObject pmParent) {
      super(pmParent);
    }
  }

  public static class MyBean {
    public final String s;

    public MyBean(String s) {
      this.s = s;
    }
  }

  @PmBeanCfg(beanClass = MyBean.class)
  public static class MyBeanPm extends PmBeanBase<MyBean> {
    public final PmAttrString s = new PmAttrStringImpl(this);
  }
}

package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHint;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitResult;
import org.pm4j.core.pm.impl.PmAttrPmListImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.pm.impl.PmLabelImpl;

public class PmVisitorTest {

  private final MyRootPm pm = new MyRootPm(new PmConversationImpl());


  private final PmVisitCallBack visitAllCallBack = new PmVisitCallBack() {
    @Override
    public PmVisitResult visit(PmObject pm) {
      storeVisit(pm);
      return PmVisitResult.CONTINUE;
    }

  };

  private final List<String> calls = new ArrayList<String>();

  private void storeVisit(PmObject pm) {
    calls.add(pm.getPmRelativeName());
  }

  private final PmVisitCallBack visitAllStrings = new PmVisitCallBack() {
    @Override
    public PmVisitResult visit(PmObject pm) {
      if (pm instanceof PmAttrString) {
        storeVisit(pm);
      }
      return PmVisitResult.CONTINUE;
    }
  };

  private final PmVisitCallBack visitChildByRelativeName = new PmVisitCallBack() {
    @Override
    public PmVisitResult visit(PmObject pm) {

      if ("myChPm1".equals(pm.getPmRelativeName())) {
        storeVisit(pm);
        return PmVisitResult.STOP_VISIT;
      }
      return PmVisitResult.CONTINUE;
    }
  };

  private final PmVisitCallBack visitSkipChildren = new PmVisitCallBack() {
    @Override
    public PmVisitResult visit(PmObject pm) {

      if (pm.getPmRelativeName().contains("myChPm")) {
        storeVisit(pm);
        return PmVisitResult.SKIP_CHILDREN;
      }
      return PmVisitResult.CONTINUE;
    }
  };

  @Before
  public void beforeTest() {
    calls.clear();
  }

  @Test
  public void testSkipFactoryPms() {
    PmVisitorApi.visit(pm, visitAllCallBack, PmVisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS);
    String expected = "[pmVisitorTest_MyRootPm, myChPm1, myChPm1_disabled, myChPm1_convImpl, myChPm1_readOnly, myChPm1_invisible, myChPm2, myChPm2_disabled, myChPm2_convImpl, myChPm2_readOnly, myChPm2_invisible, myPmList]";
    assertEquals(expected, calls.toString());
  }

  @Test
  public void testSkipReadOnly() {
    PmVisitorApi.visit(pm, visitAllCallBack, PmVisitHint.SKIP_READ_ONLY,
        PmVisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS);
    String expected = "[pmVisitorTest_MyRootPm, myChPm1, myChPm1_disabled, myChPm1_convImpl, myChPm1_invisible, myChPm2, myChPm2_disabled, myChPm2_convImpl, myChPm2_invisible, myPmList]";
    assertEquals(expected, calls.toString());
  }

  @Test
  public void testSkipInvisible() {
    PmVisitorApi.visit(pm, visitAllCallBack, PmVisitHint.SKIP_INVISIBLE,
        PmVisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS);
    String expected = "[pmVisitorTest_MyRootPm, myChPm1, myChPm1_disabled, myChPm1_convImpl, myChPm1_readOnly, myChPm2, myChPm2_disabled, myChPm2_convImpl, myChPm2_readOnly, myPmList]";
    assertEquals(expected, calls.toString());
  }

  @Test
  public void testSkipDisabled() {
    PmVisitorApi.visit(pm, visitAllCallBack, PmVisitHint.SKIP_DISABLED,
        PmVisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS);
    String expected = "[pmVisitorTest_MyRootPm, myChPm1, myChPm1_convImpl, myChPm1_readOnly, myChPm1_invisible, myChPm2, myChPm2_convImpl, myChPm2_readOnly, myChPm2_invisible, myPmList]";
    assertEquals(expected, calls.toString());
  }

  @Test
  public void testSkipConversation() {
    PmVisitorApi.visit(pm, visitAllCallBack, PmVisitHint.SKIP_CONVERSATION,
        PmVisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS);
    String expected = "[pmVisitorTest_MyRootPm, myChPm1, myChPm1_disabled, myChPm1_readOnly, myChPm1_invisible, myChPm2, myChPm2_disabled, myChPm2_readOnly, myChPm2_invisible, myPmList]";
    assertEquals(expected, calls.toString());
  }

  @Test
  public void testVisitSameType() {
    PmVisitorApi.visit(pm, visitAllStrings, PmVisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS);
    String expected = "[myChPm1_invisible, myChPm2_invisible]";
    assertEquals(expected, calls.toString());
  }

  @Test
  public void testFilterOnePm() {
    PmVisitorApi.visit(pm, visitChildByRelativeName, PmVisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS);
    assertEquals("[myChPm1]", calls.toString());
  }

  @Test
  public void testSkipChildren() {
    PmVisitorApi.visit(pm, visitSkipChildren);
    assertEquals("[myChPm1, myChPm2]", calls.toString());
  }

  @Test
  public void testConstructor() {
    PmVisitorApi.visit(pm, visitSkipChildren);
    assertEquals("[myChPm1, myChPm2]", calls.toString());
  }

  @Test
  public void testGeneratedChildren() {
    PmVisitorApi.visit(pm, visitAllCallBack);
    String expected = "[pmVisitorTest_MyRootPm, myChPm1, myChPm1_disabled, myChPm1_convImpl, myChPm1_readOnly, myChPm1_invisible, myChPm2, myChPm2_disabled, myChPm2_convImpl, myChPm2_readOnly, myChPm2_invisible, myPmList, pmVisitorTest_MyBeanPm, s, pmVisitorTest_MyBeanPm, s]";
    assertEquals(expected, calls.toString());
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

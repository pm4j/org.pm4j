package org.pm4j.core.xml.visibleState.beans;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmObject.PmMatcher;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmAttrBooleanImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl;
import org.pm4j.core.pm.impl.PmTableImpl;
import org.pm4j.core.xml.visibleState.VisibleStateAspectMatcher;
import org.pm4j.core.xml.visibleState.VisibleStateUtil;

public class VisibleStateUtilTest {

  private TestPm testPm = new TestPm();

  @Test
  public void enableTest() {
    XmlPmObjectBase xpm = toXmlObject(testPm);
    XmlPmObjectBase xBoolAttr = xpm.children.get(0);

    assertEquals(null, xpm.enabled);
    assertEquals(null, xBoolAttr.enabled);

  }

  private XmlPmObjectBase toXmlObject(PmObject pm) {
    return VisibleStateUtil.toXmlObject(testPm, Collections.<PmMatcher> emptyList(), Collections.<VisibleStateAspectMatcher> emptyList());
  }

  @PmTitleCfg(title = "Test PM")
  public static class TestPm extends PmConversationImpl {
    @PmTitleCfg(title = "Boolean Attr", tooltip="A simple boolean attribute.")
    @PmAttrCfg(defaultValue="false")
    public final PmAttrBoolean boolAttr = new PmAttrBooleanImpl(this) {
      @Override protected boolean isPmEnabledImpl() {
        return enableBoolAttr;
      }
      @Override protected boolean isPmReadonlyImpl() {
        return readOnlyBoolAttr;
      }
    };

    @PmTitleCfg(title = "Required Attr")
    @PmAttrCfg(required=true)
    public final PmAttrString requiredAttr = new PmAttrStringImpl(this);

    @PmTitleCfg(title = "Readonly Attr")
    @PmAttrCfg(readOnly=true)
    public final PmAttrString readOnlyAttr = new PmAttrStringImpl(this);

    @PmTitleCfg(title="Do something")
    public final PmCommand cmdDoSomething = new PmCommandImpl(this);

    @PmTitleCfg(title="Table")
    public final TestTablePm table = new TestTablePm(this);

    boolean enable = true;
    boolean readOnly;
    boolean enableBoolAttr = true;
    boolean readOnlyBoolAttr;

    public TestPm() {
      super(Locale.ENGLISH);
    }

    @Override
    protected boolean isPmEnabledImpl() {
      return enable;
    }

    @Override
    protected boolean isPmReadonlyImpl() {
      return readOnly;
    }
  }

  @PmTitleCfg(title="Row")
  @PmBeanCfg(beanClass=TestBean.class)
  public static class TestRowPm extends PmBeanBase<TestBean> {
    @PmTitleCfg(title = "Name")
    public final PmAttrString name = new PmAttrStringImpl(this);
  }

  @PmFactoryCfg(beanPmClasses=TestRowPm.class)
  static class TestTablePm extends PmTableImpl<TestRowPm, TestBean> {
    @PmTitleCfg(title = "Name")
    public final PmTableCol name = new PmTableColImpl(this);

    private List<TestBean> testBeans = new ArrayList<TestBean>(Arrays.asList(
                                new TestBean("Hello"), new TestBean("World")));

    public TestTablePm(PmObject pmParent) {
        super(pmParent);
    }

    @Override
    protected Collection<TestBean> getPmBeansImpl() {
      return testBeans;
    }
  }

  static class TestBean {
    public String name;

    public TestBean(String name) {
        this.name = name;
    }
  }

}

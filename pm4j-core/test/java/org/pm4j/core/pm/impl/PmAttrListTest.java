package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.pm4j.core.pm.PmAttrList;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.options.PmOptionImpl;
import org.pm4j.core.pm.impl.options.PmOptionSetImpl;

public class PmAttrListTest extends TestCase {
  enum MyEnum {
    e1, e2
  }
  public static class MyBean {
    private List<String> stringList = new ArrayList<String>();
    private List<MyEnum> enumList = new ArrayList<MyEnum>();

    public List<String> getStringList() {
      return stringList;
    }

    public void setStringList(List<String> stringList) {
      this.stringList = stringList;
    }

    private static final List<String> options = Arrays.asList(new String[]{ "a", "b", "c" });
    public List<String> getStringListOptions() {
      return options;
    }

    public List<MyEnum> getEnumList() {
      return enumList;
    }

    public void setEnumList(List<MyEnum> enumList) {
      this.enumList = enumList;
    }
  }

  @PmBeanCfg(beanClass=MyBean.class)
  public static class MyPm extends PmBeanBase<MyBean> {

    public final PmAttrList<String> stringList = new PmAttrListImpl<String>(this) {
      @Override
      protected PmOptionSet getOptionSetImpl() {
        return new PmOptionSetImpl(
            new PmOptionImpl("a", "A"),
            new PmOptionImpl("b", "B"),
            new PmOptionImpl("c", "C"));
      }
    };

    public final PmAttrList<MyEnum> enumList = new PmAttrListImpl<MyEnum>(this) {
      @Override
      protected PmOptionSet getOptionSetImpl() {
        return new PmOptionSetImpl(
            new PmOptionImpl(MyEnum.e1.name(), "-e1-", MyEnum.e1),
            new PmOptionImpl(MyEnum.e2.name(), "-e2-", MyEnum.e2));
      }
    };
}

  public void testStringList() {
    PmConversation session = new PmConversationImpl(MyPm.class);
    MyBean myBean = new MyBean();
    MyPm myPm = PmFactoryApi.getPmForBean(session, myBean);

    System.out.println("listvalue: " + myPm.stringList.getValue());
    assertEquals("[A, B, C]", myPm.stringList.getOptionSet().getOptions().toString());

    myPm.stringList.setValue(Arrays.asList(new String[]{ "a", "b", "c" }));
    assertEquals("[a, b, c]", myBean.stringList.toString());

    assertEquals("[-e1-, -e2-]", myPm.enumList.getOptionSet().getOptions().toString());

    assertEquals("[a, b]", myPm.stringList.getValueSubset(0, 2).toString());
    assertEquals("[b, c]", myPm.stringList.getValueSubset(1, 2).toString());

    assertTrue(List.class.isAssignableFrom(myPm.enumList.getValue().getClass()));
  }
}

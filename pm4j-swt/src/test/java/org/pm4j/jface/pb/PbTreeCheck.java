package org.pm4j.jface.pb;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.PmAttrPmListImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.swt.pb.PbTree;
import org.pm4j.swt.testtools.SwtTestShell;

public class PbTreeCheck {

  /** The demo bean that will be presented by the PM to the tree. */
  public static class MyBean {
    public String name;
    public List<MyBean> list1 = new ArrayList<MyBean>();
    public List<MyBean> list2 = new ArrayList<MyBean>();
    public MyBean(String name, MyBean... children) {
      this.name = name;
      for (MyBean b : children) list1.add(b);
    }
  }

  /** The PM that represents the {@link MyBean} content tree. */
  @PmBeanCfg(beanClass=MyBean.class)
  @PmTitleCfg(attrValue="name")
  public static class MyBeanPm extends PmBeanBase<MyBean> {

    public final PmAttrString name = new PmAttrStringImpl(this);
    public final PmAttrPmList<MyBeanPm> list1 = new PmAttrPmListImpl<MyBeanPm, MyBean>(this);
    public final PmAttrPmList<MyBeanPm> list2 = new PmAttrPmListImpl<MyBeanPm, MyBean>(this);

    /**
     * This overrides the default node child behavior:
     * The children of {@link #list1} will be visualized as immediate children
     * of this PM node.
     */
    @Override
    protected List<? extends PmObject> getPmChildNodesImpl() {
      return list1.getValue();
    }
  }


  public static void main(String[] args) {
    SwtTestShell s = new SwtTestShell(500, 350, "jFace Tree Binding Demo");
    new PbTree().build(s.getShell(), makeMyBeanPm(MyBeanPm.class));
    s.show();
  }



  private static MyBeanPm makeMyBeanPm(Class<?> pmClass) {
    MyBean rootBean = new MyBean("root",
        new MyBean("parent1", new MyBean("child1.1"), new MyBean("child1.2")),
        new MyBean("parent2", new MyBean("child2.1"), new MyBean("child2.2"))
    );

    PmConversation session = new PmConversationImpl(pmClass);
    return PmFactoryApi.getPmForBean(session, rootBean);
  }
}

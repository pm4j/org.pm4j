package org.pm4j.demo.user;

import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.demo.user.UserBean.Gender;

@PmBeanCfg(beanClass=UserBean.class)
public class UserEditPm extends PmBeanBase<UserBean> {

  public final PmAttrString firstName = new PmAttrStringImpl(this);
  public final PmAttrString lastName = new PmAttrStringImpl(this);
  public final PmAttrEnum<Gender> gender = new PmAttrEnumImpl<Gender>(this, Gender.class);

}

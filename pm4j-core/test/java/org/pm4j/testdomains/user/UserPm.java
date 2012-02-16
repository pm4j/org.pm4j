package org.pm4j.testdomains.user;

import static org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO.CLEAR;

import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmAttrList;
import org.pm4j.core.pm.PmAttrPmRef;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmAttrListImpl;
import org.pm4j.core.pm.impl.PmAttrPmRefImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmCommandImpl;

@PmTitleCfg(attrValue="name")
@PmBeanCfg(beanClass=User.class)
public class UserPm extends PmBeanBase<User> implements PmBean<User> {

  // ------- Attributes ------- //

  @PmAttrCfg(required=true, minLen=2, maxLen=10)
  public final PmAttrString     name = new PmAttrStringImpl(this);

  /**
   * All enumeration values are automatically use as options for this enum attribute.
   */
  @PmOptionCfg(nullOption=NullOption.NO)
  public final PmAttrEnum<User.Salutation>
                                salutation = new PmAttrEnumImpl<User.Salutation>(this, User.Salutation.class);

  /**
   * A read only attribute with some java code that generates specific content.
   */
  @PmAttrCfg(readOnly=true)
  public final PmAttrString     fullName = new PmAttrStringImpl(this) {
    @Override
    protected String getBackingValueImpl() {
      if (salutation.getValue() != null)
        return salutation.getValueLocalized() + ' ' + name.getValue();
      else
        return name.getValue();
    }
  };

  @PmAttrCfg(maxLen=1000)
  public final PmAttrString     description = new PmAttrStringImpl(this);

  /**
   * All users of the domain will be presentend as option values.
   */
  @PmOptionCfg(values="pmBean.domain.users", id="name", title="name", nullOption=NullOption.NO)
  public final PmAttrPmRef<UserPm> associate = new PmAttrPmRefImpl<UserPm, User>(this);

  public final PmAttrList<String> languages = new PmAttrListImpl<String>(this);

  public final PmCommand cmdEdit = new PmCommandImpl(this);

  //@PmCommandCfg(beforeDo=BEFORE_DO.VALIDATE)
  public final PmCommand cmdCommitChanges = new PmCommandImpl(this) {
    @Override protected void doItImpl() {
      commitBufferedPmChanges();
    }
  };

  @PmCommandCfg(beforeDo=CLEAR)
  public final PmCommand cmdClear = new PmCommandImpl(this) {
    @Override protected void doItImpl() {
      resetPmValues();
    }
  };

}

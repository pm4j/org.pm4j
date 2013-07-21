package org.pm4j.testdomains.user;

import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.PmAttrPmListImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmCommandImpl;

@PmTitleCfg(attrValue="name")
@PmBeanCfg(beanClass=Domain.class)
public class DomainPm extends PmBeanBase<Domain> {

  // ------- Attributes ------- //

  @PmAttrCfg(required=true, minLen=1, maxLen=10)
  public final PmAttrString name = new PmAttrStringImpl(this);

  @PmAttrCfg(maxLen=1000)
  public final PmAttrString description = new PmAttrStringImpl(this);

  @PmFactoryCfg(beanPmClasses=UserPm.class)
  public final PmAttrPmList<UserPm> users = new PmAttrPmListImpl<UserPm, User>(this);

  @PmFactoryCfg(beanPmClasses=DomainPm.class)
  public final PmAttrPmList<DomainPm> subDomains = new PmAttrPmListImpl<DomainPm, Domain>(this);

  // ------- Commands ---------- //

  public final PmCommand cmdAddUser = new PmCommandImpl(this) {
    @Override protected void doItImpl() {
      UserPm userPm = PmFactoryApi.getPmForBean(this, new User());
      users.add(userPm);
    }
  };
}

package org.pm4j.core.sample.admin.user;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg.CacheMode;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.sample.admin.user.service.Address;
import org.pm4j.core.sample.admin.user.service.City;

@PmBeanCfg(beanClass=Address.class)
public class AddressPm extends PmBeanBase<Address> {

  @PmOptionCfg(values="userService.getCityList()", id="zipCode", title="name")
  @PmAttrCfg(valuePath="pmBean.address.city")
  public final PmAttr<City> city   = new PmAttrImpl<City>(this);

  @PmCacheCfg(all=CacheMode.ON, value=CacheMode.REQUEST)
  public final PmAttrString street = new PmAttrStringImpl(this);

  public final PmAttrString nr     = new PmAttrStringImpl(this);

  // -- Getter for JSF --
  public PmAttr<City> getCity()     { return city; }
  public PmAttrString getStreet()   { return street; }
  public PmAttrString getNr()       { return nr; }
}

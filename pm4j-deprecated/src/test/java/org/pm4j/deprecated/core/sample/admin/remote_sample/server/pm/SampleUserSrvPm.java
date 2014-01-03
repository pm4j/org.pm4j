package org.pm4j.deprecated.core.sample.admin.remote_sample.server.pm;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmAttrLong;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmAttrLongImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.deprecated.core.sample.admin.remote_sample.server.service.AdminService;
import org.pm4j.deprecated.core.sample.admin.remote_sample.server.service.Department;
import org.pm4j.deprecated.core.sample.admin.remote_sample.server.service.SampleUser;
import org.pm4j.deprecated.core.sample.admin.remote_sample.shared.Gender;

@PmBeanCfg(beanClass=SampleUser.class)
public class SampleUserSrvPm extends PmBeanBase<SampleUser> {
  public PmAttrLong id = new PmAttrLongImpl(this);
  public PmAttrString loginName = new PmAttrStringImpl(this);
  public PmAttrString firstName = new PmAttrStringImpl(this);
  public PmAttrString lastName = new PmAttrStringImpl(this);
  public PmAttrEnum<Gender> gender = new PmAttrEnumImpl<Gender>(this, Gender.class);
  public PmAttr<Department> department = new PmAttrImpl<Department>(this);
  public PmAttrString notes = new PmAttrStringImpl(this);
  
  public PmCommand cmdSave = new PmCommandImpl(this) {
    @PmInject AdminService adminService;
    
    protected void doItImpl() {
      adminService.saveUser(getPmBean());
    }
  };
}

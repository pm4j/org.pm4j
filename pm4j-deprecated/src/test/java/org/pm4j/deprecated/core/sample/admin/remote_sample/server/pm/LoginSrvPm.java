package org.pm4j.deprecated.core.sample.admin.remote_sample.server.pm;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.deprecated.core.sample.admin.remote_sample.server.service.Department;
import org.pm4j.deprecated.core.sample.admin.remote_sample.server.service.LoginService;
import org.pm4j.deprecated.core.sample.admin.remote_sample.shared.LoginState;


public class LoginSrvPm extends PmObjectBase {
//  @PmServerCfg(providedAspects={PmAspect.VALUE, PmAspect.OPTIONS})
  @PmOptionCfg(values="#adminService.departments", id="id", title="name")
  public final PmAttr<Department> department = new PmAttrImpl<Department>(this);

  @PmAttrCfg(minLen=3)
  public final PmAttrString userName = new PmAttrStringImpl(this);

  @PmAttrCfg(minLen=6)
  public final PmAttrString pwd = new PmAttrStringImpl(this);

  public final PmAttr<LoginState> loginState = new PmAttrImpl<LoginState>(this);

  public final PmCommand cmdLogin = new PmCommandImpl(this) {

    @PmInject private LoginService loginService;

    protected void doItImpl() throws Exception {
      if (loginService.login(userName.getValue(), pwd.getValue())) {
        loginState.setValue(new LoginState("logged in"));
      }
      else {
        throw new PmRuntimeException(this, "login.failed_please_try_again");
      }
    }
  };

}

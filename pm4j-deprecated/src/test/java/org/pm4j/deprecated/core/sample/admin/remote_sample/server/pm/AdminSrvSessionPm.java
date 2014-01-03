package org.pm4j.deprecated.core.sample.admin.remote_sample.server.pm;

import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.deprecated.core.sample.admin.remote_sample.server.service.AdminService;

@PmFactoryCfg(beanPmClasses=SampleUserSrvPm.class)
public class AdminSrvSessionPm extends PmConversationImpl {

  @PmInject AdminService adminService;

  /**
   * Provides user PMs for remote clients.
   *
   * @param userId User record identifier.
   * @return The server PM for the requested user.
   */
  public SampleUserSrvPm getUserPm(long userId) {
    return PmFactoryApi.getPmForBean(this, adminService.getUser(userId));
  }
}

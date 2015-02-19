package org.pm4j.demo;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.impl.NaviLinkImpl;

public class DemoSessionPm extends PmConversationImpl {

  final NaviLink BASIC_DEMO_PAGE = new NaviLinkImpl("/pages/demo/basic/std/basic_std.jsf");
  final NaviLink USER_ADMIN_DEMO_PAGE = new NaviLinkImpl("/pages/demo/admin/userList.jsf");

  public final PmCommand cmdBasicFeatureDemo = new PmCommandImpl(this, BASIC_DEMO_PAGE);
  public final PmCommand cmdUserAdminDemo = new PmCommandImpl(this, USER_ADMIN_DEMO_PAGE);

  // -- Getter --
  public PmCommand getCmdBasicFeatureDemo() { return cmdBasicFeatureDemo; }
  public PmCommand getCmdUserAdminDemo() { return cmdUserAdminDemo; }
}

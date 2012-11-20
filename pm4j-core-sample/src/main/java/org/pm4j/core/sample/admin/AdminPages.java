package org.pm4j.core.sample.admin;

import org.pm4j.core.sample.admin.user.service.User;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.impl.NaviLinkImpl;

public class AdminPages {

  public static final NaviLink USER_EDIT_PAGE = new NaviLinkImpl("/pages/demo/admin/userEdit.jsf");

  public static NaviLink makeUserEditLink(User user) {
    return NaviLinkImpl.makeNaviScopeParamLink(
        AdminPages.USER_EDIT_PAGE, "userName", user.getName());
  }

}

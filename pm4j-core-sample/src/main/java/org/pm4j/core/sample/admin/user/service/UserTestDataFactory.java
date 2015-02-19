package org.pm4j.core.sample.admin.user.service;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.sample.admin.user.UserEditPm;

public class UserTestDataFactory {

  public static UserEditPm makeUserEditPm() {
    return new UserEditPm(makeSessionPm(), new User());
  }
  
  public static PmConversation makeSessionPm() {
    PmConversation s = new PmConversationImpl();
    s.setPmNamedObject("userService", new UserService());
    return s;
  }
  
}

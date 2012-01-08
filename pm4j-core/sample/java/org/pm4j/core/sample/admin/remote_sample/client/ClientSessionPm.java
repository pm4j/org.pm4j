package org.pm4j.core.sample.admin.remote_sample.client;

import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.sample.admin.remote_sample.shared.LoginState;

/**
 * The root PM session which additionally stores some login state information.
 */
public class ClientSessionPm extends PmConversationImpl {
  
  private LoginState loginState;

  public LoginState getLoginState() { return loginState; }
  public void setLoginState(LoginState loginState) { this.loginState = loginState; }
}

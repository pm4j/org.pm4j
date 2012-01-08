package org.pm4j.core.sample.admin.remote_sample.shared;

import java.io.Serializable;

public class LoginState implements Serializable {
  private static final long serialVersionUID = 1L;

  public String info;
  
  public LoginState(String info) {
    this.info = info;
  }
}

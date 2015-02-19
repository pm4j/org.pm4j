package org.pm4j.deprecated.core.sample.admin.remote_sample.client;

import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmAttrLong;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.impl.*;
import org.pm4j.deprecated.core.sample.admin.remote_sample.annotations.PmClientCfg;
import org.pm4j.deprecated.core.sample.admin.remote_sample.shared.Gender;

@PmClientCfg()
public class SampleUserPm extends PmObjectBase {

  // TODO: add a remote configuration to API.
  protected String getPmServerCfg() {
    return String.format("#pmConversation.getUserPm(%1)", id.getValue());
  }

  @PmAttrCfg(readOnly=true)
  public PmAttrLong id = new PmAttrLongImpl(this);
  @PmAttrCfg(minLen=3, maxLen=20)
  public PmAttrString loginName = new PmAttrStringImpl(this);
  public PmAttrString firstName = new PmAttrStringImpl(this);
  public PmAttrString lastName = new PmAttrStringImpl(this);
  @PmAttrCfg(required=true)
  public PmAttrEnum<Gender> gender = new PmAttrEnumImpl<Gender>(this, Gender.class);
  public PmAttrString department = new PmAttrStringImpl(this);
  public PmAttrString notes = new PmAttrStringImpl(this);

  public PmCommand cmdSave = new PmCommandImpl(this);
  @PmClientCfg(isServerProvided=false)
  public PmCommand cmdCancel = new PmCommandImpl(this);
}

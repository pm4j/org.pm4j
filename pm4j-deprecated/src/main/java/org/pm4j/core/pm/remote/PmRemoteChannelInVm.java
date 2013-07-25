package org.pm4j.core.pm.remote;

import org.pm4j.core.pm.PmAspect;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.serialization.PmContentCfg;
import org.pm4j.core.pm.serialization.PmContentSerializer;

public class PmRemoteChannelInVm implements PmRemoteChannel {

  private final PmObject receiverCtxtPm;
  private final PmContentCfg clientSendContentCfg;
  private final PmContentCfg serverSendContentCfg;

  public PmRemoteChannelInVm(PmObject receiverCtxtPm, PmContentCfg clientSendContentCfg, PmContentCfg serverSendContentCfg) {
    this.receiverCtxtPm = receiverCtxtPm;
    this.clientSendContentCfg = clientSendContentCfg;
    this.serverSendContentCfg = serverSendContentCfg;
  }

  public PmRemoteChannelInVm(PmObject receiverCtxtPm, PmContentCfg clientAndServerContentCfg) {
    this(receiverCtxtPm, clientAndServerContentCfg, clientAndServerContentCfg);
  }

  public PmRemoteChannelInVm(PmObject receiverCtxtPm) {
    this(receiverCtxtPm, new PmContentCfg(PmAspect.VALUE));
  }

  @Override
  public void sendValuesAndCallServerCommand(String pmPath, PmObject pm, String cmdToCall) {
    PmContentSerializer clientSerializer = new PmContentSerializer(clientSendContentCfg);
    PmContentSerializer serverSerializer = new PmContentSerializer(serverSendContentCfg);

    byte[] content = clientSerializer.serialize(pmPath, pm);
    PmObject serverPm = serverSerializer.deserialize(receiverCtxtPm, content);
    try {
      PmCommand cmd = PmExpressionApi.getByExpression(serverPm, cmdToCall, PmCommand.class);
      cmd.doIt();
    }
    finally {
      // The direct receiver PM on client side should get the content.
      content = serverSerializer.serializeWithPmMessages(serverPm);
      PmMessageUtil.clearSubTreeMessages(serverPm);
    }

    // De-serialize server PM content and messages to the client PM.
    clientSerializer.deserialize(pm, content);
  }


}

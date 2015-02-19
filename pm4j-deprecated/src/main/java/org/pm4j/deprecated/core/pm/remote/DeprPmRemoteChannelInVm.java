package org.pm4j.deprecated.core.pm.remote;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.deprecated.core.pm.DeprPmAspect;
import org.pm4j.deprecated.core.pm.serialization.DeprPmContentCfg;
import org.pm4j.deprecated.core.pm.serialization.DeprPmContentSerializer;

public class DeprPmRemoteChannelInVm implements DeprPmRemoteChannel {

  private final PmObject receiverCtxtPm;
  private final DeprPmContentCfg clientSendContentCfg;
  private final DeprPmContentCfg serverSendContentCfg;

  public DeprPmRemoteChannelInVm(PmObject receiverCtxtPm, DeprPmContentCfg clientSendContentCfg, DeprPmContentCfg serverSendContentCfg) {
    this.receiverCtxtPm = receiverCtxtPm;
    this.clientSendContentCfg = clientSendContentCfg;
    this.serverSendContentCfg = serverSendContentCfg;
  }

  public DeprPmRemoteChannelInVm(PmObject receiverCtxtPm, DeprPmContentCfg clientAndServerContentCfg) {
    this(receiverCtxtPm, clientAndServerContentCfg, clientAndServerContentCfg);
  }

  public DeprPmRemoteChannelInVm(PmObject receiverCtxtPm) {
    this(receiverCtxtPm, new DeprPmContentCfg(DeprPmAspect.VALUE));
  }

  @Override
  public void sendValuesAndCallServerCommand(String pmPath, PmObject pm, String cmdToCall) {
    DeprPmContentSerializer clientSerializer = new DeprPmContentSerializer(clientSendContentCfg);
    DeprPmContentSerializer serverSerializer = new DeprPmContentSerializer(serverSendContentCfg);

    byte[] content = clientSerializer.serialize(pmPath, pm);
    PmObject serverPm = serverSerializer.deserialize(receiverCtxtPm, content);
    try {
      PmCommand cmd = PmExpressionApi.getByExpression(serverPm, cmdToCall, PmCommand.class);
      cmd.doIt();
    }
    finally {
      // The direct receiver PM on client side should get the content.
      content = serverSerializer.serializeWithPmMessages(serverPm);
      PmMessageApi.clearPmTreeMessages(serverPm);
    }

    // De-serialize server PM content and messages to the client PM.
    clientSerializer.deserialize(pm, content);
  }


}

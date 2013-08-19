package org.pm4j.core.pm.remote;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrIntegerCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.pm.impl.PmUtil;

public class PmRemoteChannelInVmTest {

  @Test @Ignore("oboede: re-implement serialization!")
  public void testRemoteCall() {
    PmConversation clientSession = new PmConversationImpl();
    ClientPm clientPm = new ClientPm(clientSession);

    clientPm.s1.setValue("Client says hello!");
    clientPm.cmdSave.doIt();

    assertEquals("Server says hello!", clientPm.s1.getValue());
  }

  @Test @Ignore("oboede: re-implement serialization!")
  public void testRemoteCallWithFailedServerValidation() {
    PmConversation clientSession = new PmConversationImpl();
    ClientPm clientPm = new ClientPm(clientSession);

    clientPm.s1.setValue("Client says hello!");
    clientPm.i1.setValue(11);
    clientPm.cmdSave.doIt();

    assertEquals("The client values should not have been changed by the server operation.",
                 "Client says hello!", clientPm.s1.getValue());
    assertEquals("The integer value is too big. There server PM is configured to validate that.",
                 1, PmMessageUtil.getPmErrors(clientPm.i1).size());
  }


  public static class ClientPm extends PmElementImpl {
    public ClientPm(PmObject pmParent) { super(pmParent); }

    public final PmAttrString s1 = new PmAttrStringImpl(this);
    public final PmAttrInteger i1 = new PmAttrIntegerImpl(this);

    public final PmCommand cmdSave = new PmCommandImpl(this) {
      protected void doItImpl() throws Exception {
        PmElement pmParent = PmUtil.getPmParentOfType(this, PmElement.class);
        channel.sendValuesAndCallServerCommand("getMyTestPm('hallo')", pmParent, "cmdSave");
      }
    };

    private PmRemoteChannel channel = new PmRemoteChannelInVm(new ServerSessionPm());
  }


  public static class ServerPm extends PmElementImpl {
    public ServerPm(PmObject pmParent) {
      super(pmParent);
    }
    public final PmAttrString s1 = new PmAttrStringImpl(this);
    @PmAttrIntegerCfg(maxValue=10)
    public final PmAttrInteger i1 = new PmAttrIntegerImpl(this);

    @PmCommandCfg(beforeDo=BEFORE_DO.VALIDATE) //
    public final PmCommand cmdSave = new PmCommandImpl(this) {
      protected void doItImpl() throws Exception {
        System.out.println("cmdSave was called on server pm.\n"+
            "s1="+s1.getValue() + " i1=" + i1.getValueAsString());
        s1.setValue("Server says hello!");
      }
    };
  }


  public static class ServerSessionPm extends PmConversationImpl {
    // Live time control. Usually delegated to a DI container such as spring.
    public ServerPm getMyTestPm(String s) {
      return new ServerPm(this);
    }
  }

}

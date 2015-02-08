package org.pm4j.deprecated.core.pm.serialisation;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.impl.*;
import org.pm4j.deprecated.core.pm.serialization.DeprPmContentSerializer;

public class PmSerializerTest {

  private MyPmConversation clientSession, serverSession;
  private DeprPmContentSerializer clientSerializer, serverSerializer;

  @Before
  public void setUp() throws Exception {
    clientSession = new MyPmConversation();
    serverSession = new MyPmConversation();
    clientSerializer = new DeprPmContentSerializer();
    serverSerializer = new DeprPmContentSerializer();
  }

  @Test @Ignore("oboede: re-implement serialization!")
  public void testInitServerPmBasedOnClientValues() {
    MyTestPm clientPm = (MyTestPm)PmExpressionApi.getByExpression(serverSession, "myTestPm");
    clientPm.s1.setValue("abc");
    clientPm.i1.setValue(3);

    byte[] bytes = clientSerializer.serialize("myTestPm", clientPm);
    serverSerializer.deserialize(serverSession, bytes);

    MyTestPm serverPm = (MyTestPm)PmExpressionApi.getByExpression(serverSession, "myTestPm");

    assertEquals("abc", serverPm.s1.getValue());
    assertEquals(new Integer(3), serverPm.i1.getValue());
  }

  @Test @Ignore("oboede: re-implement serialization!")
  public void testGetClientPmFromServerAndSendEnteredValuesBack() {
    MyTestPm serverPm = (MyTestPm)PmExpressionApi.getByExpression(serverSession, "myTestPm");
    serverPm.s1.setValue("serverValue");
    serverPm.i1.setValue(null);

    byte[] bytes = serverSerializer.serialize("myTestPm", serverPm);

    clientSerializer.deserialize(clientSession, bytes);
    MyTestPm clientPm = (MyTestPm)PmExpressionApi.getByExpression(serverSession, "myTestPm");
    assertEquals("serverValue", clientPm.s1.getValue());
    assertEquals(null, clientPm.i1.getValue());

    clientPm.s1.setValue("clientValue");
    clientPm.i1.setValue(1);

    bytes = clientSerializer.serialize("myTestPm", clientPm);
    serverSerializer.deserialize(serverSession, bytes);

    serverPm = (MyTestPm)PmExpressionApi.getByExpression(serverSession, "myTestPm");

    assertEquals("clientValue", serverPm.s1.getValue());
    assertEquals(new Integer(1), serverPm.i1.getValue());
  }

  public static class MyPmConversation extends PmConversationImpl {
    // Live time control. Usually delegated to a DI container such as spring.
    @Override
    protected void handleNamedPmObjectNotFound(String name) {
      super.handleNamedPmObjectNotFound(name);
      if ("myTestPm".equals(name)) {
        setPmNamedObject("myTestPm", new MyTestPm(this));
      }
    }
  }

  public static class MyTestPm extends PmObjectBase {
    public MyTestPm(PmObject pmParent) {
      super(pmParent);
    }
    public final PmAttrString s1 = new PmAttrStringImpl(this);
    public final PmAttrInteger i1 = new PmAttrIntegerImpl(this);
  }

}

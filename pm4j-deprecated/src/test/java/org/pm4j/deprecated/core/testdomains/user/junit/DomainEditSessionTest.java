package org.pm4j.deprecated.core.testdomains.user.junit;

import java.util.List;

import junit.framework.TestCase;

import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.deprecated.core.testdomains.user.AdminSession;
import org.pm4j.deprecated.core.testdomains.user.DomainEditSession;

public class DomainEditSessionTest extends TestCase {

  public void testNewEditSave() {
    AdminSession adminSession = new AdminSession(null);
    DomainEditSession domainEditSession = new DomainEditSession();
    domainEditSession.setPmParent(adminSession);

    domainEditSession.cmdNew.doIt();

    assertNotNull(domainEditSession.getEditedDomain());
    domainEditSession.getEditedDomain().name.setValue("hello");

    // not yet to the bean committed value:
    assertEquals("hello", domainEditSession.getEditedDomain().name.getValue());
    assertEquals(null, domainEditSession.getEditedDomain().getPmBean().getName());

    domainEditSession.cmdSave.doIt();
    assertEquals("hello", domainEditSession.getEditedDomain().getPmBean().getName());
    List<PmMessage> infoMessages = PmMessageApi.getMessages(domainEditSession, Severity.INFO);

    assertEquals(1, infoMessages.size());

    // TODO: correct title generation.
    // PmMessage message = domainEditSession.getInfos().get(0);
    // assertEquals("Die Daten der Abteilung \"hello\" wurden gespeichert.", message.getTitle());
  }

}

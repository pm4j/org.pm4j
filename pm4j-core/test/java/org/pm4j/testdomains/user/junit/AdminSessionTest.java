package org.pm4j.testdomains.user.junit;

import org.pm4j.core.pm.PmOption;
import org.pm4j.testdomains.user.AdminSession;

import junit.framework.TestCase;

public class AdminSessionTest extends TestCase {

  public void testSession() {
    AdminSession adminSession = new AdminSession(null);
    
    adminSession.selectedDomain.getValue();
    assertEquals(3, adminSession.selectedDomain.getOptionSet().getOptions().size());
    PmOption o = adminSession.selectedDomain.getOptionSet().getOptions().get(0);
    adminSession.selectedDomain.setValueAsString(o.getIdAsString());
    
  }
}

package org.pm4j.testdomains.user;

import java.util.Locale;

import org.pm4j.core.pm.PmAttrPmRef;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.impl.PmAttrPmRefImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

@PmFactoryCfg(beanPmClasses={DomainPm.class, UserPm.class})
public class AdminSession extends PmConversationImpl {

  // -------- Service layer references -------- //

  private DomainService domainService = new DomainService();

  // -------- Session attributes -------- //

  @PmFactoryCfg(beanPmClasses={DomainPm.class, UserPm.class})
  @PmOptionCfg(values="getDomainService().domains", id="id", title="name", nullOption=NullOption.NO)
  public final PmAttrPmRef<DomainPm> selectedDomain = new PmAttrPmRefImpl<DomainPm, Domain>(this) {
  };

  // --------  -------- //

  /**
   * Constructor initializes the service as session variable:
   */
  public AdminSession(PmConversation parentSession) {
    // XXX olaf: the classes
    super(parentSession);
    // fix Locale to ensure that title tests may run successfully on different machines.
    setPmLocale(Locale.GERMAN);
  }

  // -------- Generated stuff -------- //

  public PmAttrPmRef<DomainPm> getSelectedDomain() {
    return selectedDomain;
  }

  public DomainService getDomainService() {
    return domainService;
  }

  public void setDomainService(DomainService domainService) {
    this.domainService = domainService;
  }

}

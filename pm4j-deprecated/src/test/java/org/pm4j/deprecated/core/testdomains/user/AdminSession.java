package org.pm4j.deprecated.core.testdomains.user;

import java.util.Locale;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmDefaults;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.deprecated.core.pm.DeprPmAttrPmRef;
import org.pm4j.deprecated.core.pm.impl.DeprPmAttrPmRefImpl;

@PmFactoryCfg(beanPmClasses={DomainPm.class, UserPm.class})
public class AdminSession extends PmConversationImpl {

  // -------- Service layer references -------- //

  private DomainService domainService = new DomainService();

  // -------- Session attributes -------- //

  @PmFactoryCfg(beanPmClasses={DomainPm.class, UserPm.class})
  @PmOptionCfg(values="getDomainService().domains", id="id", title="name", nullOption=NullOption.NO)
  public final DeprPmAttrPmRef<DomainPm> selectedDomain = new DeprPmAttrPmRefImpl<DomainPm, Domain>(this) {
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

  public DeprPmAttrPmRef<DomainPm> getSelectedDomain() {
    return selectedDomain;
  }

  public DomainService getDomainService() {
    return domainService;
  }

  public void setDomainService(DomainService domainService) {
    this.domainService = domainService;
  }

}

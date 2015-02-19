package org.pm4j.testdomains.user.model;

import java.util.ArrayList;
import java.util.List;


public class DomainService {
  
  private List<Domain> domains = new ArrayList<Domain>();
  
  public DomainService() {
    saveDomain(new Domain("Verwaltung"));
    saveDomain(new Domain("Abteilung 1"));
    saveDomain(new Domain("Abteilung 2"));
  }
  
  public List<Domain> getDomains() {
    return domains;
  }
  
  public Domain saveDomain(Domain domain) {
    if (domain.getId() == null) {
      domain.setId((long)domains.size());
      domains.add(domain);
    }

    return domain;
  }

  public boolean deleteDomain(Domain domain) {
    return domains.remove(domain);
  }
}

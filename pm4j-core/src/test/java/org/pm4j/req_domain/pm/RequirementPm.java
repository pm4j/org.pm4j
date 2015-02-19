package org.pm4j.req_domain.pm;

import org.pm4j.core.pm.PmAttrLong;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmBean;
import org.pm4j.req_domain.model.Requirement;

public interface RequirementPm extends PmBean<Requirement> {
  
  PmAttrLong getId();

  PmAttrString getName();

  PmAttrString getDescription();

  PmAttrString getAuthor();

}

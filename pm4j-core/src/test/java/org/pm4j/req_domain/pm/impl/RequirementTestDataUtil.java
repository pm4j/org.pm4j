package org.pm4j.req_domain.pm.impl;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.req_domain.model.Requirement;
import org.pm4j.req_domain.pm.RequirementPm;

public class RequirementTestDataUtil {

  public static RequirementPm makeRequirementPm() {
    PmConversation pmConversation = new PmConversationImpl(RequirementPmImpl.class);
    Requirement requirement = new Requirement();
    requirement.setName("Hallo");
    RequirementPm requirementPm = PmFactoryApi.getPmForBean(pmConversation, requirement);
    return requirementPm;
  }
}

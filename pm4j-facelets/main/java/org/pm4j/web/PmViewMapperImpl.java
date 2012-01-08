package org.pm4j.web;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.api.PmMessageUtil;

public class PmViewMapperImpl implements PmViewMapper {

  @Override
  public String styleClassForPm(PmAttr<?> pmAttr) {
    String result = "";

    if (pmAttr.isRequired()) {
      result += " required";
    }

    if (PmMessageUtil.getPmMessages(pmAttr).size() > 0) {
      if (PmMessageUtil.getPmErrors(pmAttr).size() > 0) {
        result += " error";
      }
      if (PmMessageUtil.getPmWarnings(pmAttr).size() > 0) {
        result += " warn";
      }
      if (PmMessageUtil.getPmInfos(pmAttr).size() > 0) {
        result += " info";
      }
    }
    return result;
  }

  @Override
  public String styleClassForPm(PmCommand pmCommand) {
    return null;
  }

  @Override
  public String styleClassForPmMsg(PmMessage pmMsg) {
    switch (pmMsg.getSeverity()) {
      case ERROR: return "pm_msg_error";
      case WARN : return "pm_msg_warn";
      case INFO : return "pm_msg_info";
      default: throw new IllegalArgumentException("Unknown enum value: " + pmMsg.getSeverity());
    }
  }

}

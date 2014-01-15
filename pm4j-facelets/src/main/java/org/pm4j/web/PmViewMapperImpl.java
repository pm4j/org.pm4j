package org.pm4j.web;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.api.PmMessageApi;

@Deprecated
public class PmViewMapperImpl implements PmViewMapper {

  @Override
  public String styleClassForPm(PmAttr<?> pmAttr) {
    String result = "";

    if (pmAttr.isRequired()) {
      result += " required";
    }

    if (PmMessageApi.getMessages(pmAttr).size() > 0) {
      if (PmMessageApi.getMessages(pmAttr, Severity.ERROR).size() > 0) {
        result += " error";
      }
      if (PmMessageApi.getMessages(pmAttr, Severity.WARN).size() > 0) {
        result += " warn";
      }
      if (PmMessageApi.getMessages(pmAttr, Severity.INFO).size() > 0) {
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

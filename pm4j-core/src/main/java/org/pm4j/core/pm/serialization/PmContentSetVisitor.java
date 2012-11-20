package org.pm4j.core.pm.serialization;

import java.io.Serializable;

import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAspect;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.core.pm.impl.PmVisitorAdapter;

public class PmContentSetVisitor extends PmVisitorAdapter {

  private PmContentContainer contentContainer;
  private Object eventSource;

  public PmContentSetVisitor(Object eventSource, PmContentContainer contentContainer) {
    assert contentContainer != null;
    this.contentContainer = contentContainer;
    this.eventSource = eventSource;
  }

  @Override
  protected void onVisit(PmObject pm) {
    for (PmObject child : PmUtil.getPmChildren(pm)) {
      PmContentContainer c = contentContainer.getNamedChildContentMap().get(child.getPmName());
      if (c != null) {
        child.accept(new PmContentSetVisitor(eventSource, c));
      }
    }
    super.onVisit(pm);
  }

  @Override
  public void visit(PmAttr<?> attr) {
    setAspect(attr, PmAspect.VALUE);
    super.visit(attr);

  }

  private void setAspect(PmObject pm, PmAspect aspect) {
    Serializable value = contentContainer.getAspect(aspect);
    if (value != null || contentContainer.getAspectMap().containsKey(aspect)) {
      try {
        PmUtil.setPmContentAspect(pm, aspect, value);
      } catch (PmConverterException e) {
        throw new PmRuntimeException(pm, "Unable to set PM aspect '" + aspect + "'.", e);
      }
    }
  }
}

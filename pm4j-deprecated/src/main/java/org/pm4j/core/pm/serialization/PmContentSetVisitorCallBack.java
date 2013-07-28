package org.pm4j.core.pm.serialization;

import java.io.Serializable;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAspect;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.api.PmVisitorApi.VisitResult;
import org.pm4j.core.pm.impl.PmUtil;


public class PmContentSetVisitorCallBack implements PmVisitorApi.VisitHierarchyCallBack {

  private Deque<PmContentContainer> stack = new LinkedList<PmContentContainer>();
  private PmContentContainer contentContainer;

  public PmContentSetVisitorCallBack(PmContentContainer contentContainer) {
    assert contentContainer != null;
    this.contentContainer = contentContainer;
    this.stack.push(contentContainer);
  }

  @Override
  public VisitResult visit(PmObject pm) {
    PmContentContainer c = contentContainer.getNamedChildContentMap().get(pm.getPmName());
    if (c != null && pm instanceof PmAttr<?>) {
      setAspect(pm, PmAspect.VALUE, c);
    }
    return VisitResult.CONTINUE;
  }

  @Override
  public VisitResult enterChildren(PmObject pmParent, Collection<PmObject> pmChildren) {
    PmContentContainer c = contentContainer.getNamedChildContentMap().get(pmParent.getPmName());
    if (c != null) {
      stack.push(c);
      contentContainer = c;
      return VisitResult.CONTINUE;
    } else {
      return VisitResult.SKIP_CHILDREN;
    }
  }

  @Override
  public void leaveChildren(PmObject pmParent, Collection<PmObject> pmChildren) {
    stack.pop();
    contentContainer = stack.getFirst();
  }

  private static void setAspect(PmObject pm, PmAspect aspect, PmContentContainer contentContainer) {
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

package org.pm4j.deprecated.core.pm.serialization;

import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitResult;
import org.pm4j.deprecated.core.pm.DeprPmAspect;


public class DeprPmContentSetVisitorCallBack implements PmVisitorApi.PmVisitHierarchyCallBack {

  private Deque<DeprPmContentContainer> stack = new LinkedList<DeprPmContentContainer>();
  private DeprPmContentContainer contentContainer;

  public DeprPmContentSetVisitorCallBack(DeprPmContentContainer contentContainer) {
    assert contentContainer != null;
    this.contentContainer = contentContainer;
    this.stack.push(contentContainer);
  }

  @Override
  public PmVisitResult visit(PmObject pm) {
    DeprPmContentContainer c = contentContainer.getNamedChildContentMap().get(pm.getPmName());
    if (c != null && pm instanceof PmAttr<?>) {
      setAspect(pm, DeprPmAspect.VALUE, c);
    }
    return PmVisitResult.CONTINUE;
  }

  @Override
  public PmVisitResult enterChildren(PmObject pmParent, Iterable<PmObject> pmChildren) {
    DeprPmContentContainer c = contentContainer.getNamedChildContentMap().get(pmParent.getPmName());
    if (c != null) {
      stack.push(c);
      contentContainer = c;
      return PmVisitResult.CONTINUE;
    } else {
      return PmVisitResult.SKIP_CHILDREN;
    }
  }

  @Override
  public void leaveChildren(PmObject pmParent, Iterable<PmObject> pmChildren) {
    stack.pop();
    contentContainer = stack.getFirst();
  }

  private static void setAspect(PmObject pm, DeprPmAspect aspect, DeprPmContentContainer contentContainer) {
    Serializable value = contentContainer.getAspect(aspect);
    if (value != null || contentContainer.getAspectMap().containsKey(aspect)) {
// TODO:
//      try {
//        PmUtil.setPmContentAspect(pm, aspect, value);
//      } catch (PmConverterException e) {
//        throw new PmRuntimeException(pm, "Unable to set PM aspect '" + aspect + "'.", e);
//      }
    }
  }

}

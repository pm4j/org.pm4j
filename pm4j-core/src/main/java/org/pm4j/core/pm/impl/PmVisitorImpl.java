package org.pm4j.core.pm.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi.VisitCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.VisitHint;
import org.pm4j.core.pm.api.PmVisitorApi.VisitResult;

/**
 * Visitor implementations. Descends deep first.
 * 
 */
public class PmVisitorImpl {

  private final Set<VisitHint> hints;
  private final VisitCallBack callBack;
  private PmObject stopOnPmObject = null;

  /**
   * Creates a visitor.
   * 
   * @param callBack
   *          the core part of the visitor client.
   * @param hints
   *          static selections.
   */
  public PmVisitorImpl(VisitCallBack callBack, VisitHint... hints) {
    assert callBack != null;
    assert hints != null;
    this.callBack = callBack;
    this.hints = new HashSet<VisitHint>(Arrays.asList(hints));
  }

  /**
   * Starts the visit of pm and pm's children.
   * 
   * @param pm
   *          the visited p.m
   */
  public void visit(PmObject pm) {
    assert pm != null;
    visitObject(pm);
  }

  /**
   * If {@link VisitCallBack} visit returns {@link VisitResult#STOP_VISIT} the
   * responsible pm child will be returned.
   * 
   * @return the visit stopping pm object.
   */
  public PmObject getStopOnPmObject() {
    return stopOnPmObject;
  }

  private VisitResult considerHints(PmObject pm) {
    if (hints.contains(VisitHint.SKIP_CONVERSATION)) {
      if (pm instanceof PmConversation) {
        return VisitResult.SKIP_CHILDREN;
      }
    }
    if (hints.contains(VisitHint.SKIP_READ_ONLY)) {
      if (pm.isPmReadonly()) {
        return VisitResult.SKIP_CHILDREN;
      }
    }
    if (hints.contains(VisitHint.SKIP_INVISIBLE)) {
      if (!pm.isPmVisible()) {
        return VisitResult.SKIP_CHILDREN;
      }
    }
    if (hints.contains(VisitHint.SKIP_DISABLED)) {
      if (!pm.isPmEnabled()) {
        return VisitResult.SKIP_CHILDREN;
      }
    }
    return null;
  }

  private VisitResult visitObject(PmObject pm) {

    VisitResult hintResult = considerHints(pm);
    if (hintResult != null) {
      return hintResult;
    }

    // The moment where the elephant...
    VisitResult result = callBack.visit(pm);

    switch (result) {
    case STOP_VISIT:
      stopOnPmObject = pm;
      return VisitResult.STOP_VISIT;
    case SKIP_CHILDREN:
      return VisitResult.SKIP_CHILDREN;
    case CONTINUE:
      visitChildren(pm);
      if (stopOnPmObject != null) {
        return VisitResult.STOP_VISIT;
      }
    }
    return result;
  }

  private void visitChildren(PmObject pm) {
    Collection<PmObject> children = ((PmObjectBase) pm).getPmChildren();
    visitChildrenCollection(children);
    if (!hints.contains(VisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS)) {
      children = ((PmObjectBase) pm).getFactoryGeneratedChildPms();
      visitChildrenCollection(children);
    }
  }

  private void visitChildrenCollection(Collection<PmObject> children) {
    for (PmObject child : children) {
      if (stopOnPmObject != null) {
        return;
      }
      visitObject(child);
    }
  }
}

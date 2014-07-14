package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.pm4j.common.util.collection.IterableUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHierarchyCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHint;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitResult;

/**
 * Visitor implementations. Descends deep first.
 *
 * @author dietmar zabel, olaf boede
 */
public class PmVisitorImpl {

  private final Set<PmVisitHint> hints;
  private PmVisitCallBack callBack;
  private PmObject visitStoppedOn = null;

  /**
   * Creates a visitor.
   *
   * @param callBack
   *          the core part of the visitor client.
   * @param hints
   *          static selections.
   */
  public PmVisitorImpl(PmVisitCallBack callBack, PmVisitHint... hints) {
    assert callBack != null;
    assert hints != null;
    this.callBack = callBack;
    this.hints = new HashSet<PmVisitHint>(Arrays.asList(hints));
  }

  /**
   * Creates a visitor.
   *
   * @param callBack
   *          the core part of the visitor client.
   * @param hints
   *          static selections.
   */
  public PmVisitorImpl(PmVisitHint... hints) {
    assert hints != null;
    this.hints = new HashSet<PmVisitHint>(Arrays.asList(hints));
  }

  /**
   * Sets the {@link PmVisitCallBack} to use.
   *
   * @param callBack The callback.
   */
  public void setCallBack(PmVisitCallBack callBack) {
    this.callBack = callBack;
    this.visitStoppedOn = null;
  }

  /**
   * Starts the visit of pm and pm's children.
   *
   * @param pm
   *          the PM to visit
   * @return the visit result state.
   */
  public PmVisitResult visit(PmObject pm) {
    assert pm != null;
    if (callBack == null) {
      throw new PmRuntimeException(pm, "Please define a callback.");
    }
    PmVisitResult hintResult = considerHints(pm);
    if (hintResult != null) {
      return hintResult;
    }

    // The moment where the elephant...
    PmVisitResult result = callBack.visit(pm);

    switch (result) {
      case STOP_VISIT:
        visitStoppedOn = pm;
        return PmVisitResult.STOP_VISIT;
      case SKIP_CHILDREN:
        return PmVisitResult.SKIP_CHILDREN;
      case CONTINUE:
        return visitChildren(pm);
      default:
        throw new RuntimeException("Unhandled visit result: " + result);
    }
  }

  /**
   * Starts the visit of pm's children.
   *
   * @param pm
   *          the PM to visit.
   */
  public PmVisitResult visitChildren(PmObject pm) {
    assert pm != null;
    Iterable<PmObject> children = getChildren(pm);
    if (children.iterator().hasNext()) {
      if(callBack instanceof PmVisitHierarchyCallBack) {
        PmVisitHierarchyCallBack vhcb = (PmVisitHierarchyCallBack)callBack;
        PmVisitResult enterResult = vhcb.enterChildren(pm, children);
        switch (enterResult) {
        case CONTINUE:
          visitChildrenCollection(children);
          vhcb.leaveChildren(pm, children);
          break;
        case SKIP_CHILDREN:
          // continue without having the children visited.
          return PmVisitResult.CONTINUE;
        case STOP_VISIT:
          return PmVisitResult.STOP_VISIT;
        }
      }
      else {
        visitChildrenCollection(children);
      }
    }
    if (visitStoppedOn != null) {
      return PmVisitResult.STOP_VISIT;
    }

    // no stop
    return PmVisitResult.CONTINUE;
  }

  /**
   * If {@link PmVisitCallBack} visit returns {@link PmVisitResult#STOP_VISIT} the
   * responsible PM child will be returned.
   *
   * @return the visit stopping PM object.
   */
  public PmObject getVisitStoppedOn() {
    return visitStoppedOn;
  }

  private PmVisitResult considerHints(PmObject pm) {

    if (!PmInitApi.isPmInitialized(pm)) {
      if (hints.contains(PmVisitHint.SKIP_NOT_INITIALIZED)) {
        return PmVisitResult.SKIP_CHILDREN;
      } else {
        PmInitApi.ensurePmInitialization(pm);
      }
    }

    if (hints.contains(PmVisitHint.SKIP_CONVERSATION)) {
      if (pm instanceof PmConversation) {
        return PmVisitResult.SKIP_CHILDREN;
      }
    }
    if (hints.contains(PmVisitHint.SKIP_READ_ONLY)) {
      if (pm.isPmReadonly()) {
        return PmVisitResult.SKIP_CHILDREN;
      }
    }
    if (hints.contains(PmVisitHint.SKIP_INVISIBLE)) {
      if (!pm.isPmVisible()) {
        return PmVisitResult.SKIP_CHILDREN;
      }
    }
    if (hints.contains(PmVisitHint.SKIP_DISABLED)) {
      if (!pm.isPmEnabled()) {
        return PmVisitResult.SKIP_CHILDREN;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  protected Iterable<PmObject> getChildren(PmObject pm) {
    // TODO: Change to iterable to be able to handle larger collections
    // without memory problems.
    Collection<PmObject> allChildren = new ArrayList<PmObject>();
    allChildren.addAll(((PmObjectBase) pm).getPmChildren());
    if (pm instanceof PmTable && hints.contains(PmVisitHint.ALL_TABLE_ROWS)) {
      allChildren.addAll(IterableUtil.asCollection(((PmTable<PmObject>)pm).getPmPageableCollection()));
    }
    else if (!hints.contains(PmVisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS)) {
      allChildren.addAll(((PmObjectBase) pm).getFactoryGeneratedChildPms());
    }
    return allChildren;
  }

  private void visitChildrenCollection(Iterable<PmObject> children) {
    for (PmObject child : children) {
      if (visitStoppedOn != null) {
        return;
      }
      visit(child);
    }
  }
}

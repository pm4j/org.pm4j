package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pm4j.common.util.collection.ListUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTab;
import org.pm4j.core.pm.PmTabSet;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTreeNode;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHierarchyCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHint;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitResult;
import org.pm4j.core.pm.api.PmVisitorApi.PmMatcher;

/**
 * Visitor implementations. Descends deep first.
 *
 * @author dietmar zabel, olaf boede
 */
public class PmVisitorImpl {

  private final Set<PmVisitHint> hints;
  private List<PmMatcher> excludes = new ArrayList<PmMatcher>();
  private PmVisitCallBack callBack;
  private PmObject visitRoot = null;
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
   * Defines match conditions for PMs to exclude from the visit.<br>
   * The excluded PMs and their children will not be visited.
   *
   * @param matchers Predicates for PMs that should be skipped.
   * @return this visitor again for fluent programming style support.
   */
  public PmVisitorImpl exclude(PmMatcher... matchers) {
    return exclude(Arrays.asList(matchers));
  }

  /**
   * Defines match conditions for PMs to exclude from the visit.<br>
   * The excluded PMs and their children will not be visited.
   *
   * @param matchers Predicates for PMs that should be skipped.
   * @return this visitor again for fluent programming style support.
   */
  public PmVisitorImpl exclude(Collection<PmMatcher> matchers) {
    if (matchers != null) {
      excludes.addAll(matchers);
    }
    return this;
  }

  /**
   * @param hints Some hints to consider.
   * @return this visitor again for fluent programming style support.
   */
  public PmVisitorImpl hints(PmVisitHint... hints) {
    this.hints.addAll(Arrays.asList(hints));
    return this;
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
    if (visitRoot == null) {
      visitRoot = pm;
    }

    if (callBack == null) {
      throw new PmRuntimeException(pm, "Please define a callback.");
    }

    // The moment where the elephant...
    PmVisitResult result = shouldVisit(pm)
          ? callBack.visit(pm)
          : PmVisitResult.SKIP_CHILDREN;

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

  /**
   * @param pm The PM to check.
   * @return <code>true</code> if the hints agree to visit the given PM.<br>
   *         <code>false</code> if the PM and its children shouldn't be visited.
   */
  private boolean shouldVisit(PmObject pm) {
    if (!PmInitApi.isPmInitialized(pm)) {
      if (hints.contains(PmVisitHint.SKIP_NOT_INITIALIZED)) {
        return false;
      } else {
        PmInitApi.ensurePmInitialization(pm);
      }
    }

    // if the conversation is the visitor start object, then a skip-conversation skips only child conversations.
    if (hints.contains(PmVisitHint.SKIP_CONVERSATION) && (pm != visitRoot)) {
      if (pm instanceof PmConversation) {
        return false;
      }
    }
    if (hints.contains(PmVisitHint.SKIP_READ_ONLY)) {
      if (pm.isPmReadonly()) {
        return false;
      }
    }
    if (hints.contains(PmVisitHint.SKIP_INVISIBLE)) {
      if (! isVisible(pm)) {
        return false;
      }
    }
    if (hints.contains(PmVisitHint.SKIP_HIDDEN_TAB_CONTENT)) {
      if (pm instanceof PmTab &&
          !PmTabSetUtil.isCurrentTab((PmTab) pm)) {
        return false;
      }
    }
    if (hints.contains(PmVisitHint.SKIP_DISABLED)) {
      if (!pm.isPmEnabled()) {
        return false;
      }
    }

    for (PmMatcher matcher : excludes) {
      if (matcher.doesMatch(pm)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Considers in addition to {@link PmObject#isPmVisible()} that content of a
   * not opened tab is not visible as well.
   * <p>
   * The method {@link PmObject#isPmVisible()} can't evaluate that every time to
   * keep a good performance.<br>
   * A visitor loop that considers tab content visibility will usually not get a
   * performance problem. It may even be faster, because it does not iterate
   * invisible not opened tab item trees.<br>
   * In addition it will be semantically correct.
   *
   * @param pm
   *          The PM to check.
   * @return <code>true</code> if its <code>isPmVisible()</code> returns
   *         <code>true</code> and it is not a child of an inactive tab.
   */
  protected boolean isVisible(PmObject pm) {
      PmObject parentPm = pm.getPmParent();
      // Skip invisible tab content. Only the current tab is really visible.
      if (parentPm != null &&
          parentPm instanceof PmTab &&
          parentPm.getPmParent() instanceof PmTabSet &&
          ((PmTabSet)parentPm.getPmParent()).getCurrentTabPm() != parentPm) {
        return false;
      }
      return pm.isPmVisible();
    }


  @SuppressWarnings("unchecked")
  protected Iterable<PmObject> getChildren(PmObject pm) {
    Collection<PmObject> allChildren = new ArrayList<PmObject>();
    allChildren.addAll(((PmObjectBase) pm).getPmChildren());
    if (pm instanceof PmTable &&
        hints.contains(PmVisitHint.ALL_TABLE_ROWS)) {
      ListUtil.addItemsNotYetInCollection(allChildren, ((PmTable<PmObject>)pm).getPmPageableCollection());
    }
    else if (!hints.contains(PmVisitHint.SKIP_FACTORY_GENERATED_CHILD_PMS)) {
      if (!hints.contains(PmVisitHint.SKIP_NOT_INITIALIZED)) {
          // Ensure that dynamic sub-pm's exist. Otherwise they will not be iterated.
          if (pm instanceof PmTable) {
              ListUtil.addItemsNotYetInCollection(allChildren, ((PmTable<PmObject>)pm).getRowPms());
          }
          if (pm instanceof PmTreeNode) {
              ListUtil.addItemsNotYetInCollection(allChildren, (Collection<PmObject>)(Object)((PmTreeNode)pm).getPmChildNodes());
          }
      }
      ListUtil.addItemsNotYetInCollection(allChildren, ((PmObjectBase) pm).getFactoryGeneratedChildPms());
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

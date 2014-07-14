package org.pm4j.deprecated.core.pm.serialization;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHierarchyCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitResult;
import org.pm4j.deprecated.core.pm.DeprPmAspect;

/**
 * Visitor creating a PmContentContainer representing the visited Pm.
 * @author DZABEL
 *
 */
public class DeprPmContentGetVisitorCallBack implements PmVisitHierarchyCallBack {

  private DeprPmContentContainer currentContainer, parentContainer, rootPmContainer;
  private final DeprPmContentCfg contentCfg;
  private final Deque<Pair> contentStack = new ArrayDeque<Pair>();
  private boolean isRootPm = true;

  /**
   * Constructor.
   * @param contentCfg configuring which Pm's will be visited.
   */
  public DeprPmContentGetVisitorCallBack() {
    this(new DeprPmContentCfg());
  }

  /**
   * Constructor.
   * @param contentCfg configuring which Pm's will be visited.
   */
  public DeprPmContentGetVisitorCallBack(DeprPmContentCfg contentCfg) {
    this(contentCfg, new DeprPmContentContainer());
  }

  /**
   * Constructor.
   * @param contentCfg configuring which Pm's will be visited.
   * @param contentContainer return value of the visit.
   */
  public DeprPmContentGetVisitorCallBack(DeprPmContentCfg contentCfg, DeprPmContentContainer contentContainer) {
    assert contentContainer != null;
    assert contentCfg != null;

    this.parentContainer = contentContainer;
    this.currentContainer = contentContainer;
    this.contentCfg = contentCfg;
  }

  @Override
  public PmVisitResult visit(PmObject pm) {
    if (isVisitVisiblePm(pm)) {
      String pmName = pm.getPmName();
      if(isRootPm) {
        // rootPm has its own PmContentContainer instantiation which has to be reused.
        parentContainer.initNamedChildContentMap();
        currentContainer = new DeprPmContentContainer();
        parentContainer.getNamedChildContentMap().put(pmName, currentContainer);
        rootPmContainer = currentContainer;
        isRootPm = false;
      } else {
        // Create new PmContentContainer for this visit.
        currentContainer = parentContainer.addNamedChildContent(pmName);
      }

      if (pm instanceof PmAttr<?>) {
        // TODO:
        Serializable value = ""; // PmUtil.getPmContentAspect(pm, PmAspect.VALUE);
        if (contentCfg.hasAspect(DeprPmAspect.VALUE) && (value != null )) {
          currentContainer.addAspect(DeprPmAspect.VALUE, value);
        }
      }
    }
    return PmVisitResult.CONTINUE;
  }

  @Override
  public PmVisitResult enterChildren(PmObject parent, Iterable<PmObject> pmChildren) {
    contentStack.push(new Pair(parentContainer, parent));
    parentContainer = currentContainer;
    currentContainer = null;
    return PmVisitResult.CONTINUE;
  }

  @Override
  public void leaveChildren(PmObject parent, Iterable<PmObject> pmChildren) {
    Pair pair = contentStack.pop();
    currentContainer = pair.container;
    parentContainer = (!contentStack.isEmpty())
        ? contentStack.peekLast().container
        : null;

    assert pair.pm == parent;
  }

  /**
   * @return the contentContainer
   */
  public DeprPmContentContainer getContentContainer() {
    return rootPmContainer;
  }

  /**
   * @return the contentCfg
   */
  public DeprPmContentCfg getContentCfg() {
    return contentCfg;
  }

  private boolean isVisitVisiblePm(PmObject pm) {
    return (!contentCfg.isOnlyVisibleItems()) || pm.isPmVisible();
  }

  private static class Pair{
    public final DeprPmContentContainer container;
    public final PmObject pm;
    public Pair(DeprPmContentContainer container, PmObject pm) {
      this.container = container;
      this.pm = pm;
    }
  }

}

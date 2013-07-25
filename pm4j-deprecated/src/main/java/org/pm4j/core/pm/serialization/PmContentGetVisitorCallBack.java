package org.pm4j.core.pm.serialization;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import org.pm4j.core.pm.PmAspect;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi.VisitHierarchyCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.VisitResult;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * Visitor creating a PmContentContainer representing the visited Pm.
 * @author DZABEL
 *
 */
public class PmContentGetVisitorCallBack implements VisitHierarchyCallBack {

  private PmContentContainer currentContainer, parentContainer;
  private Pair currentPair;
  private final PmContentCfg contentCfg;
  private final Deque<Pair> contentStack = new ArrayDeque<Pair>();
  private boolean isRootPm = true;

  /**
   * Constructor.
   * @param contentCfg configuring which Pm's will be visited.
   */
  public PmContentGetVisitorCallBack() {
    this(new PmContentCfg());
  }

  /**
   * Constructor.
   * @param contentCfg configuring which Pm's will be visited.
   */
  public PmContentGetVisitorCallBack(PmContentCfg contentCfg) {
    this(contentCfg, new PmContentContainer());
  }

  /**
   * Constructor.
   * @param contentCfg configuring which Pm's will be visited.
   * @param contentContainer return value of the visit.
   */
  public PmContentGetVisitorCallBack(PmContentCfg contentCfg, PmContentContainer contentContainer) {
    assert contentContainer != null;
    assert contentCfg != null;

    this.parentContainer = contentContainer;
    this.currentContainer = contentContainer;
    this.contentCfg = contentCfg;
  }

  private boolean isVisitVisiblePm(PmObject pm) {
    return (!contentCfg.isOnlyVisibleItems()) || pm.isPmVisible();
  }
  
  private static class Pair{
    public final PmContentContainer container;
    public final PmObject pm;
    public Pair(PmContentContainer container, PmObject pm) {
      this.container = container;
      this.pm = pm;
    } 
  }

  @Override
  public VisitResult visit(PmObject pm) {
    if (isVisitVisiblePm(pm)) {
      String pmName = pm.getPmName();
      if(isRootPm) {
        // rootPm has its own PmContentContainer instantiation which has to be reused. 
        currentContainer.initNamedChildContentMap();
        currentContainer.getNamedChildContentMap().put(pmName,  new PmContentContainer());
        isRootPm = false;
      } else {
        // Create new PmContentContainer for this visit.
        currentContainer = parentContainer.addNamedChildContent(pmName);
      }
      currentPair = new Pair(currentContainer, pm);
      storeCurrentPairAspect();
    }
    return VisitResult.CONTINUE;
  }

  @Override
  public void enterChildren(PmObject parent, Collection<PmObject> pmChildren) {
    contentStack.push(currentPair);
    parentContainer = currentContainer;
    currentContainer = null;
  }

  @Override
  public void leaveChildren(PmObject parent, Collection<PmObject> pmChildren) {
    Pair pair = contentStack.pop();
    currentContainer = pair.container;
    parentContainer = (!contentStack.isEmpty())
        ? contentStack.peekLast().container
        : null;
    
    assert pair.pm == parent;
  }

  /**
   * Stores the value aspect in value container.
   */
  private void storeCurrentPairAspect() {
    final PmAspect aspect = PmAspect.VALUE;
    if (currentPair.pm instanceof PmAttr<?>) {
      Serializable value = PmUtil.getPmContentAspect(currentPair.pm, aspect);
      if (contentCfg.hasAspect(aspect) && (value != null )) {
        currentPair.container.addAspect(aspect, value);
      }
    }
  }

  /**
   * @return the contentContainer
   */
  public PmContentContainer getContentContainer() {
    return currentContainer;
  }

  /**
   * @return the contentCfg
   */
  public PmContentCfg getContentCfg() {
    return contentCfg;
  }  
}

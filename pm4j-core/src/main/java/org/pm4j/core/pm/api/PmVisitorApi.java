package org.pm4j.core.pm.api;

import java.util.Collection;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmVisitorImpl;

/**
 * PM tree visitor functionality.
 *
 * @author DZABEL
 */
public class PmVisitorApi {

  /**
   * If the visitor client is just interested in visit a node, this is the
   * right base class.
   */
  public static abstract class DefaultVisitCallBack implements VisitHierarchyCallBack {
    @Override
    public void enterChildren(PmObject parent, Collection<PmObject> pmChildren) {
    }

    @Override
    public void leaveChildren(PmObject parent, Collection<PmObject> pmChildren) {
    }
  }

  /**
   * Visitor call back interface.
   */
  public interface VisitCallBack {
    /**
     * Called by the visitor to do the visit work.
     *
     * @param pm
     *          the current visited PM.
     * @return how the visiting should go on.
     */
    VisitResult visit(PmObject pm);

  }

  /**
   * Visitor call back interface considering children.
   */
  public interface VisitHierarchyCallBack extends VisitCallBack{
    /**
     * If parents children will be visited, this method is called before visiting all children.
     * @param parent the parent.
     */
    void enterChildren(PmObject pmParent, Collection<PmObject> pmChildren);

    /**
     * If parents children will be visited, this method is called after visiting all children.
     * @param parent the parent.
     */
    void leaveChildren(PmObject pmParent, Collection<PmObject> pmChildren);
  }

  /**
   * Gives the user of the PmVisitCallBack the possibility to affect the PM tree
   * traverse.
   */
  public enum VisitResult {
    /** Continue visiting current PM and children. */
    CONTINUE,
    /** Continue visiting this node, but skip this nodes children. (But visit all siblings of this node.) */
    SKIP_CHILDREN,
    /** Stop the tree traverse as fast as possible. */
    STOP_VISIT
  }

  /**
   * Visit hints for static selections. Skips visit of this node and the node
   * children.
   */
  public enum VisitHint {
    /** Skip visiting not visible pm's */
    SKIP_INVISIBLE,
    /** Skip visiting not enabled pm's */
    SKIP_DISABLED,
    /** Skip visiting read only pm's */
    SKIP_READ_ONLY,
    /** Skip visiting {@link PmConversation} pm's */
    SKIP_CONVERSATION,
    /** Skip visiting factory generated child pm's */
    SKIP_FACTORY_GENERATED_CHILD_PMS,
    /** Skip of yet initialized child pm's. */
    SKIP_NOT_INITIALIZED
  }

  /**
   * Visits {@code startPm} and corresponding children.
   *
   * @param startPm
   *          the visit start point.
   * @param visitCallBack
   *          defines what to be done when visiting the PM.
   * @param hints
   *          static selection informations. See {@link VisitHint}
   * @return the object which explicit stopped the visiting.
   */
  public static PmObject visit(PmObject startPm, VisitCallBack visitCallBack, VisitHint... hints) {
    PmVisitorImpl v = new PmVisitorImpl(visitCallBack, hints);
    v.visit(startPm);
    return v.getStopOnPmObject();
  }

  /**
   * Visits the children of the given {@code startPm}.
   *
   * @param parentPm
   *          the parent of the children to visit.
   * @param visitCallBack
   *          defines what to be done when visiting a PM.
   * @param hints
   *          static selection informations. See {@link VisitHint}
   * @return the object which explicit stopped the visiting.
   */
  public static PmObject visitChildren(PmObject parentPm, VisitCallBack visitCallBack, VisitHint... hints) {
    PmVisitorImpl v = new PmVisitorImpl(visitCallBack, hints);
    v.visitChildren(parentPm);
    return v.getStopOnPmObject();
  }

}

package org.pm4j.core.pm.api;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmMatcherBuilder;
import org.pm4j.core.pm.impl.PmVisitorImpl;

/**
 * PM tree visitor functionality.
 *
 * @author DZABEL
 * @author Olaf Boede
 */
public class PmVisitorApi {

  /**
   * Visitor call back interface.
   */
  public interface PmVisitCallBack {
    /**
     * Called by the visitor to do the visit work.
     *
     * @param pm
     *          the current visited PM.
     * @return how the visiting should go on.
     */
    PmVisitResult visit(PmObject pm);

  }

  /**
   * Visitor call back interface considering children.
   */
  public interface PmVisitHierarchyCallBack extends PmVisitCallBack{
    /**
     * If parents children will be visited, this method is called before visiting all children.
     * @param parent the parent.
     * @return controls if the children should really be visited.
     */
    PmVisitResult enterChildren(PmObject pmParent, Iterable<PmObject> pmChildren);

    /**
     * If parents children will be visited, this method is called after visiting all children.
     * @param parent the parent.
     */
    void leaveChildren(PmObject pmParent, Iterable<PmObject> pmChildren);
  }

  /**
   * Gives the user of the PmVisitCallBack the possibility to affect the PM tree
   * traverse.
   */
  public enum PmVisitResult {
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
  public enum PmVisitHint {
    /** Skip visiting not visible pm's */
    SKIP_INVISIBLE,
    /** Skip visiting content of a tab that is not the currently one.
     * @deprecated Please use {@link PmMatcherBuilder} to configure that filter. */
    @Deprecated
    SKIP_HIDDEN_TAB_CONTENT,
    /** Skip visiting not enabled pm's */
    SKIP_DISABLED,
    /** Skip visiting read only pm's */
    SKIP_READ_ONLY,
    /** Skip visiting {@link PmConversation} pm's */
    SKIP_CONVERSATION,
    /** Skip visiting factory generated child pm's. Embedded PMs will still be visited. */
    SKIP_FACTORY_GENERATED_CHILD_PMS,
    /** Visits all rows of table PMs.<br>
     * WARNING: This has some a performance impact, especially
     * for service based tables. */
    ALL_TABLE_ROWS,
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
   *          static selection informations. See {@link PmVisitHint}
   * @return the object which explicit stopped the visiting.
   */
  public static PmVisitResult visit(PmObject startPm, PmVisitCallBack visitCallBack, PmVisitHint... hints) {
    PmVisitorImpl v = new PmVisitorImpl(visitCallBack, hints);
    return v.visit(startPm);
  }

  /**
   * Visits the children of the given {@code startPm}.
   *
   * @param parentPm
   *          the parent of the children to visit.
   * @param visitCallBack
   *          defines what to be done when visiting a PM.
   * @param hints
   *          static selection informations. See {@link PmVisitHint}
   * @return the visit result state.
   */
  public static PmVisitResult visitChildren(PmObject parentPm, PmVisitCallBack visitCallBack, PmVisitHint... hints) {
    PmVisitorImpl v = new PmVisitorImpl(visitCallBack, hints);
    return v.visitChildren(parentPm);
  }

}

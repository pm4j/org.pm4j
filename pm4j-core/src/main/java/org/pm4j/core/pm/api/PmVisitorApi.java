package org.pm4j.core.pm.api;

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
   * Gives the user of the PmVisitCallBack the possibility to affect the PM tree
   * traverse.
   */
  public enum VisitResult {
    /** Continue visiting current PM and children. */
    CONTINUE,
    /** Continue visiting this node, but skip this nodes children. (But visit the
     * siblings of this node.) */
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
    SKIP_FACTORY_GENERATED_CHILD_PMS
  }

  /**
   * See {@link PmVisitorApi#visit(PmObject, VisitCallBack, VisitHint...)}
   * @param startPm the visit start point.
   * @param visitCallBack defines what to be done when visiting the PM. 
   * @return the object which explicit stopped the visiting. 
   */
  public static PmObject visit(PmObject startPm, VisitCallBack visitCallBack) {
    return visit(startPm, visitCallBack, new VisitHint[0]);
  }
  
  /**
   * Visits {@code startPm} and corresponding children.
   * @param startPm the visit start point.
   * @param visitCallBack defines what to be done when visiting the PM. 
   * @param hints static selection informations. See {@link VisitHint}
   * @return the object which explicit stopped the visiting. 
   */
  public static PmObject visit(PmObject startPm, VisitCallBack visitCallBack, VisitHint... hints) {
    PmVisitorImpl v = new PmVisitorImpl(visitCallBack, hints);
    v.visit(startPm);
    return v.getStopOnPmObject();
  }

}

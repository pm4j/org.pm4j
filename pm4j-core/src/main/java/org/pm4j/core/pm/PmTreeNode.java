package org.pm4j.core.pm;


/**
 * Interface for presentation models that may be displayed as tree node items.
 *
 * @author olaf boede
 * @deprecated Please used the interfaces declared in {@link PmObject}.
 */
@Deprecated
public interface PmTreeNode extends PmObject {

  /**
   * @return The set of sub-nodes to display for this item.
   */
  // List<PmTreeNode> getPmChildNodes();

  /**
   * This method supports master-detail views.
   *
   * @return The (optional) details PM to present in a details view.
   *
   * @deprecated Was only used by a show case. Will be replaced by a master-details implementation.
   */
  @Deprecated
  PmObject getNodeDetailsPm();

  /**
   * @return <code>true</code> if this instance should be presented as a tree
   *         leaf node.
   */
  boolean isPmTreeLeaf();

}

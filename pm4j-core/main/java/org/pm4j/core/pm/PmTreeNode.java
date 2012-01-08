package org.pm4j.core.pm;

import java.util.List;

/**
 * Interface for presentation models that may be displayed as tree node items.
 * 
 * @author olaf boede
 */
public interface PmTreeNode extends PmObject {

  /**
   * @return The set of sub-nodes to display for this item. 
   */
  List<PmTreeNode> getPmChildNodes();

  /**
   * This method supports master-detail views. 
   * 
   * @return The (optional) details PM to present in a details view.
   */
  PmObject getNodeDetailsPm();

  /**
   * @return <code>true</code> if this instance should be presented as a tree
   *         leaf node.
   */
  boolean isPmTreeLeaf();
  
}

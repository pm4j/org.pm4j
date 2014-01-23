package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTreeNode;
import org.pm4j.core.pm.api.PmCacheApi;

public abstract class PmElementBase
        extends PmDataInputBase
        implements PmElement {

  /**
   * Cache shortcut member.
   */
  private PmConversation pmConversation;

  /** Cached child nodes for tree interface. */
  private List<PmTreeNode> pmChildNodes = null;

  /**
   * Constructor for dependency injection frameworks that are not able to use constructors
   * (e.g. JSF).<br>
   * Please make sure that the method {@link #setPmParent(PmObject)} is called
   * before this object will be used!
   */
  public PmElementBase() {
    this(null);
  }

  /**
   * @param pmParent
   *          The context, this pm was created in. E.g. a session, a command, a
   *          list field.
   */
  public PmElementBase(PmObject pmParent) {
    super(pmParent);
  }

  /**
   * Base functionality (includes initialization etc.) is finalized here.<p>
   * Subclasses may place their logic in {@link #isPmEnabledImpl()}.
   */
  @Override
  public final boolean isPmEnabled() {
    return super.isPmEnabled();
  }

  @Override
  protected void clearCachedPmValues(Set<PmCacheApi.CacheKind> cacheSet) {
    super.clearCachedPmValues(cacheSet);
    // re-create the cached tree child nodes on next getPmChildeNodes call.
    pmChildNodes = null;
  }

  @Override
  protected boolean isPmReadonlyImpl() {
    if (super.isPmReadonlyImpl()) {
      return true;
    }
    else {
      PmObject ctxt = getPmParent();
      return (ctxt != null &&
              ctxt.isPmReadonly()) ||
             !isPmEnabled();
    }
  }

  // ==== PmTreeNode implementation ==== //

  @Override
  public PmObject getNodeDetailsPm() {
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<PmTreeNode> getPmChildNodes() {
    if (pmChildNodes == null) {
      pmChildNodes = (List<PmTreeNode>)getPmChildNodesImpl();
    }

    return pmChildNodes;
  }

  /**
   * The implementation of child node generation.<br>
   * The default implementation provides all attributes that implement the
   * {@link PmTreeNode} interface.
   * <p>
   * Subclasses may provide their own logic by overriding this method.
   * <p>
   * The result of this method will be cached by the method
   * {@link #getPmChildNodes()}. You may call {@link #clearCachedPmValues()} to
   * clear this cache.
   *
   * @return The set of child nodes.
   */
  @SuppressWarnings("unchecked")
  protected List<? extends PmTreeNode> getPmChildNodesImpl() {
    List<PmTreeNode> list = new ArrayList<PmTreeNode>();
    for (PmAttr<?> a : PmUtil.getPmChildrenOfType(this, PmAttr.class)) {
      if (a instanceof PmTreeNode) {
        list.add((PmTreeNode)a);
      }
    }
    return list.size() > 0
      ? Collections.unmodifiableList(list)
      : Collections.EMPTY_LIST;
  }

  /**
   * The default implementation returns <code>true</code> if there is no child.
   * Otherwise <code>false</code>.
   */
  @Override
  public boolean isPmTreeLeaf() {
    return getPmChildNodes().isEmpty();
  }

  /**
   * Optimization: Cached conversation navigation.
   */
  @Override
  public PmConversation getPmConversation() {
    if (pmConversation == null) {
      pmConversation = super.getPmConversation();
    }
    return pmConversation;
  }


  // ======== meta data ======== //

  @Override
  protected MetaData makeMetaData() {
    return new MetaData();
  }

}

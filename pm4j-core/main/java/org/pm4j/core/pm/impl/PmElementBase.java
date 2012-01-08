package org.pm4j.core.pm.impl;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAspect;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTreeNode;
import org.pm4j.core.pm.PmVisitor;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmEventApi;

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
   * The default implementation provides a unique identifier for this model.
   */
  @Override
  public Serializable getPmKey() {
    return ObjectUtils.identityToString(this);
  }

  @Override
  public void accept(PmVisitor visitor) {
    visitor.visit(this);
  }

  public List<PmAttr<?>> getPmAttributes() {
    return super.zz_getPmAttributes();
  }

  @Override
  public final PmAttr<?> getPmAttribute(String attrName) {
    PmObject pm = findChildPm(attrName);
    if (!(pm instanceof PmAttr)) {
      throw new PmRuntimeException(this, "The found child PM with name '" + attrName +
          "' is not an attribute or null. Found item: " + pm);
    }
    return (PmAttr<?>)pm;
  }

  @Override
  protected void clearCachedPmValues(Set<PmCacheApi.CacheKind> cacheSet) {
    super.clearCachedPmValues(cacheSet);
    // re-create the cached tree child nodes on next getPmChildeNodes call.
    pmChildNodes = null;
  }

  @Override
  public boolean isPmReadonly() {
    PmObject ctxt = getPmParent();
    return getPmMetaData().isReadOnly() ||
           (ctxt != null &&
            ctxt.isPmReadonly()) ||
           !isPmEnabled();
  }

  /**
   * Defines if the attribute validation should be executed on each attribute value set
   * operation. - That's that default setting.
   * <p>
   * It may be useful to switch that behavior off and to postpone validation till
   * command execution.
   * <p>
   * The default implementation returns <code>false</code>.
   *
   * @return <code>true</code> when attribute validation should be done on each set operation.
   */
  protected boolean isValidatingOnSetPmValue() {
    return false;
  }

  @Override
  Serializable getPmContentAspect(PmAspect aspect) {
    switch (aspect) {

      default:  return super.getPmContentAspect(aspect);
    }
  }

  @Override
  void setPmContentAspect(PmAspect aspect, Serializable value) throws PmConverterException {
    PmEventApi.ensureThreadEventSource(this);
    switch (aspect) {
      default: super.setPmContentAspect(aspect, value);
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
    for (PmAttr<?> a : getPmAttributes()) {
      if (a instanceof PmTreeNode) {
        list.add((PmTreeNode)a);
      }
    }
    return list.size() > 0
      ? Collections.unmodifiableList(list)
      : Collections.EMPTY_LIST;
  }

  /**
   * The default implementation returns <code>true</code> if there is at least a single child.
   * Otherwise <code>false</code>.
   */
  @Override
  public boolean isPmTreeLeaf() {
    return getPmChildNodes().size() > 0;
  }

  /**
   * Optimization: Cached session navigation.
   */
  @Override
  public PmConversation getPmConversation() {
    if (pmConversation == null) {
      pmConversation = super.getPmConversation();
    }
    return pmConversation;
  }


  // ======== meta data ======== //

  // XXX olaf: a workaround that keeps the old annotation logic of the first
  //           pm4j project unchanged.
  @Override
  protected <T extends Annotation> void findAnnotationsInPmHierarchy(Class<T> annotationClass, Collection<T> foundAnnotations) {
    if (getPmConversation().getPmDefaults().isElementsInheritAnnotationsOnlyFromSession()) {
      T cfg = findAnnotation(annotationClass);
      if (cfg != null) {
        foundAnnotations.add(cfg);
      }

      if (getPmConversation() != this) {
        getPmConversationImpl().findAnnotationsInPmHierarchy(annotationClass, foundAnnotations);
      }
    }
    else {
      super.findAnnotationsInPmHierarchy(annotationClass, foundAnnotations);
    }
  }

}

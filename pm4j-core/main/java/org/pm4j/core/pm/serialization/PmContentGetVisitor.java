package org.pm4j.core.pm.serialization;

import java.io.Serializable;

import org.pm4j.core.pm.PmAspect;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTreeNode;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.core.pm.impl.PmVisitorAdapter;

public class PmContentGetVisitor extends PmVisitorAdapter {

  public final PmContentContainer contentContainer;
  public final PmContentCfg contentCfg;

  public PmContentGetVisitor() {
    this(new PmContentCfg());
  }

  public PmContentGetVisitor(PmContentCfg contentCfg) {
    this(contentCfg, new PmContentContainer());
  }

  public PmContentGetVisitor(PmContentCfg contentCfg, PmContentContainer contentContainer) {
    assert contentContainer != null;
    assert contentCfg != null;

    this.contentContainer = contentContainer;
    this.contentCfg = contentCfg;
  }

  @Override
  public void visit(PmElement element) {
    super.visit(element);
  }

  @Override
  public void visit(PmAttr<?> attr) {
    // FIXME: how to store List, PmList...?
    storePmAspect(attr, PmAspect.VALUE, true);
    super.visit(attr);
  }

  @Override
  protected void onVisit(PmObject pm) {
    for (PmObject child : PmUtil.getPmChildren(pm)) {
      if ((!contentCfg.isOnlyVisibleItems()) || child.isPmVisible()) {
        PmContentContainer c = contentContainer.addNamedChildContent(child.getPmName());
        child.accept(new PmContentGetVisitor(contentCfg, c));
      }
    }
    super.onVisit(pm);
  }

  @Override
  protected void onVisitPmTreeNode(PmTreeNode pm) {
    // TODO:
    super.onVisitPmTreeNode(pm);
  }

  private void storePmAspect(PmObject pm, PmAspect aspect, boolean transferNull) {
    Serializable value = PmUtil.getPmContentAspect(pm, aspect);
    if (contentCfg.hasAspect(aspect) &&
        (value != null || transferNull)
       ) {
      contentContainer.addAspect(aspect, value);
    }
  }

}

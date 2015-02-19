package org.pm4j.swt.pb;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.pm4j.core.pb.PbFactory;
import org.pm4j.core.pb.PbMatcherMapped;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTreeNode;
import org.pm4j.swt.pb.base.PbCompositeChildToPmBase;
import org.pm4j.swt.util.SWTResourceManager;
import org.pm4j.swt.util.SwtUtil;

/**
 * Provides a details view for the PM provided by {@link PmTreeNode#getNodeDetailsPm()}.
 *
 * @author olaf boede
 */
public class PbTreeNodeDetails extends PbCompositeChildToPmBase<Composite, PmTreeNode> {

  /**
   * The view factory matcher for the details PMs to display.<br>
   * Defines which view will be displayed for the details PM of the selected
   * node. The method {@link PmTreeNode#getNodeDetailsPm()} provides the PM to
   * match.
   */
  private PbMatcherMapped pbMatcher = new PbMatcherMapped();

  /**
   * The background of the details area. Default: {@link SWT#COLOR_LIST_BACKGROUND}.
   */
  private Color backgroundColor = SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND);

  /**
   * The SWT background color mode. Default: {@link SWT#INHERIT_DEFAULT}.
   */
  private int backgroundMode = SWT.INHERIT_DEFAULT;


  /**
   * Creates an empty composite container. The details view will be displayed as
   * a child of this composite.
   * <p>
   * In case of a tree node without a details view (
   * {@link PmTreeNode#getNodeDetailsPm()} returns <code>null</code>), this
   * composite stays empty.
   */
  @Override
  public Composite makeView(Composite parentComponent, PmTreeNode pm) {
    // An intermediate composite, just to satisfy the inner layout manager.
    final Composite detailComposite = new Composite(parentComponent, SWT.BORDER);
    detailComposite.setLayout(new FillLayout());
    detailComposite.setBackground(backgroundColor);
    detailComposite.setBackgroundMode(backgroundMode);

    return detailComposite;
  }


  @Override
  protected PbBinding makeBinding(PmTreeNode pm) {
    return new Binding();
  }

  protected class Binding extends PbCompositeChildToPmBase<Composite, PmTreeNode>.Binding {
    private PmTreeNode currentDetailsPm;

    @Override
    public void bind() {
      super.bind();

      // prevent redisplay overhead in case of re-selection of the current PM.
      if (currentDetailsPm == pm) {
        return;
      }

      if (pm != null) {
        PmObject detailsPm = pm.getNodeDetailsPm();
        PbFactory<?> detailsPbFactory = pbMatcher.findPbFactory(detailsPm);
        if (detailsPbFactory != null) {
          detailsPbFactory.build(view, detailsPm);
        }
      }

      currentDetailsPm = pm;
      SwtUtil.reDisplay(view);
    }

    @Override
    public void unbind() {
      super.unbind();

      for (Control c : view.getChildren()) {
        c.dispose();
      }

      currentDetailsPm = null;
    }
  }

  public PbMatcherMapped getPbMatcher() {
    return pbMatcher;
  }

  public void setPbMatcher(PbMatcherMapped detailBuilderMap) {
    this.pbMatcher = detailBuilderMap;
  }


  public Color getBackgroundColor() {
    return backgroundColor;
  }


  public void setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
  }


  public int getBackgroundMode() {
    return backgroundMode;
  }


  public void setBackgroundMode(int backgroundMode) {
    this.backgroundMode = backgroundMode;
  }

}

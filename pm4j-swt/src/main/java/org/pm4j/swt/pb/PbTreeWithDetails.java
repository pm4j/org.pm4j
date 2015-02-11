package org.pm4j.swt.pb;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.pm4j.core.pb.PbMatcherMapped;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.swt.pb.base.PbCompositeChildToPmBase;

/**
 * A sash form with a tree in one panel and details for the selected node in the other one.
 *
 * @author olaf boede
 */
public class PbTreeWithDetails extends PbCompositeChildToPmBase<SashForm, PmElement> {

  /** Defines the PM to view mapping for the details display area. */
  private PbMatcherMapped detailsBinderMap = new PbMatcherMapped();

  /** Configures the sash form to use orientation {@link SWT#HORIZONTAL}. */
  public PbTreeWithDetails() {
    this(SWT.HORIZONTAL);
  }

  /**
   * @param style The style to apply to the {@link SashForm}.
   */
  public PbTreeWithDetails(int style) {
    super(style);
  }

  @Override
  public SashForm makeView(Composite parentComponent, PmElement pm) {
    return new SashForm(parentComponent, swtStyle);
  }

  @Override
  protected PbBinding makeBinding(PmElement pm) {
    return new Binding();
  }

  protected class Binding extends PbCompositeChildToPmBase<SashForm, PmElement>.Binding {
    PbTreeNodeDetails detailView;
    Composite detailComposite;

    public SashForm makeView(Composite parentComponent, PmElement pm) {
      return new SashForm(parentComponent, swtStyle);
    }

    @Override
    public void bind() {
      super.bind();

      PbTree treePb = new PbTree();
      Tree tree = treePb.makeView(view, pm);

      detailView = new PbTreeNodeDetails();
      detailView.setPbMatcher(detailsBinderMap);
     // TODO: tree nodes are no longer supported
     // detailComposite = detailView.makeView(view, pm);

      tree.addSelectionListener(new SelectionAdapter() {
        @Override public void widgetSelected(SelectionEvent e) {
          if (e.item.getData() instanceof PmObject) {
            PmObject pm = (PmObject) e.item.getData();
            detailView.bind(detailComposite, pm);
          }
        }
      });

      treePb.bind(tree, pm);
      detailView.bind(detailComposite, null);
    }

    @Override
    public void unbind() {
      super.unbind();
      for (Control c : view.getChildren()) {
        c.dispose();
      }
    }

  }


  public PbMatcherMapped getDetailsBinderMap() {
    return detailsBinderMap;
  }

}

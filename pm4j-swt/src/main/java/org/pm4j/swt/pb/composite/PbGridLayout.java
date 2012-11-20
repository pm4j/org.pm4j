package org.pm4j.swt.pb.composite;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.pm4j.core.pb.PbFactory;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.swt.pb.base.PbCompositeChildToPmBase;

public class PbGridLayout <PM extends PmElement> extends PbCompositeBase<Composite, PM>  {

  public GridLayout gridLayout;

  private PbFactory<?> rowBuilder = new PbCompositeChildToPmBase<Widget, PmAttr<?>>() {
    @Override
    public Widget makeView(Composite parent, PmAttr<?> pm) {
      return buildRow(parent, pm, getPbMatcher().findPbFactory(pm));
    }
  };

  /**
   * Generates a standard two column grid.
   */
  public PbGridLayout() {
    gridLayout = new GridLayout(2, false);
    gridLayout.marginWidth = 10;
  }

  /**
   * Binds all attributes.
   */
  public static class AllAttrs extends PbGridLayout<PmElement> {
    @Override
    protected PbBinding makeBinding(PmElement pm) {
      return new Binding() {
        @Override
        public void bind() {
          super.bind();
          for (Control c : view.getChildren()) {
            c.dispose();
          }
          buildRows(view, PmUtil.getPmChildrenOfType(pm, PmAttr.class).toArray(new PmAttr[]{}));
        }
      };
    }
  }


  @Override
  public Composite makeView(Composite parentComponent, PM pm) {
    Composite view;
    if (parentComponent.getLayout() instanceof FillLayout) {
      // FIXME: quick hack for grids in fill-layout composites...
      Composite gridFrame = new Composite(parentComponent, swtStyle);
      view = new Composite(gridFrame, SWT.BORDER | SWT.FILL);
    }
    else {
      view = new Composite(parentComponent, swtStyle);
      view.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    view.setLayout(this.gridLayout);

    return view;
  }

  public Widget buildRow(Composite c, PmAttr<?> pm, PbFactory<?> valueViewBuilder) {
    Widget valueView = null;
    if (valueViewBuilder != null) {
      buildLabel(c, pm);
      valueView = (Widget)valueViewBuilder.build(c, pm);
      // TODO: a style hint could help here to get the specific display style
      if (valueView instanceof Control) {
        ((Control) valueView).setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true));
      }
    }
    return valueView;
  }

  public void buildRows(Composite c, PmAttr<?>... attrs) {
    for (PmAttr<?> a : attrs) {
      rowBuilder.build(c, a);
    }
  }

  public void buildRows(Composite c, Collection<PmAttr<?>> attrs) {
    for (PmAttr<?> a : attrs) {
      rowBuilder.build(c, a);
    }
  }

}

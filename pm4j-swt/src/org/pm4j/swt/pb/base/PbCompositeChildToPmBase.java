package org.pm4j.swt.pb.base;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.pm4j.core.pm.PmObject;

/**
 * Base class for binder that uses a kind of {@link Widget} that may be added to a {@link Composite}
 * parent.
 * <p>
 * Some {@link Widget}s such as {@link Menu} and {@link MenuItem} accept other parent types.
 * For these types the {@link PbWidgetToPmBase} class should be used.
 *
 * @param <VIEW> The kind of SWT widget to bind.
 * @param <PM>   The {@link PresentationModel} to bind to the widget.
 */
public abstract class PbCompositeChildToPmBase <VIEW extends Widget, PM extends PmObject>
      extends PbWidgetToPmBase<VIEW, Composite, PM> {

  public PbCompositeChildToPmBase() {
    super();
  }

  public PbCompositeChildToPmBase(int style) {
    super(style);
  }
}
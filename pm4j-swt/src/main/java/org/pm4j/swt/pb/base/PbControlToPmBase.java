package org.pm4j.swt.pb.base;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;

/**
 * Convenience base class for bindings between SWT {@link Control}s and {@link PmAttr}s.
 *  
 * @author olaf boede
 *
 * @param <VIEW> Type of the {@link Control} to bind.
 * @param <PM>      Type of the {@link PmAttr} to bind.
 */
public abstract class PbControlToPmBase <VIEW extends Control, PM extends PmObject>
    extends PbCompositeChildToPmBase<VIEW, PM> {

  public PbControlToPmBase(int style) {
    super(style);
  }

  @Override
  protected PbBinding makeBinding(PM pm) {
    return new Binding();
  }
  
  public class Binding extends PbWidgetToPmBase<VIEW, Composite, PM>.Binding {

    @Override
    protected void onPmEnablementChange(PmEvent event) {
      view.setEnabled(pm.isPmEnabled());
    }

    @Override
    protected void onPmTooltipChange(PmEvent event) {
      // XXX: onPmTitleChange -> toolTipText update!? What does "Title" mean? 
      // (In Swing toolTips are typically associated with a "ShortDescription", 
      // opposed to a "Name" or "LongDescription". E.g. see javax.swing.Action)
      view.setToolTipText(pm.getPmTooltip());
    }

  }  
}

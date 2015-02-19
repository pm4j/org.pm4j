package org.pm4j.swt.pb.composite;

import org.eclipse.swt.widgets.Composite;
import org.pm4j.core.pm.PmObject;
import org.pm4j.swt.pb.base.PbCompositeChildToPmBase;

public abstract class PbDialogBase<C extends Composite, PM extends PmObject>
  extends PbCompositeChildToPmBase<C, PM> {

  @SuppressWarnings("unchecked")
  @Override
  public void bind(Object view, PmObject pm) {
    super.bind(view, pm);
    ((C)view).pack();
  }

}

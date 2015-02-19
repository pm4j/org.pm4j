package org.pm4j.swt.pb;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Dialog;
import org.pm4j.core.pb.PbFactoryBase;
import org.pm4j.core.pm.PmObject;

/**
 * Base class for {@link Window} and {@link Dialog} bindings.
 *
 * @param <VIEW>
 * @param <PM>
 */
public abstract class PbWindowBase<VIEW extends Window, PM extends PmObject>
    extends PbFactoryBase<VIEW, Object, PM>
{
  public class Binding extends PbFactoryBase<VIEW, Object, PM>.Binding
                       implements DisposeListener {

    @Override
    public void bind() {
      super.bind();
      // Special handling for dialog windows:
      // To be able to register a dispose listener, we need to ensure that the shell gets created
      // before the blocking 'open()' method gets called.
      if (view.getShell() == null) {
        view.create();
      }

      view.getShell().addDisposeListener(this);
    }

    @Override
    public void unbind() {
      super.unbind();
      view.getShell().removeDisposeListener(this);
    }

    @Override
    public void widgetDisposed(DisposeEvent e) {
      unbind();
    }

  }

}


package org.pm4j.swt.pb.base;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.pm4j.core.pb.PbFactoryBase;
import org.pm4j.core.pm.PmObject;

/**
 * Base class that supports binding of {@link PresentationModel}s to SWT
 * {@link Widget}s.
 * <p>
 * <b>Live time control:</b><br>
 * The bound widget gets a reference to the {@link PresentationModel} in its
 * <code>data</code> section. This ensures that the {@link PresentationModel}
 * exists as long as the widget exists.
 * <p>
 * <b>Event binding support:</b><br>
 * The <code>registerXyz...Listener()</code> methods add bi-directional listeners
 * 
 * TODOC: ...
 * 
 * 
 * @author olaf boede
 * 
 * @param <VIEW>
 *          The kind of SWT widget to bind.
 * @param <PARENT_VIEW_CTXT>
 *          The kind of widget parent. Used for the type safe {@link #makeView(Object, PresentationModel)} signature.
 * @param <PM>
 *          The {@link PresentationModel} to bind to the widget.
 */
public abstract class PbWidgetToPmBase<VIEW extends Widget, PARENT_VIEW_CTXT, PM extends PmObject>
    extends PbFactoryBase<VIEW, PARENT_VIEW_CTXT, PM>
{

  public int swtStyle;
  
  /**
   * Initializes the {@link #swtStyle} with {@link SWT#NONE}.
   */
  public PbWidgetToPmBase() {
    this(SWT.NONE);
  }
  
  public PbWidgetToPmBase(int style) {
    this.swtStyle = style;
  }
  
  // -- subclass helper --

  @Override
  protected void bindImpl(final VIEW view, final PM pm) {
  }

  @Override
  protected PbBinding makeBinding(PM pm) {
    return new Binding();
  }
  
  /**
   * Base class for listeners that may react on SWT- and PM-events
   * to communicate changes between both sides.
   *
   * @author olaf boede
   *
   * @param <W> The widget type.
   * @param <P> The PM type.
   */
  public class Binding extends PbFactoryBase<VIEW, Composite, PM>.Binding
                       implements DisposeListener {

    @Override
    public void widgetDisposed(DisposeEvent e) {
      unbind();
    }
    
    @Override
    public void bind() {
      super.bind();
      view.addDisposeListener(this);
    }
    
    @Override
    public void unbind() {
      super.unbind();
      view.removeDisposeListener(this);
    }
  }
  
}


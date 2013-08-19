package org.pm4j.swt.pb.base;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.pm4j.core.pm.PmObject;

/**
 * Base class for jFace views.
 * It provides a bridge between the 'worlds' SWT and jFace.
 *
 * @author olaf boede
 *
 * @param <V> The jFace viewer type.
 * @param <W> The SWT widget type.
 * @param <PM> The PM type to bind.
 */
public abstract class PbViewerToPmBase <V extends Viewer, W extends Widget, PM extends PmObject>
  extends PbCompositeChildToPmBase<W, PM>
{

  /**
   * Key for the jFace {@link Viewer} instance that gets stored in data property
   * of its corresponding {@link Widget}.
   */
  public static final String VIEW_KEY = "pmViewer";

  public V makeViewer(Composite parentCtxt, PM pm) {
    V v = makeViewerImpl(parentCtxt, pm);
    v.getControl().setData(VIEW_KEY, v);
    return v;
  }

  /**
   * jFace specific type safe {@link #makeView(Composite, PresentationModel)} signature.
   * 
   * @param parentCtxt The SWT parent of the viewer to create.
   * @param pm The PM to bind.
   */
  protected abstract V makeViewerImpl(Composite parentCtxt, PM pm);
  
  @SuppressWarnings("unchecked")
  public V buildViewer(Composite parentCtxt, PmObject pm) {
    V v = makeViewer(parentCtxt, (PM)pm);
    bind(v.getControl(), pm);
    return v;
  }

  @Override @SuppressWarnings("unchecked")
  public W build(Object parentCtxt, PmObject pm) {
    V v = buildViewer((Composite)parentCtxt, (PM)pm);
    return (W) v.getControl();
  }

  @Override @SuppressWarnings("unchecked")
  public W makeView(Composite parentComponent, PM pm) {
    return (W) makeViewer(parentComponent, pm).getControl();
  }
  
  /**
   * Calls the method {@link #makeViewerBinding(Viewer, PmObject)} which provides
   * the full information set for implementing classes.
   */
  protected PbBinding makeBinding(PM pm) {
    return new Binding();
  }
  
  /**
   * A binding that also provides access to the bound viewer instance.
   */
  public class Binding extends PbWidgetToPmBase<W, Composite, PmObject>.Binding {
    public V viewer;
    
    @SuppressWarnings("unchecked")
    @Override
    public void bind() {
      super.bind();
      this.viewer = (V) view.getData(VIEW_KEY);
    }
  }

}

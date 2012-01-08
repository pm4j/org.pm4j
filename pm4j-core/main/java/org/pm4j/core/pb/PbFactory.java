package org.pm4j.core.pb;

import org.pm4j.core.pm.PmObject;

/**
 * May create and bind a control for a given PM.
 * <p>
 * There may exist multiple binder classes for a single kind of PM.<br>
 * Example: A command PM may be bound by a buttonBuilder to a button control.
 * For menu scenarios there may be menuItemBuilder that may bind the control to
 * a menu item.
 *
 * @author olaf boede
 *
 * @param <VIEW>
 */
public interface PbFactory<VIEW> {

  /**
   * Defines the event that triggers the transfer of the entered value from the
   * control to the PM.
   */
  public static enum ValueUpdateEvent {
    FOCUS_LOST,
    MODIFY,
    NEVER
  }
  
  /**
   * Creates the view and binds it to the given presentation model.
   *
   * @param parentViewCtxt
   *          The parent control of the widget to create. E.g. a panel.
   * @param pm
   *          The presentation model to bind to a view.
   *
   * @return The created and bound UI view.
   */
  VIEW build(Object parentViewCtxt, PmObject pm);

  /**
   * Binds an existing UI object (something like a widget, composit, window) to
   * a presentation model.
   * <p>
   * After that, all UI data, titles, tooltips, options etc. reflect the state
   * of the bound PM.
   *
   * @param view
   *          The UI control (a widget, composit, window ...) to bind.
   * @param pm
   *          The PM that will be displayed by the UI control.
   */
  //FIXME: Shouldn't this be: bind(COMP view, PM pm);
  // Remark from olaf: I always had a lot of trouble to use generics in external parameter sets.
  //    Example: A best-match algorithm may provide this base interface. You get a lot of 
  //             trouble if you add the found binder to a typed binder collection or if you
  //             want to use the method signature with a concrete view-pm type combination.
  //             To reduce the trouble for the user of this API, I prefer to limit generics
  //             to the subclass implementation interface.
  //             In this class the VIEW type is only declared for the build() method.
  //             This helps in some situations to prevent casts and to allow inline-usage.
  void bind(Object view, PmObject pm);

}

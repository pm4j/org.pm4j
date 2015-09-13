package org.pm4j.core.pb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmEventApi;

// TODO: check if we can get rid of the duplicate bind concept:
//  The PbFactory supports a procedural bind() method (but no unbind!)
//  The Binding class supports both, but only by declaring a factory and
//  at least an anonymous binder class...
// Tasks split:
// PbFactory - Factory for Bindings
// PbFactory.Binding - Binding between view and PM
/**
 * Implementation base for view binder.<br>
 * Supports stateful and stateless binder classes.
 * <p>
 * Stateless binders have to implement
 * <ul>
 *   <li>{@link #makeView(Object, PmObject)} and</li>
 *   <li>{@link #bindImpl(Object, PmObject)}.</li>
 * </ul>
 * <p>
 * Stateful binders need to implement the following additional tasks:
 * <ul>
 *   <li>the binder needs to implement {@link Cloneable} and</li>
 *   <li>{@link #isBinderStateful()} should return true.</li>
 * </ul>
 *
 * @param <VIEW>
 *          The view component type.
 * @param <PARENT_VIEW_CTXT>
 *          The type of the parent view component.
 * @param <PM>
 *          The type of presentation model to bind to the view.
 *
 * @author olaf boede
 */
public abstract class PbFactoryBase<VIEW, PARENT_VIEW_CTXT, PM extends PmObject>
  implements PbFactory<VIEW> {

  public int boomerangEventExcludeMask = PmEvent.VALUE_CHANGE;

  /**
   * Associates a view to its current PM binding.
   * <p>
   * The registered bindings should be de-registrered from this map using
   * {@link #setBinding(Object, PbBinding)} as soon as it is clear that the
   * binding may be released.
   * <p>
   * If that was not done, the association will be cleared when the UI control
   * gets garbage collected.
   */
  private static final Map<Object, PbBinding> viewToBindingMap =
            Collections.synchronizedMap(new WeakHashMap<Object , PbBinding>());

  /**
   * An optional instance that provides the algorithm to apply style classes
   * to the view.
   */
  private List<PbViewStyler> viewStylerSet = new ArrayList<PbViewStyler>();

  public void addViewStyler(PbViewStyler s) {
    viewStylerSet.add(s);
  }

  /**
   * Creates the view component that will be used for the
   * {@link #bind(Object, PmObject)} operation.
   *
   * @param parent
   *          The parent of the view component to create.
   * @param pm
   *          The presentation model that will be bound to the component.
   * @return The view component.
   */
  public abstract VIEW makeView(PARENT_VIEW_CTXT parent, PM pm);

  @Override @SuppressWarnings("unchecked")
  public void bind(Object viewObject, PmObject pm) {
    VIEW view = (VIEW) viewObject;

    // A control may only bound to a single PM. We have to unbind it before
    // we can bind it to another PM.
    PbBinding l = getBinding(view);
    if (l != null) {
      l.unbind();
    }

    if (pm != null) {
      PbBinding binding = makeBinding((PM) pm);
      ((Binding)binding).pm = (PM)pm;
      ((Binding)binding).view = view;

      // All PM messages will be passed to the binder instance.
      // The listener exists as long as the PM is in memory.
      PmEventApi.addWeakPmEventListener(pm, PmEvent.ALL, binding.getPmEventListener());

      setBinding(view, binding);

      binding.bind();
      // Let the subclasses perform additional things.
      bindImpl(view, (PM)pm);

      binding.applyPmState();
    }
    else {
      setBinding(view, null);
    }
  }

  protected void bindImpl(final VIEW comp, final PM pm) {
  }

  /**
   * Subclasses may provide here a listener for PM-Events.<br>
   * This listener will be added and removed by the binding base class to the
   * PM.
   *
   * @param pm
   *          The PM to bind.
   * @return The PM-event listener. May be <code>null</code> if there is nothing
   *         to observe.
   */
  protected PbBinding makeBinding(PM pm) {
    return new Binding();
  }

  /**
   * Performs the {@link #makeView(Object, PmObject)} and {@link #bind(Object, PmObject)}
   * steps.
   */
  @Override @SuppressWarnings("unchecked")
  public VIEW build(Object parentViewCtxt, PmObject pm) {
    VIEW v = null;
    try {
      v = makeView((PARENT_VIEW_CTXT)parentViewCtxt, (PM)pm);
    }
    catch (ClassCastException e) {
      throw new PmRuntimeException(pm, "PM to view binding problem: The view class seems to expect another PM class.\n" +
          "Please check the PM to view binding configuration.\n", e);
    }
    bind(v, pm);
    return v;
  }

  protected void setBinding(Object view, PbBinding binding) {
    if (binding != null) {
      viewToBindingMap.put(view, binding);
    }
    else {
      viewToBindingMap.remove(view);
    }
  }

  protected PbBinding getBinding(Object view) {
    return viewToBindingMap.get(view);
  }

  /**
   * Finds the PM that is bound to a view.
   *
   * @param <PM> The requested type of PM.
   * @param view The view to find the bound PM for.
   * @return The bound PM or <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  public static <PM extends PmObject> PM getBoundPm(Object view) {
    PbBinding b = viewToBindingMap.get(view);
    return b != null
              ? (PM)b.getPm()
              : null;
  }

  /**
   * Interface for classes that organize a concrete binding of a view to a {@link PmObject}.
   *
   * @author olaf boede
   */
  public interface PbBinding {

    /**
     * Returns the bound PmObject
     *
     * @return The bound presentation model instance.
     */
    PmObject getPm();

    /**
     * Applies the current PM state on the bound (view) component.
     */
    void applyPmState();

    /**
     * Binds the (view) component and the PmObject
     */
    void bind();

    /**
     * Unbinds the (view) component and the PmObject
     */
    void unbind();

    /**
     * @return the PmEventListener taking care of events fired by the
     *         PmObject.
     */
    PmEventListener getPmEventListener();

  }


  /**
   * Base class for listeners that may react on SWT- and PM-events
   * to communicate changes between both sides.
   */
  public class Binding
            extends PbOnEventMethodCallDispatcher
            implements PbBinding, PmEventListener {

    public PM pm;
    public VIEW view;

    /**
     * A reference to the set of listeners to be un-registered.<br>
     * Optimized for size, since most UI controls will reference only a single listener.
     */
    private PmEventListener[] pmListenerArray;

    /**
     * Handles presentation model events.
     */
    @Override
    public void handleEvent(PmEvent event) {
      // XXX olaf: the onPmXXX methods are really convenient to use, but
      //           this construction costs some performance...
      //           Is this really an issue?
      //           Idea for better performing and convenient call back structure wanted!
      int changeMask = event.getChangeMask();

      if (event.getSource() == view) {
        changeMask &= (PmEvent.ALL ^ boomerangEventExcludeMask);
      }

      dispatchToOnEventMethodCalls(event, changeMask);
    }

    protected void onPmStyleClassChange(PmEvent event) {
      for (PbViewStyler s : viewStylerSet) {
        s.applyStyle(view, pm);
      }
    }

    /**
     * Applies the current PM state on the bound component by firing an event that notifies.
     */
    @Override
    public void applyPmState() {
      handleEvent(new PmEvent(this, pm, PmEvent.ALL_CHANGE_EVENTS));
    }

    /**
     * Adds the {@link PmEventListener} to the bound PM and registers it for unbinding
     * within in the main listener.
     *
     * @param listener The listener to add.
     */
    public void addPmEventListener(PmEventListener listener) {
      if (pmListenerArray == null) {
        makeArray(listener);
      }
      else {
        int oldLen = pmListenerArray.length;
        pmListenerArray = Arrays.copyOf(pmListenerArray, oldLen+1);
        pmListenerArray[oldLen] = listener;
      }
      PmEventApi.addWeakPmEventListener(pm, PmEvent.ALL, listener);
    }

    @Override
    public void bind() {
    }

    @Override
    public void unbind() {
      PmEventApi.removePmEventListener(pm, this);
      if (pmListenerArray != null) {
        for (PmEventListener l : pmListenerArray) {
          PmEventApi.removePmEventListener(pm, l);
        }
      }
      pmListenerArray = null;
      setBinding(view, null);
    }

    private void makeArray(PmEventListener itemOne) {
      pmListenerArray = new PmEventListener[1];
      pmListenerArray[0] = itemOne;
    }

    @Override
    public PmObject getPm() {
      return pm;
    }

    @Override
    public PmEventListener getPmEventListener() {
      return this;
    }

  }

}

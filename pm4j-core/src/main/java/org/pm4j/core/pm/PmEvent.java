package org.pm4j.core.pm;

import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.pm4j.core.pm.PmEventListener.PostProcessor;
import org.pm4j.core.pm.api.PmEventApi;


/**
 * Informs about presentation model events.
 *
 * @author olaf boede
 */
public class PmEvent extends EventObject {

  private static final long serialVersionUID = 4673929102728746424L;

  public static final int VALUE_CHANGE = 1;
  public static final int TITLE_CHANGE = 1 << 1;
  public static final int TOOLTIP_CHANGE = 1 << 2;
  public static final int VISIBILITY_CHANGE = 1 << 3;
  public static final int ENABLEMENT_CHANGE = 1 << 4;
  public static final int OPTIONSET_CHANGE = 1 << 5;
  /**
   * Will be fired whenever an attribute value changes to from the initial value
   * or back again to the initial value.
   */
  public static final int VALUE_CHANGED_STATE_CHANGE = 1 << 6;
  public static final int VALIDATION_STATE_CHANGE = 1 << 7;
  public static final int STYLECLASS_CHANGE = 1 << 8;
  public static final int SELECTION_CHANGE = 1 << 9;
  public static final int SORT_ORDER_CHANGE = 1 << 10;
  public static final int FILTER_CHANGE = 1 << 11;
  public static final int EXEC_COMMAND = 1 << 12;

  /**
   * Indicator for an event that fired because of re-loaded data.<br>
   * An example: After saving a record, its current state gets reloaded from the DB.
   * In this case the application may fire {@link #ALL_CHANGE_EVENTS} combined with this
   * {@link #RELOAD} flag.<br>
   * This allows to react differently in each case.
   */
  public static final int RELOAD = 1 << 30;

  /** Indicator for an event that gets propagated to the parent hierarchy. */
  public static final int IS_EVENT_PROPAGATION = 1 << 31;

  /**
   * This event gets fired on a PM tree (part) when its content was replaces.<br>
   * E.g. the backing bean of a PmBean was exchanged.
   */
  public static final int ALL_CHANGE_EVENTS =
    VALUE_CHANGE | TITLE_CHANGE | TOOLTIP_CHANGE |
    VISIBILITY_CHANGE | ENABLEMENT_CHANGE |
    OPTIONSET_CHANGE | VALUE_CHANGED_STATE_CHANGE | VALIDATION_STATE_CHANGE |
    STYLECLASS_CHANGE | SELECTION_CHANGE | SORT_ORDER_CHANGE | FILTER_CHANGE;

  public static final int ALL = ALL_CHANGE_EVENTS | EXEC_COMMAND;

  /**
   * Types of value change event.
   */
  public enum ValueChangeKind {
    /** The specific value change is not defined. */
    UNKNOWN,
    /** A value was (ex-)changed. */
    VALUE,
    /** One or more items where added to value (E.g. a list value). */
    ADD_ITEM,
    /** One or more items where deleted from a value. */
    DELETE_ITEM,
    /** The value was re-loaded from the backend. */
    RELOAD;

    /**
     * @return <code>true</code> if the value may be replaced by this value change.
     */
    public boolean isContentReplacingChangeKind() {
      return this == UNKNOWN || this == VALUE || this == RELOAD;
    }
  }

  /**
   * The {@link PmObject} that is related to the event.
   * May be <code>null</code> if the event is not related to a particular PM.
   */
  public final PmObject pm;

  /**
   * A bit mask for the change kind.
   */
  private final int changeMask;

  /**
   * In case of a value changing event, this field can be used to specify the kind of value change.
   */
  private final ValueChangeKind valueChangeKind;

  /**
   * The set of listeners that requested a call back after the current event processing phase.
   */
  private Map<PostProcessor<?>, Object> postProcessorToPayloadMap = Collections.emptyMap();


  /**
   * An event constructor without event source specification.
   * <p>
   * In some configurations (rich client UIs) the event source will be read from
   * the thread local storage.
   *
   * @param pm
   *          the PM this event is related to.
   * @param changeMask
   *          A bit mask for the change kind.
   */
  public PmEvent(PmObject pm, int changeMask) {
    this(PmEventApi.ensureThreadEventSource(pm), pm, changeMask);
  }

  /**
   * An event constructor without event source specification.
   * <p>
   * In some configurations (rich client UIs) the event source will be read from
   * the thread local storage.
   *
   * @param pm
   *          the PM this event is related to.
   * @param changeMask
   *          A bit mask for the change kind.
   * @param valueChange
   *          A value change kind specification.
   */
  public PmEvent(PmObject pm, int changeMask, ValueChangeKind valueChange) {
    this(PmEventApi.ensureThreadEventSource(pm), pm, changeMask, valueChange);
  }

  /**
   * @param eventSource
   *          The control or command that triggered the change.<br>
   *          Should not be <code>null</code>.
   * @param changeKind
   *          A bit mask for the change kind.
   */
  public PmEvent(Object eventSource, PmObject pm, int changeKind) {
    super(eventSource);

    this.changeMask = changeKind;
    this.pm = pm;
    this.valueChangeKind = ValueChangeKind.UNKNOWN;
  }

  /**
   * @param eventSource
   *          The control or command that triggered the change.<br>
   *          Should not be <code>null</code>.
   * @param changeMask
   *          A bit mask for the change kind.
   * @param valueChange
   *          A value change kind specification.
   */
  public PmEvent(Object eventSource, PmObject pm, int changeMask, ValueChangeKind valueChange) {
    super(eventSource);

    this.changeMask = changeMask;
    this.pm = pm;
    this.valueChangeKind = valueChange;
  }

  /**
   * @return <code>true</code> if the event is derived from an original event
   *         just for propagating it within the event hierarchy.<br>
   *         <code>false</code> if the event is an active event related to the
   *         linked {@link #pm}.
   */
  public boolean isPropagationEvent() {
    return hasEventMaskBits(IS_EVENT_PROPAGATION);
  }

  /**
   * @return <code>true</code> if the event is caused by an initialization. This
   *         event kind is also generated if <code>setPmBean</code> was called
   *         and all values of the related PM sub-tree are exchanged.
   */
  public boolean isInitializationEvent() {
    return (changeMask & ALL_CHANGE_EVENTS) == ALL_CHANGE_EVENTS;
  }

  /**
   * @return <code>true</code> if the event is caused by re-loading a bean behind the PM.<br>
   *         This event kind is generated for all components of a PM tree if
   *         <code>PmBean.reloadPmBean</code> exchanges the content behind the tree.<br>
   *         That usually happens on saving and re-displaying a record.
   */
  public boolean isReloadEvent() {
    return hasEventMaskBits(RELOAD);
  }


  /**
   * <code>true</code> if the event {@link #changeMask} contains all bits of the
   * given parameter.
   *
   * @param eventMask
   *          The bits to be found in the {@link #changeMask}.
   * @return <code>true</code> if the event {@link #changeMask} contains all
   *         bits of the given parameter.
   */
  public boolean hasEventMaskBits(int eventMask) {
    return (changeMask & eventMask) == eventMask;
  }

  /**
   * @return The {@link PmObject} that is related to the event.
   */
  public PmObject getPm() {
    return pm;
  }

  /**
   * @return In case of a value changing event, this field can be used to specify the kind of value change.
   */
  public ValueChangeKind getValueChangeKind() {
    return valueChangeKind;
  }

  /**
   * @return The bit-mask that indicates the set of influenced PM aspects.
   */
  public int getChangeMask() {
    return changeMask;
  }

  /**
   * Registers a listener that gets informed after finishing this event processing phase.
   *
   * @param listener the listener to call after this processing phase.
   * @param payload the payload data to be added to the event gets passed to the post
   */
  public void addPostProcessingListener(PostProcessor<?> listener, Object payload) {
    if (postProcessorToPayloadMap.isEmpty()) {
      postProcessorToPayloadMap = new HashMap<PostProcessor<?>, Object>();
    }
    postProcessorToPayloadMap.put(listener, payload);
  }

  /**
   * @return the set of registered post event listeners and their payload data.
   */
  public Map<PostProcessor<?>, Object> getPostProcessorToPayloadMap() {
    return postProcessorToPayloadMap;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[source=" + source + ", changeMask=" + getChangeMask() + "]";
  }

}

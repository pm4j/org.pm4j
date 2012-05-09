package org.pm4j.core.pm;

import java.util.EventObject;


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
  public static final int EXEC_COMMAND = 1 << 10;

  /** Indicator for an event that gets propagated to the parent hierarchy. */
  public static final int IS_EVENT_PROPAGATION = 1 << 15;

  public static final int ALL_CHANGE_EVENTS =
    VALUE_CHANGE | TITLE_CHANGE | TOOLTIP_CHANGE |
    VISIBILITY_CHANGE | ENABLEMENT_CHANGE |
    OPTIONSET_CHANGE | VALUE_CHANGED_STATE_CHANGE | VALIDATION_STATE_CHANGE |
    STYLECLASS_CHANGE | SELECTION_CHANGE;

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
    RELOAD,
    /** Sort order change */
    SORT_ORDER,
    /** Item Filter definition change */
    FILTER;

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
    return (changeMask & IS_EVENT_PROPAGATION) != 0;
  }

  /**
   * @return <code>true</code> if the event is caused by an initialization. This
   *         event kind is also generated if <code>setPmBean</code> was called
   *         and all values of the related PM sub-tree are exchanged.
   */
  public boolean isInitializationEvent() {
    return changeMask == ALL_CHANGE_EVENTS;
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

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[source=" + source + ", changeMask=" + getChangeMask() + "]";
  }

}

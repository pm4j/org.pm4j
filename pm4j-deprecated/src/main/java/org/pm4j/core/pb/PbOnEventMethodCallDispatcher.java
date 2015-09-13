package org.pm4j.core.pb;

import org.pm4j.core.pm.PmEvent;

/**
 * Provides convenience 'onPmXyzChange' methods that are sometimes easier
 * to use than event listeners instances.
 *
 * @author olaf boede
 */
public class PbOnEventMethodCallDispatcher {

  /**
   * Calls the related on...() methods. The set of methods to call is determined
   * by the given <code>eventMask</code> parameter.
   *
   * @param event The event to propagate.
   * @param eventMask Defines the set of on-methods to be called.
   */
  protected void dispatchToOnEventMethodCalls(PmEvent event, int eventMask) {
    // XXX olaf: the onPmXXX methods are really convenient to use, but
    //           this construction costs some performance...
    //           Is this really an issue?
    //           Idea for better performing and convenient call back structure wanted!
    if ((eventMask & PmEvent.VALUE_CHANGE) != 0) {
      onPmValueChange(event);
    }
    if ((eventMask & PmEvent.TITLE_CHANGE) != 0) {
      onPmTitleChange(event);
    }
    if ((eventMask & PmEvent.TOOLTIP_CHANGE) != 0) {
      onPmTooltipChange(event);
    }
    if ((eventMask & PmEvent.VISIBILITY_CHANGE) != 0) {
      onPmVisibilityChange(event);
    }
    if ((eventMask & PmEvent.ENABLEMENT_CHANGE) != 0) {
      onPmEnablementChange(event);
    }
    if ((eventMask & PmEvent.OPTIONSET_CHANGE) != 0) {
      onPmOptionSetChange(event);
    }
    if ((eventMask & PmEvent.VALIDATION_STATE_CHANGE) != 0) {
      onPmValidationStateChange(event);
    }
    if ((eventMask & (PmEvent.STYLECLASS_CHANGE |
        // FIXME olaf: a quick hack for the missing change detection in the PM layer.
                      PmEvent.VALIDATION_STATE_CHANGE)
         ) != 0) {
      onPmStyleClassChange(event);
    }
  }

  /**
   * Is called whenever an event with the flag {@link PmEvent#ENABLEMENT_CHANGE}
   * was fired for this PM.
   *
   * @param event The fired event.
   */
  protected void onPmEnablementChange(PmEvent event) {
  }

  /**
   * Is called whenever an event with the flag {@link PmEvent#VALUE_CHANGE}
   * was fired for this PM.
   *
   * @param event The fired event.
   */
  protected void onPmValueChange(PmEvent event) {
  }

  /**
   * Is called whenever an event with the flag {@link PmEvent#TITLE_CHANGE}
   * was fired for this PM.
   *
   * @param event The fired event.
   */
  protected void onPmTitleChange(PmEvent event) {
  }

  /**
   * Is called whenever an event with the flag {@link PmEvent#TOOLTIP_CHANGE}
   * was fired for this PM.
   *
   * @param event The fired event.
   */
  protected void onPmTooltipChange(PmEvent event) {
  }

  /**
   * Is called whenever an event with the flag {@link PmEvent#VISIBILITY_CHANGE}
   * was fired for this PM.
   *
   * @param event The fired event.
   */
  protected void onPmVisibilityChange(PmEvent event) {
  }

  protected void onPmOptionSetChange(PmEvent event) {
  }

  protected void onPmValidationStateChange(PmEvent event) {
  }

  protected void onPmStyleClassChange(PmEvent event) {
  }

}

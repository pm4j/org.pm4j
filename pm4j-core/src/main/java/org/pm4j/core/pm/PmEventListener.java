package org.pm4j.core.pm;

import org.pm4j.core.pm.api.PmEventApi;

/**
 * A {@link PmEvent} listener.<br>
 * {@link PmEventListener}s may be registered for particular event kinds using
 * the <code>add</code> methods available in {@link PmEventApi}.
 */
public interface PmEventListener {
  /**
   * Handles the event.
   *
   * @param event
   *          The event to handle.
   */
  void handleEvent(PmEvent event);

  /**
   * A handler may implement this interface to get a call before the event is handled.
   * <p>
   * This may be used to store some relevant state information for a post
   * processing step.<br>
   * See: {@link PmEvent#addPostProcessingListener(PostProcessor, Object)}.
   * <p>
   * TODO: Document limitation. Only fix PM structure objects receive this event.
   *
   */
  public interface WithPreprocessCallback extends PmEventListener {

    /**
     * Gets called before the event gets handled.
     *
     * @param event the event to handle.
     */
    void preProcess(PmEvent event);
  }

  /**
   * A post processor that may be registered during event handling
   * to ask for a call back after completion of the the regular event
   * processing.
   * <p>
   * See: {@link PmEvent#addPostProcessingListener(PostProcessor, Object)}.
   */
  public interface PostProcessor<T> {

    /**
     * Gets called after completion of the regular event processing.
     *
     * @param event
     *          the event that was successfully completed.
     * @param postProcessPayload
     *          the optional payload information that was passed to the
     *          {@link PmEvent#addPostProcessingListener(PostProcessor, Object)}
     *          call.
     */
    void postProcess(PmEvent event, T postProcessPayload);
  }
}
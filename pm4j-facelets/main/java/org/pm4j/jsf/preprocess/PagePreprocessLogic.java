package org.pm4j.jsf.preprocess;

import org.pm4j.web.UrlInfo;

/**
 * Interface for page preprocessing logic that can be used within the
 * {@link PreprocessPhaseListener}.
 *
 * @author olaf boede
 */
public interface PagePreprocessLogic {

  /**
   * Defines session state specific request states.
   */
  public enum RequestKind {
    /** A normal request within an existing http session. */
    NORMAL(false),
    /** An initial request that started a new session. */
    SESSION_START(true),
    /**
     * A (post) request from a page that has no longer a corresponding
     * server side session.
     * <p>
     * A new session has been created for this request when the
     * {@link PagePreprocessLogic} will be applied.
     */
    SESSION_TIMEOUT(true);

    /**
     * @return <code>true</code> if the request kind represents a servlet
     *         session start scenario.
     */
    public boolean isSessionStart() {
      return sessionStart;
    }

    private boolean sessionStart;

    private RequestKind(boolean sessionStart) {
      this.sessionStart = sessionStart;
    }
  }

  /**
   * May execute some specific preprocessing logic.<br>
   * May return some navigation information, the application should go to.
   *
   * @param requestUrlInfo
   *          The URL of the current request.
   * @param requestKind
   *          Indicates the current session state.
   * @return An optional redirect target. <code>null</code> if no redirection
   *         should be done.<br>
   *         TODO: document startup logic with null or Info with null url...
   */
	RedirectInfo preprocess(UrlInfo requestUrlInfo, RequestKind requestKind);

}

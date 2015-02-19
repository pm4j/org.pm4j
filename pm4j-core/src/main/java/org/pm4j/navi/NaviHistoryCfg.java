package org.pm4j.navi;


/**
 * Configuration data for navigation management.
 *
 * @author olaf boede
 */
public class NaviHistoryCfg {

  /** Default name of the request parameter that holds the navigation history version number. */
  public static final String DEFAULT_NAVI_HISTORY_VERSION_PARAM_NAME = "nh";

  /**
   * A strategy that defines how session ID's will be generated.
   */
  public static enum SessionIdGenStrategy {
    /**
     * Generates the first navigation session with an ID <code>0</code>.
     */
    SEQUENTIAL,
    /**
     * Generates a first navigation session ID based on the current system time.
     */
    TIME_BASED;

    public int getFirstId() {
      switch (this) {
        case SEQUENTIAL: return 0;

        // The first session id is based on the int-fraction of the current time.
        // Every 50 milliseconds a new value will be used.
        // That allows a quasi-unique session number generation over a time period of 3.4 years.
        //
        // Only the positive range is used to prevent the space consuming '-' characters
        // within the ID.
        case TIME_BASED: return Math.abs((int)(System.currentTimeMillis()/50));

        default: throw new IllegalArgumentException();
      }
    }
  }

  /**
   * Defines which path is relevant for the navigation history.<br>
   * This method may filter out ajax calls that are irrelevant for the
   * navigation history.
   */
  public static interface HistoryPathMatcher {
    /**
     * @param path
     *          The path to check.
     * @return <code>true</code> when the given path should be considered by the
     *         navigation management.
     */
    boolean isNaviHistoryPath(String path);
  }

  /**
   * A fix configured start link that may be provided as a fall back link when
   * no previous history item is available.
   */
  private NaviLink startLink;


  /**
   * The strategy for navigation session ID generation.
   */
  private SessionIdGenStrategy sessionIdGenStrategy = SessionIdGenStrategy.TIME_BASED;

  /**
   * Defines the pages that are relevant for the navigation history.<br>
   * The default matcher says that every page is relevant.
   */
  private HistoryPathMatcher historyPathMatcher = new HistoryPathMatcher() {
    @Override
    public boolean isNaviHistoryPath(String path) {
      return true;
    }
  };

  /**
   * Name of the parameter that provides navigation version information.
   */
  private String versionParamName = DEFAULT_NAVI_HISTORY_VERSION_PARAM_NAME;

  /**
   * Live time [ms] of unused navigation information without any 'ping'
   * signal from external clients.
   * <p>
   * Defaults to 5 minutes.
   */
  private int unusedLinkLiveTimeMs = 5*60*1000;

  /**
   * Defines if a change of the page position should generate a new history item
   * or if the new position should simply be applied to the current history
   * item.
   * <p>
   * Default value: <code>false</code>.
   */
  private boolean newNaviVersionOnPagePosChange;

  /**
   * @param unusedLinkLiveTimeSec Unused live time im seconds.
   */
  public void setUnusedLinkLiveTimeSec(int unusedLinkLiveTimeSec) {
    this.unusedLinkLiveTimeMs = unusedLinkLiveTimeSec*1000;
  }

  // -- getter/setter --

  public NaviLink getStartLink() {
    return startLink;
  }

  public void setStartLink(NaviLink startLink) {
    this.startLink = startLink;
  }

  public SessionIdGenStrategy getSessionIdGenStrategy() {
    return sessionIdGenStrategy;
  }

  public void setSessionIdGenStrategy(SessionIdGenStrategy sessionIdGenStrategy) {
    this.sessionIdGenStrategy = sessionIdGenStrategy;
  }

  public String getVersionParamName() {
    return versionParamName;
  }

  public void setVersionParamName(String versionParamName) {
    this.versionParamName = versionParamName;
  }

  public int getUnusedLinkLiveTimeMs() {
    return unusedLinkLiveTimeMs;
  }

  public void setUnusedLinkLiveTimeMs(int unusedLinkLiveTimeMs) {
    this.unusedLinkLiveTimeMs = unusedLinkLiveTimeMs;
  }

  public HistoryPathMatcher getHistoryPathMatcher() {
    return historyPathMatcher;
  }

  public void setHistoryPathMatcher(HistoryPathMatcher historyPathMatcher) {
    assert historyPathMatcher != null;
    this.historyPathMatcher = historyPathMatcher;
  }

  public boolean isNewNaviVersionOnPagePosChange() {
    return newNaviVersionOnPagePosChange;
  }

  public void setNewNaviVersionOnPagePosChange(boolean newNaviVersionOnPagePosChange) {
    this.newNaviVersionOnPagePosChange = newNaviVersionOnPagePosChange;
  }

}

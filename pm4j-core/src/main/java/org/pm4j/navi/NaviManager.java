package org.pm4j.navi;


// TODO: comment!!!
public interface NaviManager {

  // TODO: comment!!!
  enum NaviMode {
    NORMAL (""),
    POPUP ("_popup_");

    public String getPrefix() {
      return prefix;
    }

    private String prefix;

    private NaviMode(String prefix) {
      this.prefix = prefix;
    }

  }

  /**
   * Gets called whenever a navigation to the provided link was observed.
   *
   * @param link
   *          The observed navigation target.
   * @param versionString
   *          The navigation version string. Format: 'sessionId.versionId'.
   * @return The resulting navigation history.
   */
  NaviHistory onNavigateTo(NaviLink link, String versionString);

  /**
   * Gets called whenever a navigation to the provided link was observed.
   *
   * @param link
   *          The observed navigation target.
   * @param versionString
   *          The navigation version string. Format: 'sessionId.versionId'.
   * @param naviMode
   *          Defines the navigation handling strategy.
   * @return The resulting navigation history.
   */
  NaviHistory onNavigateTo(NaviLink link, String versionString, NaviMode naviMode);

  /**
   * @return The navigation history of the specified session id.<br>
   *         <code>null</code> when there is no matching session available.
   */
  NaviHistory getCurrentHistoryOfSession(String sessionId);

  /**
   * @param sessionId
   *          Identifier for the session to ask for the versionId.
   * @param versionId
   *          Identifier (counter) of the version within the session.
   * @return The corresponding history or <code>null</code> if there is no
   *         history for the given version string.
   */
  NaviHistory findHistory(String sessionId, String versionId);

  /**
   * @return The navigation configuration.
   */
  NaviHistoryCfg getNaviCfg();

}

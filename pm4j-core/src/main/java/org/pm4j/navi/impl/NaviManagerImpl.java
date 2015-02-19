package org.pm4j.navi.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.navi.NaviHistory;
import org.pm4j.navi.NaviHistoryCfg;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.NaviManager;

/**
 * Responsible for managing a set of client sessions (e.g. browser tab sessions).
 * <p>
 * It knows about the set of active sessions.
 *
 * @author olaf boede
 */
public class NaviManagerImpl implements NaviManager {

  private static final Logger LOG = LoggerFactory.getLogger(NaviManagerImpl.class);

  /**
   * The navigation configuration.
   */
  private NaviHistoryCfg naviCfg = new NaviHistoryCfg();

  /**
   * The navigation start link. May be the configured startLink or just
   * the first navigation move target.
   */
  private NaviLink startLink;

  /**
   * The set of conversation sessions.
   */
  private Map<String, NaviSessionImpl> idToSessionMap = new LinkedHashMap<String, NaviSessionImpl>();

  /**
   * The unique id that will be used when the next navigation will be created.<br>
   * Is initially randomized to minimize the likelihood of matches with stored browser links.
   */
  private int nextSessionId;

  /**
   * The last time [ms] this manager checked for histories and sessions that are
   * out of date.
   */
  private long lastUnusedCheckTime;

  /**
   * Constructor for application use.
   *
   * @param naviCfg
   */
  public NaviManagerImpl(NaviHistoryCfg naviCfg) {
    assert naviCfg != null;

    this.naviCfg = naviCfg;
    this.nextSessionId = naviCfg.getSessionIdGenStrategy().getFirstId();
  }

  /**
   * @return A session id that is unique within the scope of this navigation manager.
   */
  /* package */ String newSessionId() {
    String id;

    do {
      id = NaviUtil.intToId(nextSessionId);

      // Big values may exist in case of time based start session ID's
      this.nextSessionId = (nextSessionId == Integer.MAX_VALUE)
                            ? 0
                            : nextSessionId + 1;

    // Make sure that the ID is really not in use:
    } while (idToSessionMap.containsKey(id));

    return id;
  }

  @Override
  public NaviHistory onNavigateTo(NaviLink link, String versionString) {
    return onNavigateTo(link, versionString, NaviMode.NORMAL);
  }

  @Override
  public NaviHistory onNavigateTo(NaviLink link, String versionString, NaviMode naviMode) {
    String sessionId = null;
    String versionId = null;

    if (StringUtils.isNotEmpty(versionString)) {
      String[] sarr = NaviUtil.splitVersionString(versionString);
      sessionId = sarr[0];
      versionId = sarr[1];
    }

    synchronized(this) {
      NaviSessionImpl session = idToSessionMap.get(sessionId);

      if (session == null) {
        if (sessionId != null) {
          session = new NaviSessionImpl(this, sessionId, versionId, link);

          if (LOG.isInfoEnabled())
            LOG.info("Received request for a dead navigation session. VersionString: '" +
                NaviUtil.makeVersionString(sessionId, versionId) +
                "'. A new navigation session with the same ID will be generated " +
                "to be able to handle request from the 'dead' page somehow.");
        }
        else {
          session = new NaviSessionImpl(this);

          if (LOG.isDebugEnabled())
            LOG.debug("New navigation session with ID=" + session.getSessionId() +
                " started on navigation to '" + link + "'.");
        }

        idToSessionMap.put(session.getSessionId(), session);

        if (LOG.isTraceEnabled()) LOG.trace(getTraceString());
      }

      NaviSessionImpl forkedSession = session.onNavigateTo((NaviLinkImpl)link, versionId, naviMode);

      if (forkedSession != null) {
        idToSessionMap.put(forkedSession.getSessionId(), forkedSession);
        session = forkedSession;

        // XXX olaf: Sollte das Logger nicht in der SessionImpl gemacht werden (bei der Fork Herstellung)
        if (LOG.isDebugEnabled())
          LOG.debug("Created a navigation session fork: " + forkedSession +
                    " for " + naviCfg.getVersionParamName() + "=" + versionString + "" +
                    " caused by link: " + link +
                    "\nTrace: " + getTraceString());

        if (LOG.isTraceEnabled()) LOG.trace(getTraceString());
      }

      removeUnused();

      if (LOG.isDebugEnabled()) {
        if (! ( ObjectUtils.equals(sessionId, session.getSessionId()) &&
                ObjectUtils.equals(versionId, session.getVersion()) )
           ) {
          LOG.debug("Changed to history: " + session.getNaviHistory());
        }
        else if (LOG.isTraceEnabled()) {
          LOG.trace("Navigation history not changed: " + session.getNaviHistory());
        }
      }

      return session.getNaviHistory();
    }
  }

  @Override
  public NaviHistoryCfg getNaviCfg() {
    return naviCfg;
  }

  /**
   * @return The start link of the navigation within this navigation manager.
   */
  /* package */ NaviLink getStartLink() {
    if (startLink == null) {
      startLink = getNaviCfg().getStartLink();
    }
    return startLink;
  }

  @Override
  public NaviHistory getCurrentHistoryOfSession(String sessionId) {
    NaviSessionImpl session = idToSessionMap.get(sessionId);
    return session != null
            ? session.getNaviHistory()
            : null;
  }

  @Override
  public NaviHistory findHistory(String sessionId, String versionId) {
    NaviSessionImpl session = idToSessionMap.get(sessionId);
    return session != null
            ? session.findHistoryVersion(versionId)
            : null;
  }

  /**
   * @param sessionId
   *          Identifier of the session go get.
   * @return The found session or <code>null</code> if there is no session for
   *         the given ID.
   */
  /* package */ NaviSessionImpl findNaviSession(String sessionId) {
    return idToSessionMap.get(sessionId);
  }

  /**
   * @return A report about the state of the navigation manager.
   */
  public String getTraceString() {
    StringBuilder sb = new StringBuilder(8000);

    sb.append("NaviManager State:");

    for (NaviSessionImpl s : idToSessionMap.values()) {
      sb.append("\n").append(s.getTraceString());
    }

    return sb.toString();
  }

  /**
   * Gets called on each {@link #onNavigateTo(NaviLink, String, String)}. Checks for unused
   * items only in an interval of {@link NaviHistoryCfg#getUnusedLinkLiveTimeMs()}.
   */
  private void removeUnused() {
    long now = System.currentTimeMillis();
    long timeSinceLastUnusedCheck = now - naviCfg.getUnusedLinkLiveTimeMs();
    if (lastUnusedCheckTime < timeSinceLastUnusedCheck) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Checking for unused histories. Previous check was " + (now - lastUnusedCheckTime) + "ms ago.");
      }

      lastUnusedCheckTime = now;
      for (Iterator<NaviSessionImpl> i = idToSessionMap.values().iterator(); i.hasNext(); ) {
        NaviSessionImpl s = i.next();
        if (! s.removeUnusedAndCheckIfActive(timeSinceLastUnusedCheck)) {
          i.remove();
          if (LOG.isDebugEnabled()) {
            LOG.debug("Removed unused navigation session: " + s);
          }
        }
      }
    }
  }

}


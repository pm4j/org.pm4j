package org.pm4j.navi.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.navi.NaviHistoryCfg;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.NaviManager;
import org.pm4j.navi.NaviManager.NaviMode;

/**
 * Implementation of the navigation session (also known as browser tab sessions).
 * <p>
 * It knows about its active navigation history versions.
 *
 * @author olaf boede
 */
class NaviSessionImpl {

  private static final Log LOG = LogFactory.getLog(NaviSessionImpl.class);

  /** The instance that is responsible for handling this session. */
  private final NaviManagerImpl naviManager;

  /** Identifier of this session. Is unique within its {@link NaviManager} scope. */
  private final String sessionId;

  /**
   * The set of navigation versions this session keeps track of.
   */
  private LinkedHashMap<String, NaviHistoryImpl> versionToHistoryMap = new LinkedHashMap<String, NaviHistoryImpl>();

  /**
   * The set of navigation versions that is moved out because of detected navigation loops.<br>
   * They will be garbage collected as soon as it's sure that they are no longer used by client
   * views.
   */
  private LinkedHashMap<String, NaviHistoryImpl> loopVersionToHistoryMap = new LinkedHashMap<String, NaviHistoryImpl>();

  /**
   * The currently active (latest) history version.
   */
  private NaviHistoryImpl currentHistory;

  /** The current version ID. */
  private String currentVersion = "-1";

  /**
   * Properties that are stored within the scope of this navigation session.
   */
  private HashMap<String, Serializable> conversationProperties;

  /**
   * Creates a new empty session instance.
   *
   * @param naviManager
   *          The instance that is responsible for handling this session.
   */
  public NaviSessionImpl(NaviManagerImpl naviManager) {
    this.naviManager = naviManager;
    this.sessionId = naviManager.newSessionId();
  }

  /**
   * Creates a new empty session that may be used to start a session that uses
   * the identifiers of a died session.
   * <p>
   * This allows to prevent continuous re-generation of navigation sessions in
   * case of a re-appeared old browser page that polls the server.
   *
   * @param naviManager
   *          The instance that is responsible for handling this session.
   * @param sessionId
   *          Id of this navigation session.
   * @param newLastItem
   *          The start history item to be used.
   */
  public NaviSessionImpl(NaviManagerImpl naviManager, String sessionId, String versionId, NaviLink newLastItem) {
    this.naviManager = naviManager;
    this.sessionId = sessionId;

    addVersionWithPrevHistory(new NaviHistoryImpl(this, newLastItem, versionId), null);
  }

  /**
   * Creates a new session with a new item.
   *
   * @param naviManager
   * @param newLastItem
   * @param inheritedNaviScopeProps
   *          The initial set of navigation scoped properties. May be
   *          <code>null</code>.
   */
  public NaviSessionImpl(NaviManagerImpl naviManager, NaviLink newLastItem, Map<String, Object> inheritedNaviScopeProps) {
    this.naviManager = naviManager;
    this.sessionId = naviManager.newSessionId();

    addVersionWithPrevScopeProps(
        new NaviHistoryImpl(this, newLastItem), inheritedNaviScopeProps, true /* clone navi scope params */);
  }

  /**
   * Creates a session fork based on a defined history and a new navigation
   * link.
   *
   * @param baseSession
   *          The predecessor session instance.
   * @param baseHistory
   *          The history of the predecessor session, the new session is based
   *          on.
   * @param newLastItem
   *          The new last navigation item for the new session.
   */
  @SuppressWarnings("unchecked")
  private NaviSessionImpl(NaviSessionImpl baseSession, NaviHistoryImpl baseHistory, NaviLink newLastItem) {
    this(baseSession.naviManager);
    if (baseSession.conversationProperties != null) {
      this.conversationProperties = (HashMap<String, Serializable>)
        SerializationUtils.clone(baseSession.conversationProperties);
    }

    // FIXME olaf: naviHistory property transfer from base session not yet implemented for this case.
    //             simple approach: just collect the properties from the naviLinks of the baseHistory
    //             items that are used to construct the new history....
    Iterator<NaviLink> linkIter = baseHistory.getItemIterator();
    Map<String, Object> baseNaviScopeProps = new TreeMap<String, Object>();
    while (linkIter.hasNext()) {
      NaviLink l = linkIter.next();
      baseNaviScopeProps.putAll(((NaviLinkImpl)l).getNaviScopeParams());
      // prevent adoption of props that come from old navi loops.
      if (l.isLinkToSamePage(newLastItem)) {
        break;
      }
    }
    // Don't forget the properties, set directly on the history without navigation:
    baseNaviScopeProps.putAll(baseHistory.getNaviScopeProperties());

    addVersionWithPrevScopeProps(
        new NaviHistoryImpl(this, baseHistory, newLastItem),
        baseNaviScopeProps, true /* clone navi scope params */);
  }

  /**
   * @return The navigation configuration.
   */
  public NaviHistoryCfg getNaviCfg() {
    return naviManager.getNaviCfg();
  }

  /**
   * @return The start link of the navigation within this session.
   */
  public NaviLink getStartLink() {
    return naviManager.getStartLink();
  }

  /**
   * @param link
   *          The link to navigate to.
   * @param versionId
   *          Version of this request.
   * @param naviMode
   *          If set to <code>true</code> a new session will be forked, even if
   *          not required by the current version-link configuration.
   * @return An new {@link NaviSessionImpl} instance when a new navigation session
   *         fork was created.<br>
   *         <code>null</code> when the navigation did not cause a session fork.
   */
  public NaviSessionImpl onNavigateTo(NaviLinkImpl link, String versionId, NaviMode naviMode) {
    assert link != null;
    assert versionId != null;

    NaviSessionImpl forkedSession = null;

    if (versionToHistoryMap.isEmpty()) {
      // first item of history...
      addVersionWithPrevHistory(new NaviHistoryImpl(this, link), null);
    }
    else {

      if ((naviMode == NaviMode.NORMAL) && versionId.equals(currentVersion)) {
        // Simplest case: call matches the last navigation move version.

        boolean createNewVersion = true;
        currentHistory.ping();

        NaviLink lastHistoryItem = currentHistory.getCurrentLink();
        if (link.isLinkToSamePage(lastHistoryItem) &&
            link.getNaviScopeParams().isEmpty()) {

          // same page, no new navi-scope data: the current version is fine!
          createNewVersion = false;

          // pos on page changed?
          if (! StringUtils.equalsIgnoreCase(link.getPosOnPage(), lastHistoryItem.getPosOnPage())) {

            if (naviManager.getNaviCfg().isNewNaviVersionOnPagePosChange()) {
              // new history entry according to the history configuration.
              createNewVersion = true;
            }
            else {
              // just modify the position of the last history item.
              currentHistory.setPosOnPage(link.getPosOnPage());
            }
          }
        }

        if (createNewVersion) {
          NaviHistoryImpl newHistory = new NaviHistoryImpl(this, currentHistory, link);

          NaviHistoryImpl prevHistory = currentHistory;
          // In case of a back navigation (indicated by a shorter link list),
          // the unused 'loop' items are moved out.
          int newLinkCount = newHistory.getSize();
          if (newLinkCount <= currentHistory.getSize()) {
            Iterator<NaviHistoryImpl> iter = versionToHistoryMap.values().iterator();
            while (iter.hasNext()) {
              NaviHistoryImpl h = iter.next();
              if (h.getSize() >= newLinkCount) {
                iter.remove();
                loopVersionToHistoryMap.put(h.getVersion(), h);
              }
              if (h.getSize() <= newLinkCount) {
                prevHistory = h;
              }
            }
          }

          addVersionWithPrevHistory(newHistory, prevHistory);
        }
      }
      else {
        // Call does not match the last navigation move version.
        // A new session fork has to be created.
        NaviHistoryImpl baseHistory = findHistoryVersion(versionId);

        if (baseHistory != null) {
          // It's old, but still in use from somewhere:
          baseHistory.ping();

          if (naviMode == NaviMode.POPUP) {
            forkedSession = new NaviSessionImpl(naviManager, link, baseHistory.getNaviScopeProperties());
          }
          else {
            NaviSessionImpl existingFork = baseHistory.getForkedSession();

            if ((existingFork != null) &&
                isSamePageRef(link, existingFork.getNaviHistory().getCurrentLink())) {
              // If the forked session is located on the requested link, it will be
              // re-used.
              NaviHistoryImpl h = existingFork.getNaviHistory();
              h.ping();
              // ensure that new navigation scope properties get transferred to the
              // re-used session history.
              h.setNaviScopeProperties(h.getNaviScopeProperties(), link.getNaviScopeParams(),
                  false /* don't clone the values, because they are already in the scope of the forked session. */);

              forkedSession = existingFork;
            }
            else {
              forkedSession = new NaviSessionImpl(this, baseHistory, link);
              baseHistory.setForkedSession(forkedSession);
            }
          }
       }
       else {
          // no history match:
          LOG.warn("No history found for navigation version=" +
              NaviUtil.makeVersionString(sessionId, versionId) +
              " in link " + link +
              ".\nThe link might have been stored as a bookmark of browser favorite." +
              "\nA new navigation session with an empty history will be created.");

          // TODO: Hier wäre ein guter Platz für die Verwaltung von 'alten'
          //       Historien die per ping immer wieder zu neuen Sessions führen...
          forkedSession = new NaviSessionImpl(naviManager);
          forkedSession.onNavigateTo(link, versionId, NaviMode.NORMAL);
        }
      }
    }

    return forkedSession;
  }

  /**
   * @return An identifier that is unique within its {@link NaviManager} scope.
   */
  public String getSessionId() {
    return sessionId;
  }

  /**
   * @return The current navigation call version (counter) of this session.
   */
  public String getVersion() {
    return currentVersion;
  }

  /**
   * @return The current navigation history of this session.
   */
  public NaviHistoryImpl getNaviHistory() {
    return currentHistory;
  }

  /**
   * @return The manager that is responsible for navigation sessions handling.
   */
  public NaviManagerImpl getNaviManager() {
    return naviManager;
  }

  /**
   * Checks if any move-out history is unused since the given time stamp.<br>
   * Removes the unused items.
   *
   * @param minLastVisitTime
   *          The accepted lastVisitTime for the oldest history.
   * @return <code>true</code> if at least one history item was used since the
   *         given time. <code>false</code> if all last-usage-times of all
   *         history items are older than the given time.
   */
  public boolean removeUnusedAndCheckIfActive(long minLastVisitTime) {
    boolean traceEnabled = LOG.isTraceEnabled();

    for (Iterator<NaviHistoryImpl> i = loopVersionToHistoryMap.values().iterator(); i.hasNext(); ) {
      NaviHistoryImpl h = i.next();
      if (h.getLastVisitTime() < minLastVisitTime) {
        i.remove();
        if (traceEnabled) {
          LOG.trace("Unused history removed: " + h);
        }
      }
    }

    boolean isActive = currentHistory.getLastVisitTime() >= minLastVisitTime;

    if (!isActive) {
      for (Iterator<NaviHistoryImpl> i = versionToHistoryMap.values().iterator(); i.hasNext();) {
        NaviHistoryImpl h = i.next();
        if (h.getLastVisitTime() >= minLastVisitTime) {
          isActive = true;
          break;
        }
      }
    }

    return isActive;
  }

  /**
   * Find the matching history version within the active version
   * history or within the moved-out loop version set.
   *
   * @param versionId
   *          versionId of the history to find.
   * @return The found history or <code>null</code> if there is no matching
   *         instance.
   */
  public NaviHistoryImpl findHistoryVersion(String version) {
    NaviHistoryImpl history = versionToHistoryMap.get(version);
    if (history == null) {
      history = loopVersionToHistoryMap.get(version);
    }
    return history;
  }

  /**
   * Provides a conversation scoped property.
   * <p>
   * A conversation is internally bound to a navigation session (may represent a
   * browser tab session).
   *
   * @param propName
   *          Unique identifier of the property.
   * @return The property value or <code>null</code> when no value was
   *         registered for the given name.
   */
  public Serializable getConversationProperty(String propName) {
    return (conversationProperties != null)
              ? conversationProperties.get(propName)
              : null;
  }

  /**
   * Sets a conversation scoped property.
   * <p>
   * A conversation is internally bound to a navigation session (may represent a
   * browser tab session).
   *
   * @param propName
   *          Unique identifier of the property.
   * @param value
   *          The property value.
   */
  public void setConversationProperty(String propName, Serializable value) {
    if (conversationProperties == null) {
      conversationProperties = new HashMap<String, Serializable>();
    }
    conversationProperties.put(propName, value);
  }

  /**
   * @return A report about the state of the session.
   */
  public String getTraceString() {
    StringBuilder sb = new StringBuilder(1000);
    sb.append("NaviSession ").append(getSessionId())
      .append("\n current version: ").append(currentVersion)
      .append("\n  Set of active histories:");
    for (NaviHistoryImpl h : versionToHistoryMap.values()) {
      sb.append("\n\t").append(h.getTraceString());
    }
    if (loopVersionToHistoryMap.size() > 0) {
      sb.append("\n  Set of moved-out loop histories:");
      for (NaviHistoryImpl h : loopVersionToHistoryMap.values()) {
        sb.append("\n\t").append(h.getTraceString());
      }
    }

    return sb.toString();
  }

  @Override
  public String toString() {
    return NaviUtil.makeVersionString(sessionId, currentVersion);
  }

  // -- internal helper implementation --

  /**
   * Checks according to the navi configuration for the same page or same pagePos.
   */
  private boolean isSamePageRef(NaviLink l1, NaviLink l2) {
    return l1.isLinkToSamePage(l2) &&
           (!naviManager.getNaviCfg().isNewNaviVersionOnPagePosChange() ||
            StringUtils.equals(l1.getPosOnPage(), l2.getPosOnPage()));
  }

  /** Maintains all related attributes consistently. */
  private void addVersionWithPrevHistory(NaviHistoryImpl history, NaviHistoryImpl prevHistory) {
    @SuppressWarnings("unchecked")
    Map<String, Object> inheritedNaviScopeProps = (prevHistory != null)
              ? prevHistory.getNaviScopeProperties()
              : (Map<String, Object>)Collections.EMPTY_MAP;

    boolean cloneNaviProps = prevHistory != null &&
                             ! history.getSessionId().equals(prevHistory.getSessionId());
    addVersionWithPrevScopeProps(history, inheritedNaviScopeProps, cloneNaviProps);
  }

  /** Maintains all related attributes consistently. */
  private void addVersionWithPrevScopeProps(
      NaviHistoryImpl history,
      Map<String, Object> inheritedNaviScopeProps,
      boolean shouldCloneNaviProps) {
    versionToHistoryMap.put(history.getVersion(), history);
    currentHistory = history;
    currentVersion = history.getVersion();

    history.setNaviScopeProperties(
        inheritedNaviScopeProps,
        ((NaviLinkImpl)history.getCurrentLink()).getNaviScopeParams(),
        shouldCloneNaviProps);
  }

}

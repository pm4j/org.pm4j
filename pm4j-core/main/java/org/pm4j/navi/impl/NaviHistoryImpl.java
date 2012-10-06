package org.pm4j.navi.impl;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import org.pm4j.common.util.collection.ListUtil;
import org.pm4j.navi.NaviHistory;
import org.pm4j.navi.NaviHistoryCfg;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.NaviManager;

/**
 * A simple navigation history that maintains a simple list of navigation items.
 *
 * @author olaf boede
 */
public class NaviHistoryImpl implements NaviHistory {

  /** The current navigation history item set. */
  private NaviLink[] items;

  /**
   * The navigation manager configuration.
   */
  private final NaviManagerImpl naviManager;

  /**
   * Id of navigation session that handles this history.<br>
   * No reference to prevent live time issues.
   */
  private final String sessionId;

  /** The navigation move 'counter' within the session. */
  private final String version;

  /** Time stamp of the last observed usage of this history. */
  private long lastVisitTime = System.currentTimeMillis();

  /**
   * A navigation history fork that was created, based on this history.<br>
   * Implemented as weak reference, since the session might have a short live time...
   */
  private WeakReference<NaviSessionImpl> forkedSessionRef;

  /**
   * A set of properties that is valid for the navigation scope of this history.
   */
  private Map<String, Object> naviScopeProperties = Collections.emptyMap();

  /**
   * Creates a history with a single item.
   *
   * @param naviSession
   *          The navigation session that handles this history.
   * @param naviLink
   *          The single navigation item of this history.
   */
  public NaviHistoryImpl(NaviSessionImpl naviSession, NaviLink naviLink) {
    this(naviSession, naviLink, NaviUtil.nextId(naviSession.getVersion()));
  }

  /**
   * Creates an empty history with a single item.<br>
   * Asks for a specific version to start with.
   *
   * @param naviSession
   *          The navigation session that handles this history.
   * @param naviLink
   *          The single navigation item of this history.
   * @param version
   *          The version ID to be used for this history.
   */
  public NaviHistoryImpl(NaviSessionImpl naviSession, NaviLink naviLink, String version) {
    this.naviManager = naviSession.getNaviManager();
    this.sessionId = naviSession.getSessionId();
    this.version = version;
    this.items = ListUtil.toArray(naviLink);
  }

  /**
   * Creates a history based on an existing one.
   * <p>
   * TODO: document back navigation case!!!
   *
   * @param naviSession The owning navigation session.
   * @param baseHistory The history predecessor, used to make the new one.
   * @param newLastItem The new last history item.
   */
  public NaviHistoryImpl(NaviSessionImpl naviSession, NaviHistoryImpl baseHistory, NaviLink newLastItem) {
    this.naviManager = naviSession.getNaviManager();
    this.sessionId = naviSession.getSessionId();
    this.version = NaviUtil.nextId(naviSession.getVersion());

    NaviLink[] baseItems = (baseHistory).items;
    int historyPos = findPosInList(baseItems, newLastItem);
    NaviLink[] newItems;

    if (historyPos != -1) {
      // Back-navigation: Link found in the existing history.
      newItems = Arrays.copyOfRange(baseItems, 0, historyPos+1);
      newItems[historyPos] = newLastItem;
    }
    else {
      // Forward navigation:
      newItems = Arrays.copyOf(baseItems, baseItems.length+1);
      newItems[baseItems.length] = newLastItem;
    }

    this.items = newItems;
  }

  @Override
  public NaviLink getCurrentLink() {
    NaviLink[] itemListRef = items;
    return (itemListRef.length > 0)
              ? itemListRef[itemListRef.length-1]
              : null;
  }

  @Override
  public NaviLink getPrevLink() {
    NaviLink[] itemListRef = items;
    return (itemListRef.length >= 2)
        ? itemListRef[itemListRef.length-2]
        : null;
  }

  @Override
  public NaviLink getFirstLink() {
    NaviLink[] itemListRef = items;
    return itemListRef.length > 0
        ? itemListRef[0]
        : null;
  }

  @Override
  public NaviLink getPrevOrStartLink(NaviLink... linksToSkip) {
    @SuppressWarnings("unchecked")
    Set<NaviLink> skipSet = linksToSkip.length > 0
                      ? new HashSet<NaviLink>(Arrays.asList(linksToSkip))
                      : Collections.EMPTY_SET;

    NaviLink[] itemListRef = items;
    for (int i=itemListRef.length-2; i>=0; --i) {
      NaviLink l = itemListRef[i];
      if (! skipSet.contains(l)) {
        return l;
      }
    }

    // no (non-skip) item in history
    return naviManager.getStartLink();
  }

  @Override
  public NaviLink findLinkBefore(NaviLink l) {
    NaviLink predecessor = null;
    for (NaviLink item : items) {
      if (item.equals(l)) {
        return predecessor;
      }
      else {
        predecessor = item;
      }
    }
    // link not found in list.
    // TODO olaf: should we throw an exception in this case?
    return null;
  }

  @Override
  public boolean containsLinkToPage(NaviLink naviLink) {
    for (Iterator<NaviLink> iter = getItemIterator(); iter.hasNext(); ) {
      if (iter.next().isLinkToSamePage(naviLink)) {
        return true;
      }
    }
    // link not found in history:
    return false;
  }

  @Override
  public Iterator<NaviLink> getItemIterator() {
    return Arrays.asList(items).iterator();
  }

  @Override
  public Iterator<NaviLink> getReverseIterator() {
    return new Iterator<NaviLink>() {
      // copied reference to prevent concurrent modification issues on item list replacements.
      private NaviLink[] itemListRef = items;
      private int posIdx = itemListRef.length;
      @Override public boolean hasNext() {
        return posIdx > 1;
      }
      @Override public NaviLink next() {
        if (posIdx < 1) {
          throw new NoSuchElementException();
        }
        return itemListRef[--posIdx];
      }
      @Override public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public int getSize() {
    return items.length;
  }

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public String getVersionString() {
    return NaviUtil.makeVersionString(sessionId, version);
  }

  @Override
  public String getSessionId() {
    return sessionId;
  }

  /**
   * @return An optional session that was forked, based on this history state.
   */
  /* package */ NaviSessionImpl getForkedSession() {
    return forkedSessionRef != null
            ? forkedSessionRef.get()
            : null;
  }

  /**
   * Defines 'the' fork that was created, based on this history state.
   * <p>
   * Navigation calls that refer to this history will be handled by the forked
   * session.
   *
   * @param forkedSession
   *          The forked session.
   */
  /* package */ void setForkedSession(NaviSessionImpl forkedSession) {
    forkedSessionRef = new WeakReference<NaviSessionImpl>(forkedSession);
  }

  @Override
  public NaviManager getNaviManager() {
    return naviManager;
  }

  @Override
  public NaviHistoryCfg getNaviCfg() {
    return naviManager.getNaviCfg();
  }

  @Override
  public void ping() {
    lastVisitTime = System.currentTimeMillis();
  }

  @Override
  public void setPosOnPage(String pos) {
    NaviLink[] newItems = Arrays.copyOf(items, items.length);

    int lastPos = newItems.length-1;
    if (lastPos < 0) {
      throw new NaviRuntimeException("History is empty. Unable to set page position to '" + pos + "'.");
    }
    NaviLinkImpl newLink = new NaviLinkImpl((NaviLinkImpl)newItems[lastPos], pos);

    newItems[lastPos] = newLink;
    items = newItems;
  }

  @Override @SuppressWarnings("unchecked")
  public <T extends Serializable> T  getConversationProperty(String propName) {
    NaviSessionImpl s =  naviManager.findNaviSession(sessionId);
    if (s == null) {
      throw new NaviRuntimeException("Session for history not found: " + this);
    }
    return (T) s.getConversationProperty(propName);
  }

  @Override
  public void setConversationProperty(String propName, Serializable value) {
    NaviSessionImpl s =  naviManager.findNaviSession(sessionId);
    if (s == null) {
      throw new NaviRuntimeException("Session for history not found: " + this);
    }
    s.setConversationProperty(propName, value);
  }

  @Override @SuppressWarnings("unchecked")
  public <T extends Object> T getNaviScopeProperty(String propName) {
    return (T) naviScopeProperties.get(propName);
  }

  @Override @SuppressWarnings("unchecked")
  public <T extends Object> T getNaviScopeProperty(String propName, T defaultValue) {
    T value = (T) naviScopeProperties.get(propName);
    return value != null
            ? value
            : defaultValue;
  };

  @Override
  public void setNaviScopeProperty(String propName, Object value) {
    NaviUtil.checkNaviScopeParam(propName, value);

    if (naviScopeProperties.size() == 0) {
      naviScopeProperties = new TreeMap<String, Object>();
    }
    naviScopeProperties.put(propName, value);
  }

  /**
   * Provides the navigation loop scoped property set of this history.
   *
   * @return The set. Never <code>null</code>.
   */
  /* package */ Map<String, Object> getNaviScopeProperties() {
    return naviScopeProperties;
  }

  /**
   * Defines the set of navigation scope properties.
   *
   * @param prevHistoryProps
   *          The set of properties, inherited from the previous history.
   * @param newLinkProps
   *          The set of properties, passed by the current navigation operation.
   * @param shouldCloneNaviProps
   *          Defines if the property values should be cloned or shallow copied.
   */
  /* package */ void setNaviScopeProperties(
      Map<String, Object> prevHistoryProps,
      Map<String, Object> newLinkProps,
      boolean cloneNaviProps) {
    int prevCount = (prevHistoryProps != null) ? prevHistoryProps.size() : 0;
    int newCount  = (newLinkProps != null) ? newLinkProps.size() : 0;

    if (prevCount + newCount > 0) {
      Map<String, Object> naviScopeProperties;
      if (newCount == 0) {
        // Histories with the same property set share the same map.
        naviScopeProperties = prevHistoryProps;
      }
      else {
        // We assume usually small maps.
        // TreeMaps should be less memory consuming in this case...
        naviScopeProperties = new TreeMap<String, Object>();
        if (prevHistoryProps != null) {
          naviScopeProperties.putAll(prevHistoryProps);
        }
        // New properties may (and should) replace existing values.
        if (newLinkProps != null) {
          naviScopeProperties.putAll(newLinkProps);
        }
      }

      if (cloneNaviProps) {
        this.naviScopeProperties =
          NaviUtil.deepCloneValues(naviScopeProperties, new TreeMap<String, Object>());
      }
      else {
        this.naviScopeProperties = naviScopeProperties;
      }
    }
  }

  /**
   * @return The latest usage (ping) time of this history.
   */
  /* package */ long getLastVisitTime() {
    return lastVisitTime;
  }

  /**
   * @return A report about the state of the history.
   */
  /* package */ public String getTraceString() {
    return toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(100);
    sb.append("history (").append(getVersionString()).append("): ").append(Arrays.asList(items));
    if (naviScopeProperties.size() > 0) {
      sb.append(" naviProps: ").append(naviScopeProperties);
    }
    return sb.toString();
  }

  /**
   * Searches only for links to the same page. Ignores the position on the page.
   * <p>
   * XXX olaf: That might be fine for most use cases. When the position on the
   * page gets relevant for the history, this code should get configurable...
   */
  private int findPosInList(NaviLink[] list, NaviLink itemToFind) {
    for (int i = 0; i < list.length; ++i) {
      if (itemToFind.isLinkToSamePage(list[i])) {
        return i;
      }
    }

    // not found
    return -1;
  }

}

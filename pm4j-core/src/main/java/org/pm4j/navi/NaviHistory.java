package org.pm4j.navi;

import java.io.Serializable;
import java.util.Iterator;


/**
 * Interface for navigation history implementations.
 *
 * @author olaf boede
 */
public interface NaviHistory {

  /**
   * @return ID of the navigation session this history belongs to.
   */
  String getSessionId();

  /**
   * @return The navigation move 'counter' within the session.
   */
  String getVersion();

  /**
   * @return An identifier that provides the navigation session ID and the
   *         navigation version within that session.<br>
   *         Format: 'sessionId.versionId'
   */
  String getVersionString();

  /**
   * @return An iterator that starts with the first history item.
   */
  // XXX olaf: remove 'Item' from name.
  Iterator<NaviLink> getItemIterator();

  /**
   * @return An iterator that starts with the current history item.
   */
  Iterator<NaviLink> getReverseIterator();

  /**
   * @return The number of history items.
   */
  int getSize();

  /**
   * @return The link for the current navigation position.
   */
  NaviLink getCurrentLink();

  /**
   * Provides the previous navigation item.
   *
   * @return The previous item or <code>null</code>.
   */
  NaviLink getPrevLink();

  /**
   * Provides the first navigation item of the current history.
   *
   * @return The first item. May return only be <code>null</code> when the
   *         navigation history is empty.
   */
  NaviLink getFirstLink();

  /**
   * Provides the previous navigation item.
   * When there is no previous item, the start link will be returned.
   *
   * @return The previous item or the start item when the current page is the first one.
   */
  NaviLink getPrevOrStartLink(NaviLink... linksToSkip);

  /**
   * Provides the navigation link before the given link.
   *
   * @param l The link to find the predecessor for.
   * @return The found predecessor or <code>null</code> if there is none.
   */
  NaviLink findLinkBefore(NaviLink l);

  /**
   * Checks whether this navigation history contains a link to the given page.
   * <p>
   * The implementation checks only the page information, not the position on
   * the page (see {@link NaviLink#getPosOnPage()}.
   *
   * @param naviLink
   *          The link to check.
   * @return <code>true</code> when the given link is part of the navigation
   *         history.
   */
  boolean containsLinkToPage(NaviLink naviLink);

  /**
   * @return The manager of navigation sessions and histories.
   */
  NaviManager getNaviManager();

  /**
   * @return The navigation management configuration.
   */
  NaviHistoryCfg getNaviCfg();

  /**
   * Gets called whenever this history state was used by a navigation activity.
   * That includes moves, re-displays of the page, and creation of derived
   * histories.
   */
  void ping();

  /**
   * Modifies the position information on the current page.
   * <p>
   * May be used in case of back navigation scenarios: A link in the middle of
   * some page should have a history based back-navigation to this 'middle-page'
   * position.<br>
   * In this case it is useful mark the current position with this method before
   * the navigation to the next page occurs.<br>
   * The method {@link #getPrevLink()} will then provide a link back to the
   * 'middle-page' position.
   *
   * @param pos
   *          The position on the current page to remember for back navigation.
   */
  void setPosOnPage(String pos);

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
  <T extends Serializable> T getConversationProperty(String propName);

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
  void setConversationProperty(String propName, Serializable value);

  /**
   * Provides a property that is scoped within a navigation loop.
   *
   * @param propName
   *          Name of the property to get.
   * @return The found value or <code>null</code>.
   */
  <T extends Object> T getNaviScopeProperty(String propName);

  /**
   * Provides a property that is scoped within a navigation loop.
   * 
   * @param propName
   *          Name of the property to get.
   * @param defaultValue
   *          The value to provide if there is no property with the given name.
   * @return The found value or the provided <code>defaultValue</code>.
   */
  <T extends Object> T getNaviScopeProperty(String propName, T defaultValue);

  // XXX olaf: check if that will stay public...
  void setNaviScopeProperty(String propName, Object value);
}

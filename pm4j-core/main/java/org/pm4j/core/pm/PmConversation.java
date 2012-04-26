package org.pm4j.core.pm;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.navi.NaviHistory;


/**
 * A presentation model conversation is a controller for a set of presentation
 * models. It is responsible for
 * <ul>
 * <li>handling the {@link PmMessage}s of all</li>
 * <li>localization context</li>
 * <li>undo command list handling</li>
 * <li>named objects within the scope</li>
 * </ul>
 *
 * @author olaf boede
 */
public interface PmConversation extends PmElement {

  /**
   * @return The optional parent conversation context.
   *         <p>
   *         <code>null</code> if this is the top most conversation instance.
   */
  PmConversation getPmParentConversation();

  /**
   * Provides the language to be used for the for titles, tool tips etc. of the
   * PMs within the scope of this conversation.
   * <p>
   * If no language is defined for this conversation and all (optionally existing)
   * parent conversations, the default language of the Java VM will be used.
   *
   * @return The locale that is used within this conversation.<br>
   *         Returns never <code>null</code>.
   */
  Locale getPmLocale();

  /**
   * @param locale
   *          The locale to be used within the scope of this conversation.<br>
   *          May be <code>null</code> for sub conversations to switch back to the
   *          locale of the parent conversation or (in case of a root conversation) to the
   *          language of the Java VM.
   */
  void setPmLocale(Locale locale);
  
  /**
   * Used for date conversions.
   * 
   * @return The time zone that is used within this conversation.<br>
   *         Returns never <code>null</code>.
   */
  public TimeZone getPmTimeZone();

  /**
   * @param pmTimeZone The time zone that is used within this conversation.<br>
   *         Must never be <code>null</code>.
   */
  public void setPmTimeZone(TimeZone pmTimeZone);

  /**
   * @return The undo/redo command history of this conversation.
   */
  PmCommandHistory getPmCommandHistory();

  /**
   * Gets all active messages within this conversation.
   *
   * @return The messages. An empty collection if there are no messages.
   */
  List<PmMessage> getPmMessages();

  /**
   * Gets all messages for the given model.
   *
   * @param severity
   *          Severities to return. If <code>null</code> is passed here all
   *          messages will be returned.
   * @return The messages. An empty collection if there are no messages.
   */
  List<PmMessage> getPmMessages(PmObject forPm, Severity severity);

  /**
   * @return <code>true</code> when any validation has failed and/or another
   *         error message exists.
   */
  boolean getHasPmErrors();

  /**
   * Adds a message to the conversation.
   *
   * @param pmMessage The message to add.
   */
  void addPmMessage(PmMessage pmMessage);

  /**
   * Sets a property that is stored within this conversation instance.
   * <p>
   * ATTENTION for Web application developers: If your {@link PmConversation} is used
   * in session scope, this property value will be shared for all opened tabs
   * and windows of the user session.<br>
   * If you want to manage specific property values for each tab or window,
   * consider the usage of conversation or navigation scoped properties.
   * <p>
   * If you have configured a {@link NaviHistory}, you may use conversation and
   * navigation scoped variables.<br>
   * See {@link NaviHistory#setNaviScopeProperty(String, Object)} for more
   * information.
   * <p>
   * Alternatively you may consider the usage of dialog specific memory scopes
   * provided by your application framework. <br>
   * Frameworks like Seem and Spring Webflow offer some solutions.
   *
   * @param key
   *          The property key.
   * @param value
   *          The property value. When it is <code>null</code>, the property
   *          will be removed.
   * @return The value that was bound to the given key before this call.<br>
   *         <code>null</code> if the property was set for the first time.
   */
  Object setPmNamedObject(Object key, Object value);

  /**
   * @see #setPmNamedObject(Object, Object) for more detailed information.
   *
   * @param key
   *          The property key.
   * @return
   *          The found property value. May be <code>null</code> when the property
   *          is not set.
   */
  Object getPmNamedObject(Object key);

  // TODO: move to PmDataInput when support for other buffer scopes is implemented.
  /**
   * Defines if changes should be applied to the buffer only or to the data
   * container behind the presentation model.
   *
   * @param bufferedMode
   *          <code>true</code> switches to buffered mode.<br>
   *          <code>false</code> switches to unbuffered mode.
   */
  void setBufferedPmValueMode(boolean bufferedMode);

  /**
   * The default definitions may be defined for a tree of PMs.
   *
   * @return The {@link PmDefaults} definitions to be used.
   */
  PmDefaults getPmDefaults();

  /**
   * @return The history of visited pages.
   */
  NaviHistory getPmNaviHistory();

}

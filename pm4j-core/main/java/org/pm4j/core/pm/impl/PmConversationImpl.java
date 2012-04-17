package org.pm4j.core.pm.impl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.pm.PmCommandHistory;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmDefaults;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.impl.connector.PmToNoViewTechnologyConnector;
import org.pm4j.core.pm.impl.connector.PmToViewTechnologyConnector;
import org.pm4j.navi.NaviHistory;

/**
 * Basic PM conversation implementation.
 * <p>
 * TODO: Move code for message and invalid value handling out to a separate class.
 *       The current code mix is too complex.
 *
 * @author olaf boede
 */
public class PmConversationImpl extends PmElementBase implements PmConversation {

  private static final Log LOG = LogFactory.getLog(PmConversationImpl.class);

  // initialized with self-reference which indicates that the reference is not yet resolved.
  private PmConversationImpl pmParentConversation = this;
  private Locale pmLocale;
  private PmExceptionHandler pmExceptionHandler;
  private PmToViewTechnologyConnector pmViewConnector;

  /**
   * Configurable default settings for this conversation.
   * <p>
   * Is optional. If not configured, the settings of the containing PM tree
   * or (if there is no parent tree) the {@link PmDefaults} of the VM will be
   * used.
   */
  private PmDefaults pmDefaults;


  /**
   * The element factory definition is usually a member of the in
   * {@link PmObjectBase.MetaData}.<br>
   * This attribute exists in parallel to support constructor based
   * bean mapping declarations that are currently used in many test cases.
   * <p>
   * This (test-) feature may disappear in future releases. (?)
   */
  private BeanPmFactory pmElementFactory;

  private List<PmMessage> pmMessages = Collections.synchronizedList(new ArrayList<PmMessage>());


  /**
   * Weak references to failed attributes to allow an efficient error state
   * cleanup implementation.
   */
  private List<WeakReference<SetValueContainer<?>>> pmInvalidValues = Collections.synchronizedList(new ArrayList<WeakReference<SetValueContainer<?>>>());

  /**
   * A container for named objects that are bound to this conversation.
   */
  private Map<Object, Object> pmNamedObjects = new ConcurrentHashMap<Object, Object>();

  /**
   * The history of undo/redo commands.
   */
  private PmCommandHistoryImpl pmCommandHistory = new PmCommandHistoryImpl();

  /**
   * Default constructor for some limited dependency injection frameworks
   * that don't support constructor parameters.
   */
  public PmConversationImpl() {
    this((PmObject)null);
  }

  /**
   * Constructor for not annotation based creation of root conversations.<br>
   * Used for very small sample and test applications.
   *
   * @param pmFactoryClasses The PM classes, used to initialize the internal PM-Bean factory.
   */
  public PmConversationImpl(Class<?>... pmFactoryClasses) {
    this(null, pmFactoryClasses);
  }

  /**
   * Constructor for not annotation based creation of conversations.<br>
   * Used for very small sample and test applications.
   *
   * @param pmParent The optional parent context PM.
   * @param pmFactoryClasses The PM classes, used to initialize the internal PM-Bean factory.
   */
  public PmConversationImpl(PmObject pmParent, Class<?>... pmFactoryClasses) {
    this(pmParent);
    this.pmElementFactory = new BeanPmFactory(pmFactoryClasses);
  }

  /**
   * @param pmParent The optional parent context PM.
   */
  public PmConversationImpl(PmObject pmParent) {
    super(pmParent);
  }

  @Override
  public PmConversation getPmConversation() {
    return this;
  }

  @Override
  public PmDefaults getPmDefaults() {
    return (pmDefaults != null)
              ? pmDefaults
              : (getPmParentConversation() != null)
                  ? getPmParentConversation().getPmDefaults()
                  : PmDefaults.getInstance();
  }

  /**
   * @param pmDefaults The settings to be used for the PM tree of this conversation object.
   */
  public void setPmDefaults(PmDefaults pmDefaults) {
    this.pmDefaults = pmDefaults;
  }

  @Override
  public NaviHistory getPmNaviHistory() {
    return getViewConnector().getNaviHistory();
  }

  @Override
  public Locale getPmLocale() {
    if (pmLocale != null) {
      return pmLocale;
    }
    else {
      PmConversation parentConversationPm = getPmParentConversation();
      if (parentConversationPm != null) {
        return parentConversationPm.getPmLocale();
      }
      else {
        return Locale.getDefault();
      }
    }
  }

  @Override
  public void setPmLocale(Locale locale) {
    this.pmLocale = locale;
  }

  @Override
  public PmCommandHistory getPmCommandHistory() {
    return pmCommandHistory;
  }

  /**
   * Defines a handler that will be called whenever an exception occurs with an
   * PM layer operation that is related to this conversation.
   * <p>
   * Views may define specific handlers to render exception related information.
   *
   * @param pmExceptionHandler
   */
  public void setPmExceptionHandler(PmExceptionHandler localPmExceptionHandler) {
    this.pmExceptionHandler = localPmExceptionHandler;
  }

  /**
   * @return The {@link PmExceptionHandler} that handles exceptions for this
   *         conversation. If no local handler is defined, the handler of the parent
   *         conversation or the default handler will be returned.<br>
   *         Will never return <code>null</code>.
   */
  public PmExceptionHandler getPmExceptionHandler() {
    PmExceptionHandler result;
    if (pmExceptionHandler != null) {
      result = pmExceptionHandler;
    }
    else {
      if (getPmParent() != null) {
        result = ((PmConversationImpl)getPmParentConversation()).getPmExceptionHandler();
      }
      else {
        // Root conversation without explicitly defined exception handler.
        // A default handler will be defined on the fly here:
        pmExceptionHandler = new PmExceptionHandlerImpl();
        result = pmExceptionHandler;
      }
    }

    return result;
  }

  public void setPmViewConnector(PmToViewTechnologyConnector navigationHandler) {
    this.pmViewConnector = navigationHandler;
  }

  /**
   * @return The platform and/or conversation specific
   *         {@link PmToViewTechnologyConnector}. If no local handler is defined,
   *         the handler of the parent conversation or the default handler will be
   *         returned.<br>
   *         Will never return <code>null</code>.
   */
  public PmToViewTechnologyConnector getViewConnector() {
    PmToViewTechnologyConnector result;
    if (pmViewConnector != null) {
      result = pmViewConnector;
    }
    else {
      if (getPmParent() != null) {
        result = pmViewConnector = ((PmConversationImpl)getPmParentConversation()).getViewConnector();
      }
      else {
        // Root conversation without explicitly defined handler.
        // A default handler will be defined on the fly here:
        result = pmViewConnector = new PmToNoViewTechnologyConnector();
      }
    }
    return result;
  }

  @Override
  public PmConversation getPmParentConversation() {
    if (pmParentConversation == this) {
      pmParentConversation = getPmParent() != null
              ? (PmConversationImpl)getPmParent().getPmConversation()
              : null;
    }
    return pmParentConversation;
  }

  @Override
  protected BeanPmFactory getOwnPmElementFactory() {
    // local test factory support:
    if (pmElementFactory != null) {
      return pmElementFactory;
    }
    else {
      // use annotated definition.
      pmElementFactory = getPmMetaData().getPmElementFactory();
      // to prevent permanent re-evaluation of the attribute
      if (pmElementFactory == null) {
        pmElementFactory = new BeanPmFactory();
      }
    }
    return pmElementFactory;
  }

  @Override
  public void addPmMessage(PmMessage pmMessage) {
    pmMessages.add(pmMessage);
    if (pmMessage instanceof PmValidationMessage) {
      registerInvalidValue((PmValidationMessage)pmMessage);
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug(pmMessage.getTitle());
    }
  }

  /**
   * Adds a message to the conversation.
   * <p>
   * Creates internally a message that uses this conversation as pm context.
   * That means that it should be possible to find the resource string in relation
   * to the concrete conversation class. (In its directory or above it.)
   *
   * @param severity Severity of the message.
   * @param resKey Resource key for the localized message.
   * @param resArgs Resource string arguments.
   * @return The added message instance.
   */
  public PmMessage addPmMessage(Severity severity, String resKey, Object... resArgs) {
    PmMessage message = new PmMessage(this, severity, resKey, resArgs);
    addPmMessage(message);
    return message;
  }

  /**
   * Registers the invalid value of a validation message.
   * <p>
   * Prevents duplicate value references for the same PM and
   * also cleans garbage collected value references.
   */
  private void registerInvalidValue(PmValidationMessage msg) {
    WeakReference<SetValueContainer<?>> newRef = msg.getInvalidValueRef();
    SetValueContainer<?> newValue = (newRef != null) ? newRef.get() : null;
    if (newValue != null) {
      // Cleanup values for the same PM-attribute and garbage collected values:
      synchronized (pmInvalidValues) {
        Iterator<WeakReference<SetValueContainer<?>>> iter = pmInvalidValues.iterator();
        while (iter.hasNext()) {
          WeakReference<SetValueContainer<?>> ref = iter.next();
          SetValueContainer<?> value = ref.get();
          if ((value == null) ||
              (value.getPm() == newValue.getPm())) {
            iter.remove();
          }
        }
      }
      pmInvalidValues.add(newRef);
    }
  }

  @Override
  public List<PmMessage> getPmMessages() {
    return getPmMessages(null, null);
  }

  /**
   * Gets all messages for the given model.
   *
   * @param severity
   *          Severities to return. If <code>null</code> is passed here all
   *          messages will be returned.
   * @return The messages. An empty collection if there are no messages.
   */
  public List<PmMessage> getPmMessages(PmObject forPm, Severity severity) {
    if (pmMessages.size() == 0) {
      return Collections.emptyList();
    }

    boolean forAllPms = (forPm == null || forPm == this);
    List<PmMessage> list = new ArrayList<PmMessage>();
    synchronized(pmMessages) {
      for (PmMessage m : pmMessages) {
        if ((forAllPms || forPm == m.getPm()) &&
            (severity == null || severity.equals(m.getSeverity()))
           ) {
            list.add(m);
        }
      }
    }

    return list;
  }

  /**
   * Removes a single message.
   *
   * @param pmMessage
   *          The message to remove.
   * @return <code>true</code> when the message was member of the message set.<br/>
   *         <code>false</code> when the message was not part of the message
   *         set.
   */
  public boolean clearPmMessage(PmMessage pmMessage) {
    return pmMessages.remove(pmMessage);
  }

  /**
   * Clears the messages with the given severity.
   *
   * @param pm
   *          The presentation model the messages should be cleaned for or
   *          <code>null</code> if all messages have to be cleared.
   * @param severity
   *          The severity level to clear. Clears all messages if
   *          <code>null</code> is passed here.
   */
  public void clearPmMessages(PmObject pm, Severity severity) {
    PmEventApi.ensureThreadEventSource(this);
    synchronized(pmMessages) {
      if ((pm == this || pm == null) && severity == null) {
        clearPmInvalidValues();
        pmMessages.clear();
      }
      else {
        Iterator<PmMessage> iter = new ArrayList<PmMessage>(pmMessages).iterator();
        while(iter.hasNext()) {
          PmMessage m = iter.next();
          if ((pm == null || pm == m.getPm()) &&
              (severity == null || m.getSeverity().equals(severity))) {
            if (m instanceof PmValidationMessage) {
              PmMessageUtil.clearPmInvalidValues(m.getPm());
            }
            pmMessages.remove(m);
          }
        }
      }
    }
  }

  /**
   * Clears:
   * <ul>
   *   <li>All messages of all models within this conversation.</li>
   *   <li>The not validated attribute values of all elements within this conversation.</li>
   * <ul>
   */
  @Override
  public void clearPmInvalidValues() {
    boolean debugEnabled = LOG.isDebugEnabled();
    synchronized(pmInvalidValues) {
      for (WeakReference<SetValueContainer<?>> ref : pmInvalidValues) {
        SetValueContainer<?> value = ref.get();
        if (value != null) {
          if (debugEnabled) {
            LOG.debug("Cleaning invalid value state for attribute '" + PmUtil.getPmLogString(value.getPm()) + "'.");
          }
          PmMessageUtil.clearPmInvalidValues(value.getPm());
        }
      }
      pmInvalidValues.clear();
    }
  }

  /**
   * Will be called when {@link #findNamedPmObject(String)} did not find a
   * value for the given name.<br>
   * An implementation may use this method to generate the requested object
   * on the fly.
   * <p>
   * TODO: describe a conversation scope scenario...
   *
   * @param name Name of the missing property.
   */
  protected void handleNamedPmObjectNotFound(String name) {
    if (pmParentConversation != null &&
        pmParentConversation != this) {
      pmParentConversation.handleNamedPmObjectNotFound(name);
    }
  }

  public boolean getHasPmErrors() {
    return !getPmMessages(null, Severity.ERROR).isEmpty();
  }

  public Object getPmNamedObject(Object key) {
    Object obj = pmNamedObjects.get(key);
    return obj == null &&
           getPmParent() != null
              ? getPmParentConversation().getPmNamedObject(key)
              : obj;
  }

  public Object setPmNamedObject(Object key, Object value) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("setPmNamedObject '" + key + "' to '" + value + "'. PmConversation context: " + PmUtil.getPmLogString(this));
    }

    if (value == null) {
      return pmNamedObjects.remove(key);
    }
    else {
      return pmNamedObjects.put(key, value);
    }
  }

  // ======== Buffered data input support ======== //

  /**
   * Is switched on for conversations that store all modified bean attribute values
   * in a buffer.
   */
  private boolean bufferedPmValueMode;

  @Override
  public boolean isBufferedPmValueMode() {
    return bufferedPmValueMode;
  }

  public void setBufferedPmValueMode(boolean bufferedMode) {
    this.bufferedPmValueMode = bufferedMode;
  }

  // TODO olaf: conversation based pm value commit is not yet implemented...
  @Override
  public void rollbackBufferedPmChanges() {
    if (LOG.isWarnEnabled()) {
      LOG.warn("!!! WARNING !!! Conversation based pm value commit implementation is not yet completed.\n" +
        "Operation will only be applied on attributes of this instance.");
    }
    super.rollbackBufferedPmChanges();
  }

  @Override
  public void commitBufferedPmChanges() {
    if (LOG.isWarnEnabled()) {
      LOG.warn("!!! WARNING !!! Conversation based pm value commit implementation is not yet completed.\n" +
        "Operation will only be applied on attributes of this instance.");
    }
    super.commitBufferedPmChanges();
  }

  /**
   * XXX olaf: remove??
   * <p>
   * Convenience class with type cmdSave access to the parent conversation.
   *
   * @author olaf boede
   *
   * @param <T_PARENT> Type of the parent conversation.
   */
  @Deprecated
  public static class ChildSession<T_PARENT extends PmConversation> extends PmConversationImpl {

    public ChildSession() {
      super();
    }

    public ChildSession(T_PARENT parentSession) {
      super(parentSession);
    }

    @SuppressWarnings("unchecked")
    public T_PARENT getParentSessionImpl() {
      return (T_PARENT)getPmParentConversation();
    }
  }

}

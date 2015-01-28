package org.pm4j.core.pm.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.cache.CacheStrategy;
import org.pm4j.common.util.collection.ListUtil;
import org.pm4j.common.util.reflection.BeanAttrAccessor;
import org.pm4j.common.util.reflection.BeanAttrAccessorImpl;
import org.pm4j.common.util.reflection.BeanAttrArrayList;
import org.pm4j.common.util.reflection.ClassUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmDefaults;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmCacheCfg.Clear;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmInit;
import org.pm4j.core.pm.annotation.PmObjectCfg;
import org.pm4j.core.pm.annotation.PmObjectCfg.Enable;
import org.pm4j.core.pm.annotation.PmObjectCfg.Visible;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.annotation.customize.CustomizedAnnotationUtil;
import org.pm4j.core.pm.annotation.customize.PmAnnotationApi;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.api.PmValidationApi;
import org.pm4j.core.pm.impl.PmObjectBase.MetaData.MetaDataId;
import org.pm4j.core.pm.impl.inject.DiResolver;
import org.pm4j.core.pm.impl.inject.DiResolverUtil;
import org.pm4j.core.pm.impl.title.PmTitleProvider;
import org.pm4j.core.pm.impl.title.PmTitleProviderValuebased;
import org.pm4j.core.pm.impl.title.TitleProviderAttrValueBased;

/**
 * Provides common presentation model base functionality.
 *
 * @author olaf boede
 */
public abstract class PmObjectBase implements PmObject {

  private static final Log LOG = LogFactory.getLog(PmObjectBase.class);

  /** Indicator for view adapters . */
  private static String PM_TO_VIEW_CONNECTOR_NOT_YET_INITIALIZED = "The PM to view connector is not yet initialized.";

  /**
   * The parent context, this PM was created in.
   * <p>
   * Is used to get some application context information. E.g. the locale ...
   * <p>
   * Is <code>null</code> for root sessions only.
   */
  private PmObjectBase pmParent;

  /** Dynamic visibility flag. */
  private boolean pmVisible = true;
  /* package */ Object pmVisibleCache;

  /** Enabled state flag. */
  private boolean pmEnabled = true;
  /* package */ Object pmEnabledCache;

  /**
   * Optional title cache.
   */
  /* package */  String pmCachedTitle;

  public enum PmInitState {
    NOT_INITIALIZED,
    FIELD_BOUND_CHILD_META_DATA_INITIALIZED,
    BEFORE_ON_PM_INIT,
    INITIALIZED };

  /** Helper indicator that prevents double initialization. */
  /* package */ PmInitState pmInitState = PmInitState.NOT_INITIALIZED;

  /**
   * An optional cache for the bean to PM association within the current PM hierarchy scope.
   */
  /* package */ BeanPmCache pmBeanFactoryCache;

  /** A container for application/user specific additional information. */
  private Map<String, Object> pmProperties = Collections.emptyMap();

  /**
   * An optional view technology specific adapter class.<br>
   * If {@link #getPmToViewConnector()} was not yet called it is <code>null</code>.<br>
   * If there is no view technology connector available, this member will be set to a self-reference.
   */
  private Object pmToViewConnector = PM_TO_VIEW_CONNECTOR_NOT_YET_INITIALIZED;

  /**
   * Constructor.
   */
  public PmObjectBase() {
  }

  /**
   * @param pmParent
   *          The context, this PM was created in. E.g. a session, a command, a
   *          list field.
   */
  public PmObjectBase(PmObject pmParent) {
    this.pmParent = (PmObjectBase) pmParent;
  }


  @Override
  public boolean canSetPmTitle() {
    return getPmTitleDef().canSetTitle(this);
  }

  protected String getPmTitleImpl() {
    return getPmTitleDef().getTitle(this);
  }

  @Override
  @Deprecated public String getPmShortTitle() {
    return getPmTitleDef().getShortTitle(this);
  }

  @Override
  public final String getPmTooltip() {
    String toolTip = getPmTooltipImpl();
    // XXX olaf: a kind of decorator could add some flexibility for
    //           different error display requirements...
    if (getPmMetaData().addErrorMessagesToTooltip &&
        !isPmValid())
    {
      List<String> lines = new ArrayList<String>();

      if (toolTip != null)
        lines.add(toolTip);

      for (PmMessage m : PmMessageApi.getMessages(this, Severity.ERROR))
        lines.add(m.getTitle());

      toolTip = lines.size() > 0
        ? StringUtils.join(lines, "\n")
        : null;
    }

    if (getPmConversation().getPmDefaults().debugHints) {
      toolTip = toolTip != null ? toolTip + "/n" : "";
      toolTip += PmUtil.getPmLogString(this);
    }

    return toolTip;
  }

  protected String getPmTooltipImpl() {
    return getPmMetaData().tooltipUsesTitle
              ? getPmTitle()
              : getPmTitleDef().getToolTip(this);
  }

  @Override
  public String getPmIconPath() {
    return getPmTitleDef().getIconPath(this);
  }

  @Override
  public void setPmTitle(String titleString) {
    PmEventApi.ensureThreadEventSource(this);
    getPmTitleDef().setTitle(this, titleString);
  }

  @Override
  public PmConversation getPmConversation() {
    return (pmParent != null)
              ? pmParent.getPmConversation()
              : null;
  }

  @Override
  public final PmObject getPmParent() {
    return pmParent;
  }

  @Override
  public void setPmParent(PmObject pmParent) {
    assert pmInitState == PmInitState.NOT_INITIALIZED;
    if (this.pmParent != null)
      throw new PmRuntimeException(this, "pmParent is already set.");
    if (pmParent == this)
      throw new PmRuntimeException("Can't use a self reference as pmParent.");

    this.pmParent = (PmObjectBase)pmParent;

  }

  @Override
  public final boolean isPmVisible() {
    MetaData metaData = getPmMetaData();
    Object v = metaData.visibilityCache.cacheStrategy.getCachedValue(this);

    if (v != CacheStrategy.NO_CACHE_VALUE) {
      // just return the cache hit (if there was one)
      return (Boolean) v;
    }
    else {
      boolean visible = isPmVisibleImpl() &&
                        CustomizedAnnotationUtil.isVisible(this, metaData.permissionAnnotations);
      return (Boolean) metaData.visibilityCache.cacheStrategy.setAndReturnCachedValue(this, visible);
    }
  }

  /**
   * Override this method to define business logic driven visibility logic.
   * 
   * Overriding this method may result in losing {@link @PmObjectCfg} annotation 
   * configuration, that might have been set. 
   *
   * @return <code>true</code> if the PM should be visible.
   */
  protected boolean isPmVisibleImpl() {  
    switch(getPmMetaData().visibilityCfg) {
    case IF_ENABLED:
      return isPmEnabled();
    case IN_EDITABLE_CTXT:
      return !isPmReadonly();
    case NO:
      return false;
    default:
      return pmVisible;    
    }
  }

  @Override @Deprecated
  public void setPmVisible(boolean visible) {
    PmEventApi.ensureThreadEventSource(this);
    boolean changed = (pmVisible != visible);
    pmVisible = visible;
    // Does not fire change events if called within the initialization phase.
    if (changed) {
      PmEventApi.firePmEventIfInitialized(this, PmEvent.VISIBILITY_CHANGE);
    }
  }

  @Override
  public boolean isPmEnabled() {
    MetaData metaData = getPmMetaData();
    Object e = metaData.enablementCache.cacheStrategy.getCachedValue(this);

    if (e != CacheStrategy.NO_CACHE_VALUE) {
      // just return the cache hit (if there was one)
      return (Boolean) e;
    }
    else {
      boolean enabled = isPmEnabledImpl() &&
                        CustomizedAnnotationUtil.isEnabled(this, metaData.permissionAnnotations);
      return (Boolean) metaData.enablementCache.cacheStrategy.setAndReturnCachedValue(this, enabled);
    }
  }

  /**
   * Override this method to define business logic driven enablement logic.
   * 
   * Be aware, that overriding this method may result in losing {@link PmObjectCfg} 
   * configuration, that might have been set. 
   *
   * @return <code>true</code> if the PM should be visible.
   */
  protected boolean isPmEnabledImpl() {
    switch(getPmMetaData().enablementCfg) {
    case NO: 
      return false;
    case IN_EDITABLE_CTXT:
      return !isPmReadonly();
    default:
      return pmEnabled;
    }
  }

  @Override @Deprecated
  public void setPmEnabled(boolean enabled) {
    PmEventApi.ensureThreadEventSource(this);
    boolean changed = (pmEnabled != enabled);
    pmEnabled = enabled;
    // Does not fire change events if called within the initialization phase.
    if (changed) {
      PmEventApi.firePmEventIfInitialized(this, PmEvent.ENABLEMENT_CHANGE);
    }
  }

  @Override
  public boolean isPmValid() {
    List<PmMessage> msgList = PmMessageApi.getPmTreeMessages(this, Severity.ERROR);
    return msgList.isEmpty();
  }

  /**
   * Please override {@link #isPmReadonlyImpl()} to provide your specific logic.
   */
  @Override
  public final boolean isPmReadonly() {
    zz_ensurePmInitialization();
    boolean readonly  = isPmReadonlyImpl() ||
                        CustomizedAnnotationUtil.isReadonly(this, getPmMetaData().permissionAnnotations);
    return readonly;
  }

  /**
   * Sub classes may implement their specific read-only definition here.
   *
   * @return <code>true</code> if the PM is in read-only state.
   */
  protected boolean isPmReadonlyImpl() {
    return getPmMetaData().readOnly ||
           (pmParent != null && pmParent.isPmReadonly());
  }

  /**
   * @return The key for language specific resources.
   */
  public String getPmResKey() {
    return getPmMetaDataWithoutPmInitCall().resKey;
  }

  /**
   * Provides a resource key prefix that is used for sub-PM's resource key generation.
   * <p>
   * It may be defined by {@link PmTitleCfg#resKeyBase()} or by overriding this method.
   * <p>
   * If nothing special is defined this method provides the resource key of this instance.
   *
   * @return the resource key base.
   */
  public String getPmResKeyBase() {
    return getPmMetaDataWithoutPmInitCall().resKeyBase;
  }

  @Override
  public final String getPmTitle() {
    CacheStrategy strategy = getPmMetaData().titleCache.cacheStrategy;
    Object title = strategy.getCachedValue(this);

    if (title != CacheStrategy.NO_CACHE_VALUE) {
      // just return the cache hit (if there was one)
      return (String) title;
    }
    else {
      return (String) strategy.setAndReturnCachedValue(this, getPmTitleImpl());
    }
  }

  /** The set of event listeners. */
  /* package */ PmEventTable pmEventTable;
  /* package */ PmEventTable pmWeakEventTable;


  /**
   * Is called whenever an event with the flag {@link PmEvent#VALUE_CHANGE}
   * was fired for this PM.
   *
   * @param event The fired event.
   */
  protected void onPmValueChange(PmEvent event) {
  }

  /**
   * Gets called whenever the state of a child was changed.
   * <p>
   * This method can be used to observe changes within a part of the PM tree.
   *
   * @param child
   *          The affected child of this PM. This PM must not be the changed
   *          one. The change may be related to a sub-PM of this child. Please
   *          check {@link PmEvent#pm} to get the changed PM.
   * @param event
   *          The change event that caused this call.
   */
  protected void onPmChildStateChange(PmObject child, PmEvent event) {
  }

  /**
   * Clears cached content (if there was something cached).<br>
   * Causes a reload of the content with the next request.
   *
   * @param cacheKinds
   *          The set of caches to be cleared. If no cacheKind is specified, all
   *          cache kinds will be cleared.
   */
  protected void clearCachedPmValues(final Set<PmCacheApi.CacheKind> cacheSet) {
    // Has no effect for fresh instances.
    if (pmInitState != PmInitState.INITIALIZED)
      return;

    MetaData md = getPmMetaData();

    if (cacheSet.contains(PmCacheApi.CacheKind.ENABLEMENT))
      md.enablementCache.cacheStrategy.clear(this);

    if (cacheSet.contains(PmCacheApi.CacheKind.VISIBILITY))
      md.visibilityCache.cacheStrategy.clear(this);

    if (cacheSet.contains(PmCacheApi.CacheKind.TITLE))
      md.titleCache.cacheStrategy.clear(this);

    for (PmObject p : getPmChildrenAndFactoryPms()) {
      if (p instanceof PmObjectBase) {
        ((PmObjectBase)p).clearCachedPmValues(cacheSet);
      }
    }
  }

  /**
   * Provides a default title provider that may be overridden by concrete
   * classes.
   *
   * @return The concrete title provider that implements the specific title
   *         logic.
   */
  @SuppressWarnings("unchecked")
  protected <T extends PmObject> PmTitleProvider<T> getPmTitleDef() {
    return getPmMetaData().pmTitleProvider;
  }

  /**
   * Fix resources are usually located in a package and archive context of a
   * domain class. E.g. the string resources are may be located in the same
   * package or a parent package of the class that uses it.
   * <p>
   * If you write a domain specific presentation model class then the resource
   * loader context class is just this domain class.<br>
   * If you only use instances of classes (e.g. standard attributes) then the
   * domain specific resources will not be found in relation to that class.<br>
   * The standard class has to provide a way to get a domain context class.
   * In case of standard attributes that is done by providing the parent element class.
   *
   * @return Classes that can be used to find resources (string resources icons...).
   */
  // TODO oboede: make it final after removing overrides in domain code.
  public List<Class<?>> getPmResLoaderCtxtClasses() {
    MetaData md = getPmMetaDataWithoutPmInitCall();
    if (md.resLoaderCtxtClasses == null) {
      synchronized(md) {
        if (md.resLoaderCtxtClasses == null) {
          md.resLoaderCtxtClasses = Collections.unmodifiableList(getPmResLoaderCtxtClassesImpl());
        }
      }
    }
    return md.resLoaderCtxtClasses;
  }

  // TODO oboede: publish as protected method after split of custom and internal implementation.
  private List<Class<?>> getPmResLoaderCtxtClassesImpl() {
    // The parent's resource path and the path set of the own inheritance hierarchy.
    ArrayList<Class<?>> resClasses = getPmMetaDataWithoutPmInitCall().isSubPm
          ? new ArrayList<Class<?>>(pmParent.getPmResLoaderCtxtClasses())
          : new ArrayList<Class<?>>();
    // Own class/package structure will be considered after the embedding context.
    for (Class<?> c = getClass(); c != null && c != PmObjectBase.class; c = c.getSuperclass()) {
      resClasses.add(c);
    }
    return resClasses;
  }

  protected List<PmCommand> getVisiblePmCommands(PmCommand.CommandSet commandSet) {
    return PmUtil.getVisiblePmCommands(this);
  }

  /* package */ List<PmObject> getPmChildren() {
    return BeanAttrArrayList.makeList(this, getPmMetaDataWithoutPmInitCall().childFieldAccessorArray, pmDynamicSubPms.all);
  }

  /**
   * TODO olaf: provides not only the fix structure children.
   * Use that when we the new PM structure visitor will be implemented.
   * @return
   */
  /* package */ List<PmObject> getPmChildrenAndFactoryPms() {
    List<PmObject> subPms = BeanAttrArrayList.makeList(this, getPmMetaDataWithoutPmInitCall().childFieldAccessorArray, pmDynamicSubPms.all);
    if (pmBeanFactoryCache != null && !pmBeanFactoryCache.isEmpty()) {
      return ListUtil.collectionsToList(subPms, getFactoryGeneratedChildPms());
    } else {
      return subPms;
    }
  }

  /* package */ @SuppressWarnings("unchecked")
  Collection<PmObject> getFactoryGeneratedChildPms() {
    return (Collection<PmObject>) ((pmBeanFactoryCache != null && !pmBeanFactoryCache.isEmpty())
      ? pmBeanFactoryCache.getItems()
      : Collections.EMPTY_LIST);
  }

  /* package */ PmObject findChildPm(String localChildName) {
    BeanAttrAccessor accessor = getPmMetaData().nameToChildAccessorMap.get(localChildName);
    if (accessor != null) {
      return accessor.getBeanAttrValue(this);
    }
    else {
      return pmDynamicSubPms.nameToPmMap.get(localChildName);
    }
  }

  @Override
  public final String getPmName() {
    return getPmMetaDataWithoutPmInitCall().name;
  }

  @Override
  public final String getPmRelativeName() {
    return getPmMetaDataWithoutPmInitCall().relativeName;
  }

  String getPmCompositeChildName() {
    return getPmMetaDataWithoutPmInitCall().compositeChildName;
  }

  /**
   * @return The conversation implementation interface. It allows to use the
   *         internal feature set (not declared in the {@link PmConversation}
   *         interface).
   */
  protected PmConversationImpl getPmConversationImpl() {
    return (PmConversationImpl) getPmConversation();
  }

  /**
   * Provides a view technology specific PM adapter for this PM instance.
   * <p>
   * Implements a lazy init mechanism.
   *
   * @return The view adapter or <code>null</code> if none is available (or needed) for the view technology.
   */
  public Object getPmToViewConnector() {
    // Ensure that pmInit() was called. A domain PM might create its special view connector there.
    // On the other hand: don't trigger pmInit if some other code (e.g. a constructor) defined a
    // connector explicitly.
    if (pmToViewConnector == PM_TO_VIEW_CONNECTOR_NOT_YET_INITIALIZED) {
      zz_ensurePmInitialization();
      if (pmToViewConnector == PM_TO_VIEW_CONNECTOR_NOT_YET_INITIALIZED) {
        pmToViewConnector = getPmConversationImpl().getPmToViewTechnologyConnector().createPmToViewConnector(this);
      }
    }
    return pmToViewConnector;
  }

  /**
   * Defines an optional view specific connector.
   *
   * @param pmToViewConnector the connector to use.
   */
  public void setPmToViewConnector(Object pmToViewConnector) {
    this.pmToViewConnector = pmToViewConnector;
  }

  @Override
  public String toString() {
    return getPmConversation().getPmDefaults().getToStringNameBuilder().makeName(this);
  }


  // === Bean to PM mapping === XXX olaf: move to PmFactoryApi?

  /**
   * @return Registers a new bean in the bean PM cache.
   */
  /* package */ void registerInPmBeanCache(PmBean<?> pmBean) {
    BeanPmFactory f = getOwnPmElementFactory();
    if ((f != null) && f.canMakePmFor(pmBean.getPmBean())) {
      if (pmBeanFactoryCache == null) {
        pmBeanFactoryCache = new BeanPmCacheImpl();
      }
      pmBeanFactoryCache.add(pmBean);
    }
  }

  /* package */ BeanPmFactory getOwnPmElementFactory() {
    return getPmMetaData().pmElementFactory;
  }

  // ======== Validation ======== //

  /**
   * PM validator interface.
   *
   * @param <T> The type of PM to validate.
   */
  public static interface Validator {

    /**
     * Validates the given PM.<br>
     * The validation results are reported as {@link PmMessage}s.
     *
     * @param pm The PM to validate.
     */
    void validate(PmObject pm);
  }

  /**
   * Factory method that generates a type specific PM validator.
   * @return The validator.
   */
  protected Validator makePmValidator() {
    return new ObjectValidator<PmObjectBase>();
  }

  /**
   * Temporary switch that allows to keep old validation logic alive for a limited time.<br>
   * Gets called only once on meta data generation.<br>
   * TODO oboede: remove asap.
   *
   * @return <code>true</code> if the old logic should be applied for this PM and its children.
   */
  protected boolean isDeprValidation() {
    return pmParent != null
        ? pmParent.isDeprValidation()
        : true;
  }

  /**
   * Delegates to the validator created by {@link #makePmValidator()}.
   */
  @Override
  public void pmValidate() {
    getPmMetaData().validator.validate(this);
  }

  /**
   * General validation logic implementations for a {@link PmObject}.
   * <p>
   * Delegates the type specific logic to {@link #validateImpl(PmObjectBase)}.
   *
   * @param <T> The PM type to validate.
   */
  public static class ObjectValidator<T extends PmObjectBase> implements Validator {

    /**
     * Calls {@link #validateImpl(PmObjectBase)}. Translates catched {@link PmValidationException}s
     * to {@link PmMessage}s. For other exceptions {@link PmExceptionHandler#onExceptionInPmValidation(PmObject, RuntimeException)}
     * gets called.
     * <p>
     * If the valid-state of this PM was changed, a {@link PmEvent#VALIDATION_STATE_CHANGE} gets fired.
     */
    @Override
    public final void validate(PmObject pm) {
      @SuppressWarnings("unchecked")
      T ipm = (T) pm;
      if (shouldValidate(ipm)) {
        boolean wasValid = ipm.isPmValid();
        try {
          validateImpl(ipm);
        } catch (PmValidationException e) {
          // XXX: severity will be configurable when we implement the warning concept.
          PmMessageApi.addMessage(ipm, Severity.ERROR, e.getResourceData().msgKey, e.getResourceData().msgArgs);
        } catch (RuntimeException e) {
          ipm.getPmConversationImpl().getPmExceptionHandler().onExceptionInPmValidation(pm, e);
        }
        boolean isValid = ipm.isPmValid();
        if (isValid != wasValid) {
          PmEventApi.firePmEvent(ipm, PmEvent.VALIDATION_STATE_CHANGE);
        }
      }

    }

    /** Provides the set of child PMs to validate too. */
    protected Iterable<PmObject> getChildrenToValidate(T pm) {
      return PmUtil.getPmChildren(pm);
    }

    /**
     * Defines if {@link #validateImpl(PmObjectBase)} should be called for this PM.
     *
     * @param pm The PM to check.
     * @return <code>true</code> if the given PM should be validated.
     */
    protected boolean shouldValidate(T pm) {
      // XXX oboede: Logic will be extended for the task 'validation on read'
      return pm.isPmVisible() &&
             !pm.isPmReadonly();
    }

    /**
     * The type specific validation implementation.
     *
     * @param pm
     *          The PM to validate.
     * @throws PmValidationException
     *           Will be thrown if a validation failed.<br>
     *           Alternatively
     *           {@link PmMessageApi#addMessage(PmObject, Severity, String, Object...)}
     *           may be used to generate multiple messages.
     */
    protected void validateImpl(T pm) throws PmValidationException {
      for (PmObject d : getChildrenToValidate(pm)) {
        d.pmValidate();
      }
    }
  }

  // ======== Static data ======== //

  /**
   * A map of static definitions for presentation model.
   */
  private static Map<Object, MetaData> pmKeyToMetaDataMap = new ConcurrentHashMap<Object, MetaData>();

  /**
   * A reference to the static definition for this presentation model.
   */
  private MetaData pmMetaData;

  /**
   * Provides the PM meta data.
   * <p>
   * Ensures that the PM is competely initialized.
   *
   * @return The meta data for this PM.
   */
  protected final MetaData getPmMetaData() {
    zz_ensurePmInitialization();
    return pmMetaData;
  }

  protected final MetaData getPmMetaDataWithoutPmInitCall() {
    ensurePmMetaDataInitialization();
    assert pmMetaData != null;
    return pmMetaData;
  }

  protected final boolean isMetaDataInitialized() {
    return pmMetaData != null;
  }

  /**
   * Ensures that all static definitions of this model are initialized.
   */
  protected void ensurePmMetaDataInitialization() {
    if (!hasOwnMetaData()) {
      if (pmParent != null) {
        // ensure initialization by calling any external pm-method.
        pmParent.ensurePmMetaDataInitialization();
      }
      // the parent may have generated may metadata.
      if (!hasOwnMetaData()) {
        // Prevent concurrent double initialization.
        // Can't be done on class level because of
        // http://bugs.sun.com/bugdatabase/view_bug.do;jsessionid=82a8144e020c83fd1bd1bd741b6e?bug_id=7031759
        synchronized(pmKeyToMetaDataMap) {
          try {
            zz_initMetaData(pmParent, (String) null, false, false);
          }
          catch (RuntimeException e) {
            PmObjectUtil.throwAsPmRuntimeException(this, e);
          }
        }
      }
    }

    // This may happen if meta data just was assigned from the parent or
    // found in the meta data map.
    // The field bound children need to be initialized too, to get them ready to work
    // without reflection based parent-scans to detect, 'Who I am within my parent?'.
    if (pmInitState == PmInitState.NOT_INITIALIZED) {
      // -- Ensure that all child fields get their data initialized --
      for (int i=0; i<pmMetaData.childFieldAccessorArray.length; ++i) {
        PmObjectBase child = pmMetaData.childFieldAccessorArray[i].getBeanAttrValue(this);
        if (child.pmMetaData == null) {
          child.setPmMetaData(pmMetaData.childFieldMetaDataArray[i]);
        }
      }
      pmInitState = PmInitState.FIELD_BOUND_CHILD_META_DATA_INITIALIZED;

      for (int i=0; i<pmMetaData.childFieldAccessorArray.length; ++i) {
        PmObjectBase child = pmMetaData.childFieldAccessorArray[i].getBeanAttrValue(this);
        if (child.pmInitState == PmInitState.NOT_INITIALIZED) {
          child.ensurePmMetaDataInitialization();
        }
      }
    }
  }

  private void setPmMetaData(MetaData sd) {
    this.pmMetaData = sd;
  }

  /** Checks of the PM has a metadata reference and if it's really the matching one. */
  private final boolean hasOwnMetaData() {
    return pmMetaData != null &&
           // In case of pre-assigned child PM meta data the type may not match (if the
           // embedded PM in place may have different types in different scenarios).
           pmMetaData.id.pmClass == getClass();
  }
  /**
   * Initializes the shared presentation model configuration data that is
   * provided for each PM instance with the given name within the given parent
   * scope.
   * <p>
   * Example: All presentation model for each presentation model instance of
   * "userPm.name" will share the same set of meta information. Such as: the
   * used title provider, option provider etc.
   * <p>
   * To prevent a lot of initialization overhead for each presentation model the
   * shared meta data is only initialized once for all instances that use the
   * same meta data set in application live time.
   *
   * @param parentPm
   *          The parent of the presentation model to initialize.
   * @param name
   *          The name of this presentation model. Is often used to find
   *          annotations attached to a field or getter.
   */
  /* package */ void zz_initMetaData(PmObjectBase parentPm, String name, boolean isPmField, boolean isSubPm) {
    if (!hasOwnMetaData()) {
      String lastKeyPart = (name != null)
                            ? name
                            : pmMetaData != null
                              ? pmMetaData.name // name is known in case of having a more than one type for one child PM.
                              : getClass().getName(); // a PM that is not a field of a parent PM.
      MetaDataId key = new MetaDataId(pmParent, lastKeyPart, getClass());

      setPmMetaData(pmKeyToMetaDataMap.get(key));
      if (!hasOwnMetaData()) {
        // Double check with synchronization to ensure maximum get performance and to ensure that
        // meta data for each PM class gets initialized only once.
        // This can't be done on class level because of http://bugs.sun.com/bugdatabase/view_bug.do;jsessionid=82a8144e020c83fd1bd1bd741b6e?bug_id=7031759
        synchronized (pmKeyToMetaDataMap) {
          setPmMetaData(pmKeyToMetaDataMap.get(key));
          if (!hasOwnMetaData()) {
            MetaData md = makeMetaData();
            md.id = key; // XXX: should be parameter of the factory method. But that needs a cross-project refactoring.
            setPmMetaData(md);

            pmMetaData.name = (name != null)
                                ? name
                                : StringUtils.uncapitalize(getClass().getSimpleName());
            pmMetaData.isPmField = isPmField;
            pmMetaData.isSubPm = isSubPm;

            PmConversation conversation = getPmConversation();
            if (conversation == null) {
              throw new PmRuntimeException(this, "PM without pmConversation found. Please make sure that a conversation exists within the PM parent hierarchy.");
            }
            pmMetaData.init(conversation.getPmDefaults());

            if (pmParent == null &&
                ! (this instanceof PmConversation)) {
              String reportName = name != null ? name : getClass().getSimpleName();
              throw new PmRuntimeException("Unable to initialize PM '" + reportName + "' without defined pmParent.\n" +
                  "Please make sure that the pmParent is defined either by using an initializing constructor\n" +
                  "or by using the method setpmParent(pmParent) before using this instance.\n" +
                  "PM class: " + getClass().getCanonicalName());
            }

            pmMetaData.absoluteName = key.getAbsoluteName();

            // Perform the subclass specific meta data initialization after having defined names.
            try {
              pmMetaData.ensureDerivedNames(this);
              initMetaData(pmMetaData);
            }
            catch (RuntimeException e) {
              throw new PmRuntimeException(this, e);
            }
            pmKeyToMetaDataMap.put(key, pmMetaData);


            // -- Meta data initialization for PM fields --
            List<BeanAttrAccessor> allFields = new ArrayList<BeanAttrAccessor>();
            for (Field f : ClassUtil.getAllFields(getClass())) {
              // XXX olaf: Currently only public fields are considered.
              if ((f.getModifiers() & Modifier.PUBLIC) != 0 &&
                  (f.getModifiers() & Modifier.STATIC) == 0) {

                BeanAttrAccessor accessor = new BeanAttrAccessorImpl(getClass(), f);
                Object fieldValue = accessor.getBeanAttrValue(this);

                if (fieldValue instanceof PmObject) {
                  if ((f.getModifiers() & Modifier.FINAL) == 0) {
                    LOG.warn("PM field has no 'final' declaration. " +
                        "Please check if it may be declared this way. The field: " + f);
                  }

                  allFields.add(accessor);
                }
              }
            }

            int numOfPmFields = allFields.size();
            if (numOfPmFields > 0) {
              pmMetaData.childFieldAccessorArray = new BeanAttrAccessor[numOfPmFields];
              allFields.toArray(pmMetaData.childFieldAccessorArray);

              pmMetaData.childFieldMetaDataArray = new MetaData[numOfPmFields];
              pmMetaData.nameToChildAccessorMap = new HashMap<String, BeanAttrAccessor>(numOfPmFields);
              for (int i=0; i<pmMetaData.childFieldAccessorArray.length; ++i) {
                BeanAttrAccessor a = pmMetaData.childFieldAccessorArray[i];
                String attrName = a.getName();
                PmObjectBase child = (PmObjectBase)a.getBeanAttrValue(this);
                child.zz_initMetaData(this, attrName, true, true);
                pmMetaData.childFieldMetaDataArray[i] = child.pmMetaData;
                pmMetaData.nameToChildAccessorMap.put(attrName, a);
              }
              pmInitState = PmInitState.FIELD_BOUND_CHILD_META_DATA_INITIALIZED;

              for (int i=0; i<pmMetaData.childFieldAccessorArray.length; ++i) {
                PmObjectBase child = pmMetaData.childFieldAccessorArray[i].getBeanAttrValue(this);
                if (child.pmInitState == PmInitState.NOT_INITIALIZED) {
                  child.ensurePmMetaDataInitialization();
                }
              }

            }
          }
        }
      }
    }

    if (pmMetaData == null) {
      throw new PmRuntimeException("failed to initialize PM '" + name +
          "'. PM class=" + getClass() +
          (parentPm != null ? " parentPm=" + parentPm : "")
          );
    }
  }

  /**
   * Gets called when the meta data part of this PM is initialized and assigned
   * to this instance.
   */
  protected void onPmInit() {
  };

  /**
   * INTERNAL method.<br>
   * Initializes this PM runtime instance.
   */
  protected final void zz_ensurePmInitialization() {
    if (pmInitState != PmInitState.INITIALIZED) {
      if (pmInitState == PmInitState.BEFORE_ON_PM_INIT) {
        if (LOG.isDebugEnabled())
          LOG.debug("Pm interface usage within an 'onPmInit()' in PM: " + PmUtil.getPmLogString(this));
      }
      else {
        ensurePmMetaDataInitialization();

        synchronized(this) {
          // ensure strict top-down initialization of the PM tree.
          if (pmParent != null) {
            pmParent.zz_ensurePmInitialization();
          }

          if (pmInitState.ordinal() < PmInitState.BEFORE_ON_PM_INIT.ordinal()) {
            for (DiResolver d : pmMetaData.diResolvers) {
              d.resolveDi(this);
            }
            pmInitState = PmInitState.BEFORE_ON_PM_INIT;
            try {
              pmMetaData.onPmInit(this);
              onPmInit();
              for (Method method : pmMetaData.initMethods) {
                method.invoke(this, new Object[] {});
              }
            }
            catch (Exception e) {
              pmInitState = PmInitState.FIELD_BOUND_CHILD_META_DATA_INITIALIZED;
              throw PmRuntimeException.asPmRuntimeException(this, e);
            }
            pmInitState = PmInitState.INITIALIZED;
          }
        }
      }
    }
  }

  /**
   * Gets called when the meta data instance for this presentation model is
   * not yet available (first call within the VM live time).
   * <p>
   * Subclasses that provide more specific meta data should override this
   * method to provide their meta data information container.
   *
   * @return A PM type specific static data container for this presentation model.
   */
  protected MetaData makeMetaData() {
    return new MetaData();
  }


  @SuppressWarnings({ "rawtypes", "deprecation" })
  protected void initMetaData(MetaData metaData) {
    
    // -- Enablement & visibility configuration --
    PmObjectCfg objectCfg = AnnotationUtil.findAnnotation(this, PmObjectCfg.class);
    if (objectCfg != null) {
      metaData.enablementCfg = objectCfg.enabled();
      metaData.visibilityCfg = objectCfg.visible();
    }
    
    // -- Language resource configuration --
    PmTitleCfg annotation = AnnotationUtil.findAnnotation(this, PmTitleCfg.class);
    if (annotation != null) {
      metaData.resKey = StringUtils.defaultIfEmpty(annotation.resKey(), null);
      metaData.resKeyBase = StringUtils.defaultIfEmpty(annotation.resKeyBase(), null);
      metaData.tooltipUsesTitle = annotation.tooltipUsesTitle();

      if (!annotation.titleProvider().equals(Void.class)) {
        try {
          metaData.pmTitleProvider = (PmTitleProvider) annotation.titleProvider().newInstance();
        } catch (Exception e) {
          throw new PmRuntimeException(this, e);
        }
      } else if (StringUtils.isNotBlank(annotation.attrValue())) {
        metaData.pmTitleProvider = new TitleProviderAttrValueBased(annotation.attrValue(), this instanceof PmElement);
      }
      // TODO: check if only a tooltip or icon is provided...
      else if (! "".equals(annotation.title())) {
        metaData.pmTitleProvider = new PmTitleProviderValuebased(
            annotation.title(), annotation.tooltip(), annotation.icon());
      }
    }

    if (metaData.resKeyBase == null) {
      metaData.resKeyBase = metaData.isSubPm
            ? pmParent.getPmResKeyBase() + "." + metaData.name
            : StringUtils.uncapitalize(ClassUtils.getShortClassName(getClass()));
    }


    if (metaData.resKey == null) {
      metaData.resKey = metaData.resKeyBase;
    }

    // Default title provider:
    if (metaData.pmTitleProvider == null) {
      metaData.pmTitleProvider = getPmConversation().getPmDefaults().getPmTitleProvider();

      if (metaData.pmTitleProvider == null) {
        throw new PmRuntimeException(this, "title provider is null.");
      }
    }

    // -- Bean Factory --
    PmFactoryCfg factoryAnnotation = AnnotationUtil.findAnnotation(this, PmFactoryCfg.class);
    if (factoryAnnotation != null) {
      if (factoryAnnotation.beanPmClasses().length > 0) {
        metaData.pmElementFactory = new BeanPmFactory(factoryAnnotation.beanPmClasses());
      }
      else {
        throw new IllegalArgumentException("Missing items in beanPmClasses of annotation " +
            PmFactoryCfg.class.getSimpleName() + " in specification of " + PmUtil.getPmLogString(this));
      }
    }

    // -- Cache configuration --
    List cacheAnnotations = InternalPmCacheCfgUtil.findCacheCfgsInPmHierarchy(this, new ArrayList());
    if (!cacheAnnotations.isEmpty()) {
      metaData.titleCache = InternalPmCacheCfgUtil.readCacheMetaData(this, CacheKind.TITLE, cacheAnnotations, InternalCacheStrategyFactory.INSTANCE);
      metaData.enablementCache = InternalPmCacheCfgUtil.readCacheMetaData(this, CacheKind.ENABLEMENT, cacheAnnotations, InternalCacheStrategyFactory.INSTANCE);
      metaData.visibilityCache = InternalPmCacheCfgUtil.readCacheMetaData(this, CacheKind.VISIBILITY, cacheAnnotations, InternalCacheStrategyFactory.INSTANCE);
      metaData.cacheClearBehavior = DeprInternalPmCacheCfgUtil.evaluateCacheClearBehavior(this, cacheAnnotations);
    }

    // -- Check for registered domain specific annotations
    metaData.permissionAnnotations = AnnotationUtil.findAnnotations(this, PmAnnotationApi.getPermissionAnnotations()).toArray(new Annotation[0]);

    // -- Dependency injection configuration --
    metaData.diResolvers = DiResolverUtil.getDiResolvers(getClass());

    // -- collect all methods annotated with @PmInit
    metaData.initMethods = findInitMethods();

    // -- validator strategy --
    metaData.deprValidation = isDeprValidation();
    metaData.validator = makePmValidator();
    assert metaData.validator != null;
  }

  /**
   * @return all methods in the class hierarchy of this PM that are annotated with {@link PmInit}.
   */
  private List<Method> findInitMethods() {
    List<Method> initMethods = ClassUtil.findAnnotatedMethodsTopDown(this.getClass(), PmInit.class);
    Map<String, Method> nameToMethodMap = new HashMap<String, Method>();

    for (Iterator<Method> iter = initMethods.listIterator(); iter.hasNext();) {
      Method method = iter.next();
      // only no-arg methods are allowed
      if (method.getParameterTypes().length > 1) {
        throw new IllegalArgumentException("Methods annotated with '" + PmInit.class
            + "' can not have parameters. This is not true for '" + method + "'. Please rafactore the method!");
      }
      // no static methods are allowed
      if (Modifier.isStatic(method.getModifiers())) {
        throw new IllegalArgumentException("Methods annotated with '" + PmInit.class
            + "' must not be static. This is not true for '" + method
            + "'. Please rafactore the method!");
      }
      // only public and protected methods are allowed
      if (!Modifier.isPublic(method.getModifiers()) && !Modifier.isProtected(method.getModifiers())) {
        throw new IllegalArgumentException("Methods annotated with '" + PmInit.class
            + "' must be public or protected. This is not true for '" + method
            + "'. Please change the method visibility!");
      }
      // If a sub class overrides an annotated super class init method it must
      // be ensured that the init method is only called once.
      if (nameToMethodMap.get(method.getName()) != null) {
        iter.remove();
      } else {
        nameToMethodMap.put(method.getName(), method);
      }
      // if onPmInit is annotated, do not call it twice
      if (method.getName().equals("onPmInit")) {
        iter.remove();
      }
    }

    return initMethods;
  }

  /**
   * Shared PM meta data for all attributes of the same kind. E.g. for all
   * 'myapp.User.name' attributes.
   */
  protected static class MetaData {

    protected static final char NAME_PATH_DELIMITER = '_';

    public MetaData() {}

    /**
     * Provides PM type specific initialization logic. Intended for defining common PM types.
     * Gets called before domain specific initialization.
     * <p>
     * To provide domain specific PM initialization logic please use {@link PmObjectBase#onPmInit()}.
     *
     * @param pm The PM to initialize.
     */
    protected void onPmInit(PmObjectBase pm) {
      InternalPmCacheCfgUtil.registerClearOnListeners(pm, CacheKind.TITLE, titleCache.cacheClearOn);
      InternalPmCacheCfgUtil.registerClearOnListeners(pm, CacheKind.ENABLEMENT, enablementCache.cacheClearOn);
      InternalPmCacheCfgUtil.registerClearOnListeners(pm, CacheKind.VISIBILITY, visibilityCache.cacheClearOn);
    }

    /** Initializes the some attributes based on PM-default settings. */
    protected void init(PmDefaults pmDefaults) {
      this.addErrorMessagesToTooltip = pmDefaults.addErrorMessagesToTooltip;
    }

    private MetaDataId id;
    private String name;
    private String compositeChildName;
    private String relativeName;
    private String absoluteName;
    boolean isPmField;
    private boolean isSubPm;

    @SuppressWarnings("rawtypes")
    private PmTitleProvider pmTitleProvider;

    private boolean readOnly = false;

    private String resKey;
    private String resKeyBase;
    private List<Class<?>> resLoaderCtxtClasses;
    private boolean tooltipUsesTitle = false;
    private boolean addErrorMessagesToTooltip;

    boolean deprValidation;
    private Validator validator;
    private Annotation[] permissionAnnotations = {};
    Enable enablementCfg = Enable.DEFAULT;
    Visible visibilityCfg = Visible.DEFAULT;

    private InternalPmCacheCfgUtil.CacheMetaData titleCache = InternalPmCacheCfgUtil.CacheMetaData.NO_CACHE;
    private InternalPmCacheCfgUtil.CacheMetaData enablementCache = InternalPmCacheCfgUtil.CacheMetaData.NO_CACHE;
    private InternalPmCacheCfgUtil.CacheMetaData visibilityCache = InternalPmCacheCfgUtil.CacheMetaData.NO_CACHE;
    @Deprecated /* package */ Clear   cacheClearBehavior         = Clear.DEFAULT;

//    private boolean cacheTooltip = false;
    /** An optional factory that is responsible for creating PMs for beans. */
    private BeanPmFactory pmElementFactory;

    private DiResolver[] diResolvers;

    /**
     * An array that allows faster initialization of child PM's. After
     * initialization of the first element, they can get their meta data just
     * by array position access.<br>
     * That method is very efficient, because it prevents the permanent call of
     * attribute name detection algorithm. Which takes some time...
     */
    private MetaData[] childFieldMetaDataArray = {};

    /**
     * The set of fields or get/set-methods to access the child PM's.<br>
     * Provides generic top-down access to fix (field bound) PM structure.
     */
    private BeanAttrAccessor[] childFieldAccessorArray = {};
    private Map<String, BeanAttrAccessor> nameToChildAccessorMap = Collections.emptyMap();

    /** all methods annotated with {@link PmInit} */
    private List<Method> initMethods;

    public String getName() { return name; }
    /* package */ String getAbsoluteName() { return absoluteName; }

    @SuppressWarnings("rawtypes")
    public PmTitleProvider getPmTitleProvider() {
      return pmTitleProvider;
    }

    public void setPmTitleProvider(@SuppressWarnings("rawtypes") PmTitleProvider pmTitleProvider) {
      assert pmTitleProvider != null;

      this.pmTitleProvider = pmTitleProvider;
    }

    public BeanPmFactory getPmElementFactory() {
      return pmElementFactory;
    }

    public void setPmElementFactory(BeanPmFactory pmElementFactory) {
      this.pmElementFactory = pmElementFactory;
    }

    public Enable getEnablementCfg() {
      return this.enablementCfg;
    }
    
    public Visible getVisibilityCfg() {
      return this.visibilityCfg;
    }
    
    /* package */ void setDefaultEnablementCfg(Enable defaultEnablementCfg) {
      this.enablementCfg = defaultEnablementCfg;
    }
    
    /* package */ void setDefaultVisibilityCfg(Visible defaultVisibilityCfg) {
      this.visibilityCfg = defaultVisibilityCfg;
    }
    
    public boolean isReadOnly() { return readOnly; }
    public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }

    /** @return The {@link Validator} for this PM. */
    public Validator getValidator() { return validator; }

    /** @param validator The {@link Validator} for this PM. */
    public void setValidator(Validator validator) { this.validator = validator; }

    // TODO: change to a hierarchical name structure. is simpler and faster.
    void ensureDerivedNames(PmObjectBase pmObject) {
      compositeChildName = isSubPm
          ? (pmObject.pmParent.getPmCompositeChildName().length() == 0)
              ? name
              : StringUtils.join(new String[]{ pmObject.pmParent.getPmCompositeChildName(), name }, MetaData.NAME_PATH_DELIMITER)
          : "";

      relativeName = isSubPm
          ? compositeChildName
          : StringUtils.replaceChars(
              StringUtils.uncapitalize(ClassUtils.getShortClassName(pmObject.getClass())),
              '.', MetaData.NAME_PATH_DELIMITER);


    }


    /**
     * Internal identifier used as key for the shared {@link PmObjectBase#pmKeyToMetaDataMap}.
     * <p>
     * Identifies metadata by {@link #parentId}, {@link #name} and {@link #pmClass}.<br>
     * The {@link #pmClass} part allows having multiple meta data definitions for a single id path.
     * This is needed to support polymorph PMs having scenario specific types.
     */
    public static class MetaDataId {
      final MetaDataId parentId;
      final String name;
      /** In case of polymorph factory generated PMs a PM field may be used for various types having different meta data. */
      final Class<?> pmClass;
      final int hashCode;

      public MetaDataId(PmObjectBase parentPm, String name, Class<?> pmClass) {
        Validate.notEmpty(name);
        Validate.notNull(pmClass);
        if (parentPm != null) {
          if (parentPm.pmMetaData.id == null) {
            throw new IllegalStateException("Parent id should be generated before child PM id.");
          }
          this.parentId = parentPm.pmMetaData.id;
        } else {
          this.parentId = null;
        }

        this.name = name;
        this.pmClass = pmClass;
        this.hashCode = new HashCodeBuilder(68993, 17).append(parentId).append(name).append(pmClass).toHashCode();
      }

      @Override
      public boolean equals(Object obj) {
        if (obj == this) {
          return true;
        }
        if (obj == null) {
          return false;
        }
        MetaDataId other = (MetaDataId) obj;
        return hashCode == other.hashCode &&
               ObjectUtils.equals(parentId, other.parentId) &&
               name.equals(other.name) &&
               pmClass.equals(other.pmClass);
      }

      @Override
      public int hashCode() {
        return hashCode;
      }

      /** @return An underline delimited concatenation of the ID hierarchy. */
      public String getAbsoluteName() {
          return parentId != null
                  ? parentId.toString() + '_' + name
                  : name;
      }

      @Override
      public String toString() {
        return getAbsoluteName();
      }
    }
  }

  /**
   * The default implementation always returns zero.
   *
   * @deprecated PM based compare operations are no longer supported. Please compare the related data objects.
   */
  @Override
  @Deprecated
  public int compareTo(PmObject otherPm) {
    return 0;
  }

  /**
   * Clears:
   * <ul>
   *   <li>All invalid attribute value messages within the scope of this PM.</li>
   *   <li>The not validated attribute values within the scope of this PM.</li>
   * <ul>
   */
  public void clearPmInvalidValues() {
    PmEventApi.ensureThreadEventSource(this);
    PmValidationApi.clearInvalidValuesOfSubtree(this);
  }

  @Override
  public final Set<String> getPmStyleClasses() {
    // TODO olaf: add some caching here
    Set<String> styleClassSet = new TreeSet<String>();
    getPmStyleClassesImpl(styleClassSet);
    return styleClassSet;
  }

  /**
   * Provides subclass specific style class definitions.
   * <p>
   * This base implementation provides style classes for active messages
   * that are related to this PM.<br>
   * See: {@link Severity#getStyleClass()}.
   *
   *
   * @param styleClassSet The container to add the style classes to.
   */
  protected void getPmStyleClassesImpl(Set<String> styleClassSet) {
    for (PmMessage m : PmMessageApi.getMessages(this)) {
      styleClassSet.add(m.getSeverity().getStyleClass());
    }
  }

  @Override
  public Object getPmProperty(String propName) {
    return pmProperties.get(propName);
  }

  @Override
  public void setPmProperty(String propName, Object value) {
    if (pmProperties.isEmpty()) {
      pmProperties = new ConcurrentHashMap<String, Object>();
    }
    if (value == null) {
      pmProperties.remove(propName);
    } else {
      pmProperties.put(propName, value);
    }
  }

  // ====== dynamic pm support ====== //

  /**
   * Adds the given PM as a named member of this PM composite.
   * <p>
   * Has the same effect as declaring a composite PM in a public final field
   * of its parent.
   */
  protected void addToPmComposite(String name, PmObject pm) {
    if (pm.getPmParent() != this) {
      throw new PmRuntimeException(this, "The added child '" + name + "' has not the expected parent.");
    }

    if (pm instanceof PmObjectBase) {
      PmObjectBase pmAsPmBase = (PmObjectBase) pm;
      if (pmAsPmBase.isMetaDataInitialized()) {
        throw new PmRuntimeException(this, "Added child '" + name + "' is already initialized.\n" +
            "Please make sure that no PM-method was called on the object before adding it to its parent.");
      }

      pmAsPmBase.zz_initMetaData(this, name, false, true);
    }

    if (pmDynamicSubPms == PmDynamicSubPms.EMPTY_INSTANCE) {
      pmDynamicSubPms = new PmDynamicSubPms();
    }
    pmDynamicSubPms.addPm(name, pm);
  }

  protected void removePmChild(PmObject pm) {
    pmDynamicSubPms.removePm(pm);
  }

  private PmDynamicSubPms pmDynamicSubPms = PmDynamicSubPms.EMPTY_INSTANCE;

  /**
   * A data structure that exists only in case of a PM with dynamic sub-PMs.
   */
  static class PmDynamicSubPms {
    public static final PmDynamicSubPms EMPTY_INSTANCE = new PmDynamicSubPms() {
      @Override public void addPm(String arg0, PmObject arg1) {
        throw new UnsupportedOperationException();
      }
    };

    private List<PmObject> all = Collections.emptyList();
    private Map<String, PmObject> nameToPmMap = Collections.emptyMap();

    public void addPm(String name, PmObject pm) {
      if (!ObjectUtils.equals(name, pm.getPmName())) {
        throw new PmRuntimeException("Illegal attempt to register PM with the name '" +
            pm.getPmName() + "' under the name '" + name + "'. PM class: " + pm.getClass());
      }
      if (nameToPmMap.isEmpty()) {
        nameToPmMap = new HashMap<String, PmObject>();
      }
      else if (nameToPmMap.containsKey(name)) {
        throw new PmRuntimeException("A PM child with the name '" +
            name + "' already exists.");
      }
      nameToPmMap.put(name, pm);

      if (all.isEmpty())
        all = new ArrayList<PmObject>();
      all.add(pm);
    }

    public void removePm(PmObject pm) {
      nameToPmMap.remove(pm.getPmName());
      all.remove(pm);
    }

  }

  public static interface NameBuilder {
    String makeName(PmObjectBase pm);
  }

  public static class NameBuilderAbsoluteName implements NameBuilder {
    public static final NameBuilder INSTANCE = new NameBuilderAbsoluteName();
    @Override
    public String makeName(PmObjectBase pm) {
      return PmUtil.getAbsoluteName(pm);
    }
  }

  public static class NameBuilderShortName implements NameBuilder {
    public static final NameBuilder INSTANCE = new NameBuilderShortName();
    @Override
    public String makeName(PmObjectBase pm) {
      return pm.getPmName();
    }
  }

  /** Logs the the relative PM name and a hash code. */
  public static class NameBuilderRelNameWithHashCode implements NameBuilder {
    public static final NameBuilder INSTANCE = new NameBuilderRelNameWithHashCode();
     @Override
      public String makeName(PmObjectBase pm) {
          return pm.getPmRelativeName() + "(" + Integer.toHexString(pm.hashCode()) + ")";
      }
  }

  public static class NameBuilderTitle implements NameBuilder {
    public static final NameBuilder INSTANCE = new NameBuilderTitle();
    @Override
    public String makeName(PmObjectBase pm) {
      String title = null;

      try {
        title = pm.getPmTitle();
      }
      catch (Exception e) {
        // possibly a toString call before completed presentation model initialization.
        LOG.warn("Unable to resolve title for :" + PmUtil.getPmLogString(pm), e);
      }

      return title != null
                ? title
                : PmUtil.getPmLogString(pm);
    }
  }

} // end of PmObjectBase


/**
 * A container for registered event listener - event mask pairs.
 * <p>
 * FIXME olaf: Currently the listener references are weak.
 *             An binding implementation with complete unbind support should be able
 *             to handle strong references too...
 * <p>
 * XXX olaf: The implementation is not really optimized for size and speed.
 *           Have a look the SWT EventTable for a better performing implementation.<br>
 *           Usage of the {@link CopyOnWriteArraySet} may also be a good choice.
 *
 * @author olaf boede
 */
class PmEventTable {
  private static final Log log = LogFactory.getLog(PmEventTable.class);

  private final Map<PmEventListener, Integer> pmEventListeners;

  public PmEventTable(boolean isWeak) {
    // Initial map size reduced to 2 to optimize memory consumption.
    // XXX oboede: further memory reduction may be achieved by performing standard observer operations
    // in methods instead of using observer instances. See PmDataInputBase ctor.
    // A table example test pointed out that this would reduce the sample screen PM footprint by 15%.
    pmEventListeners = isWeak
          ? new WeakHashMap<PmEventListener, Integer>(2)
          : new HashMap<PmEventListener, Integer>(2);
  }

  public synchronized void addListener(int eventMask, PmEventListener listener) {
    Integer foundMask = pmEventListeners.get(listener);

    if (foundMask == null) {
      pmEventListeners.put(listener, eventMask);
    }
    else {
      if (foundMask == eventMask) {
        log.warn("Duplicate listerner registration call. Listener: " + listener);
      }

      int newMask = foundMask | eventMask;
      pmEventListeners.put(listener, newMask);
    }
  }

  public synchronized void removeListener(PmEventListener listener) {
    pmEventListeners.remove(listener);
  }

  public synchronized void removeListener(int eventMask, PmEventListener listener) {
    Integer foundMask = pmEventListeners.get(listener);

    if (foundMask != null) {
      int negEventMask = (eventMask ^ PmEvent.ALL);
      int newMask = foundMask.intValue() & negEventMask;
      if (newMask == 0) {
        pmEventListeners.remove(listener);
      }
      else {
        pmEventListeners.put(listener, newMask);
      }
    }
  }

  /**
   * @param event the event to handle.
   * @param preProcess if set to <code>true</code>, only the pre process part will be done for each listener.<br>
   *                   if set to <code>false</code>, only the handle part will be done for each listener.<br>
   */
  /* package */ void fireEvent(final PmEvent event, boolean preProcess) {
    boolean hasListeners = !pmEventListeners.isEmpty();

    if (log.isTraceEnabled())
      log.trace("fireChange[" + event + "] for event source   : " + PmEventApi.getThreadEventSource() +
          (hasListeners ? "\n\teventListeners: " + pmEventListeners : ""));

    if (hasListeners) {
      boolean isPropagationEvent = event.isPropagationEvent();
      // copy the listener list to prevent problems with listener
      // set changes within the notification processing loop.
      for (Map.Entry<PmEventListener, Integer> e : eventListenersCopy()) {
        // could be null because of WeakReferences.
        if(e == null || e.getValue() == null) {
          continue;
        }
        int listenerMask = e.getValue().intValue();
        boolean isPropagationListener = ((listenerMask & PmEvent.IS_EVENT_PROPAGATION) != 0);
        // Propagation events have to be passed only to listeners that observe that special flag.
        // Standard events will be passed to listeners that don't have set this flag.
        boolean listenerMaskMatch = (listenerMask & event.getChangeMask()) != 0;
        if (listenerMaskMatch &&
            (isPropagationEvent == isPropagationListener)) {
          if (preProcess) {
            // XXX olaf: prevent this permanent base class check to optimize runtime.
            if (e.getKey() instanceof PmEventListener.WithPreprocessCallback) {
              ((PmEventListener.WithPreprocessCallback)e.getKey()).preProcess(event);
            }
          } else {
            e.getKey().handleEvent(event);
          }
        }
      }
    }
  }

  boolean isEmpty() {
    return pmEventListeners.isEmpty();
  }

  /** Provides a shallow copy of all event listener entries. Used to prevent concurrent
   * modification problems with listeners changing the event listener set. */
  @SuppressWarnings("unchecked")
  private synchronized Map.Entry<PmEventListener, Integer>[] eventListenersCopy() {
    return pmEventListeners.entrySet().toArray(new Map.Entry[pmEventListeners.size()]);
  }
} // end of PmEventTable

/**
 * Core internal utility for common internal tasks.
 *
 * @author olaf boede
 */
final class PmObjectUtil {

  /**
   * Tries to ensure that the given PM context gets reported within the thrown exception.
   *
   * @param pm The PM to report.
   * @param e The exception that caused the throw activity.
   */
  static void throwAsPmRuntimeException(PmObjectBase pm, RuntimeException e) {
    // generate the most informative exception
    if (e instanceof PmRuntimeException) {
      throw e;
    }
    else {
      throw new PmRuntimeException(pm, e);
    }
  }

}


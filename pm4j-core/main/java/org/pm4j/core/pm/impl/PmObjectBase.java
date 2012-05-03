package org.pm4j.core.pm.impl;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.util.collection.MapUtil;
import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAspect;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmDefaults;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.PmVisitor;
import org.pm4j.core.pm.annotation.PmCacheCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg.CacheMode;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.impl.cache.PmCacheLog;
import org.pm4j.core.pm.impl.cache.PmCacheStrategy;
import org.pm4j.core.pm.impl.cache.PmCacheStrategyBase;
import org.pm4j.core.pm.impl.cache.PmCacheStrategyNoCache;
import org.pm4j.core.pm.impl.cache.PmCacheStrategyRequest;
import org.pm4j.core.pm.impl.commands.PmCommandSeparator;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;
import org.pm4j.core.pm.impl.pathresolver.PmExpressionPathResolver;
import org.pm4j.core.pm.impl.title.PmTitleProvider;
import org.pm4j.core.pm.impl.title.PmTitleProviderValuebased;
import org.pm4j.core.pm.impl.title.TitleProviderAttrValueBased;
import org.pm4j.core.util.reflection.BeanAttrAccessor;
import org.pm4j.core.util.reflection.BeanAttrAccessorImpl;
import org.pm4j.core.util.reflection.BeanAttrArrayList;
import org.pm4j.core.util.reflection.ClassUtil;

/**
 * Provides base functionality if presentation model classes.
 *
 * @author olaf boede
 */
public abstract class PmObjectBase implements PmObject {

  private static final Log LOG = LogFactory.getLog(PmObjectBase.class);

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
  private Object pmVisibleCache;

  /** Enabled state flag. */
  private boolean pmEnabled = true;
  private Object pmEnabledCache;

  /**
   * Optional title cache.
   */
  private String pmCachedTitle;

  enum PmInitState {
    NOT_INITIALIZED,
    FIELD_BOUND_CHILD_META_DATA_INITIALIZED,
    BEFORE_ON_PM_INIT,
    INITIALIZED };

  /** Helper indicator that prevents double initialization. */
  PmInitState pmInitState = PmInitState.NOT_INITIALIZED;

  /**
   * An optional cache for the bean to PM association within the current PM hierarchy scope.
   */
  /* package */ BeanPmCache pmBeanFactoryCache;

  /** A container for application/user specific additional information. */
  private Map<String, Object> pmProperties = Collections.emptyMap();

 /** Logger for cache usage statistics. */
  protected static final PmCacheLog pmCacheLog = new PmCacheLog();


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
  public String getPmShortTitle() {
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

      for (PmMessage m : PmMessageUtil.getPmErrors(this))
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
    if (this.pmParent != null)
      throw new PmRuntimeException(this, "pmParent is already set.");
    if (pmParent == this)
      throw new PmRuntimeException("Can't use a self reference as pmParent.");

    this.pmParent = (PmObjectBase)pmParent;

  }

  @Override
  public final boolean isPmVisible() {
    PmCacheStrategy strategy = getPmMetaData().cacheStrategyForVisibility;
    Object v = strategy.getCachedValue(this);

    if (v != PmCacheStrategy.NO_CACHE_VALUE) {
      // just return the cache hit (if there was one)
      return (Boolean) v;
    }
    else {
      return (Boolean) strategy.setAndReturnCachedValue(this, isPmVisibleImpl());
    }
  }

  protected boolean isPmVisibleImpl() {
    return pmVisible;
  }

  @Override
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
    PmCacheStrategy strategy = getPmMetaData().cacheStrategyForEnablement;
    Object e = strategy.getCachedValue(this);

    if (e != PmCacheStrategy.NO_CACHE_VALUE) {
      // just return the cache hit (if there was one)
      return (Boolean) e;
    }
    else {
      return (Boolean) strategy.setAndReturnCachedValue(this, isPmEnabledImpl());
    }
  }

  protected boolean isPmEnabledImpl() {
    return pmEnabled;
  }

  @Override
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
    List<PmMessage> msgList = getPmConversationImpl().getPmMessages(this, Severity.ERROR);
    return msgList.isEmpty();
  }

  /**
   * Please override {@link #isPmReadonlyImpl()} to provide your specific logic.
   */
  @Override
  public final boolean isPmReadonly() {
    return isPmReadonlyImpl();
  }

  /**
   * Sub classes may implement their specific read-only definition here.
   *
   * @return <code>true</code> if the PM is in read-only state.
   */
  protected boolean isPmReadonlyImpl() {
    return getPmMetaData().readOnly;
  }

  /**
   * @return The key for language specific resources.
   */
  public String getPmResKey() {
    return getPmMetaDataWithoutPmInitCall().resKey;
  }

  // TODO: doku!
  public String getPmResKeyBase() {
    return getPmMetaDataWithoutPmInitCall().resKeyBase;
  }

  @Override
  public final String getPmTitle() {
    PmCacheStrategy strategy = getPmMetaData().cacheStrategyForTitle;
    Object title = strategy.getCachedValue(this);

    if (title != PmCacheStrategy.NO_CACHE_VALUE) {
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
   *
   * @deprecated Please use PmAttr.beforeValueChange/afterValueChange or an PmEventListener to {@link PmEvent#VALUE_CHANGE} instead.
   */
  @Deprecated
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
  protected void clearCachedPmValues(Set<PmCacheApi.CacheKind> cacheSet) {
    // Has no effect for fresh instances.
    if (pmInitState != PmInitState.INITIALIZED)
      return;

    MetaData sd = getPmMetaData();

    if (cacheSet.contains(PmCacheApi.CacheKind.ENABLEMENT))
      sd.cacheStrategyForEnablement.clear(this);

    if (cacheSet.contains(PmCacheApi.CacheKind.VISIBILITY))
      sd.cacheStrategyForVisibility.clear(this);

    if (cacheSet.contains(PmCacheApi.CacheKind.TITLE))
      sd.cacheStrategyForTitle.clear(this);

    for (PmObject p : getPmChildren()) {
      PmCacheApi.clearCachedPmValues(p, cacheSet);
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
   * @return The class that can be used to find resources (string resources icons...).
   */
  public List<Class<?>> getPmResLoaderCtxtClasses() {
    return getPmMetaDataWithoutPmInitCall().resLoaderCtxtClasses;
  }

  protected List<PmCommand> getVisiblePmCommands() {
    return PmCommandSeparator.filterVisibleCommandsAndSeparators(zz_getPmCommands());
  }

  protected List<PmCommand> getVisiblePmCommands(PmCommand.CommandSet commandSet) {
    return getVisiblePmCommands();
  }

  /**
   * @return All available commands, even commands that are not visible in the UI.
   */
  /* package */ List<PmCommand> zz_getPmCommands() {
    return BeanAttrArrayList.makeList(this, getPmMetaData().childFieldCommandAccessorArray, pmDynamicSubPms.commands);
  }

  /* package */ List<PmObject> getPmChildren() {
    return BeanAttrArrayList.makeList(this, getPmMetaData().childFieldAccessorArray, pmDynamicSubPms.all);
  }

  /* package */ List<PmDataInput> zz_getPmDataInputPms() {
    return BeanAttrArrayList.makeList(this, getPmMetaData().childFieldDataInputAccessorArray, pmDynamicSubPms.dataInputPms);
  }

  /* package */ List<PmAttr<?>> zz_getPmAttributes() {
    return BeanAttrArrayList.makeList(this, getPmMetaData().childFieldAttrAccessorArray, pmDynamicSubPms.attrs);
  }

  /* package */ List<PmElement> zz_getPmElements() {
    return BeanAttrArrayList.makeList(this, getPmMetaData().childFieldElementAccessorArray, pmDynamicSubPms.elements);
  }

  /* package */ List<PmTableCol> zz_getPmColumns() {
    return BeanAttrArrayList.makeList(this, getPmMetaData().childFieldColumnAccessorArray, pmDynamicSubPms.columns);
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

  Serializable getPmContentAspect(PmAspect aspect) {
    switch (aspect) {
      case TITLE:   return getPmTitle();
      case TOOLTIP: return getPmTooltip();
      default: throw new PmRuntimeException(this, "Unsupported PmAspect: " + aspect);
    }
  }

  void setPmContentAspect(PmAspect aspect, Serializable value) throws PmConverterException {
    switch (aspect) {
//      case TITLE:   this.pmCachedTitle = (String)value;
//      case TOOLTIP: this.pm;
      default: throw new PmRuntimeException(this, "Can't set PmAspect: " + aspect);
    }
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
    if (pmMetaData == null) {
      if (pmParent != null) {
        // ensure initialization by calling any external pm-method.
        pmParent.ensurePmMetaDataInitialization();
      }
      if (pmMetaData == null) {
        // prevent concurrent double initialization:
        synchronized(getClass()) {
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

  /**
   * Initializes the shared presentation model configuration data that is
   * provided for each PM instance with the given name within the given paren
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
  private void zz_initMetaData(PmObjectBase parentPm, String name, boolean isPmField, boolean isSubPm) {
    if (pmMetaData == null) {
      String lastKeyPart = (name != null)
                            ? name
                            : getClass().getName();
      String key = (pmParent != null)
                            ? PmUtil.getAbsoluteName(pmParent) + PmObjectBase.MetaData.NAME_PATH_DELIMITER + lastKeyPart
                            : lastKeyPart;

      setPmMetaData(pmKeyToMetaDataMap.get(key));
      if (pmMetaData == null) {

        // With this double synchronization it should be OK to have a non-
        // synchronized meta data map.
        synchronized (getClass()) {
          setPmMetaData(pmKeyToMetaDataMap.get(key));
          if (pmMetaData == null) {
            setPmMetaData(makeMetaData());
            pmMetaData.name = (name != null)
                                ? name
                                : StringUtils.uncapitalize(getClass().getSimpleName());
            pmMetaData.isPmField = isPmField;
            pmMetaData.isSubPm = isSubPm;
            pmMetaData.init(getPmConversation().getPmDefaults());

            if (pmParent == null &&
                ! (this instanceof PmConversation)) {
              String reportName = name != null ? name : getClass().getSimpleName();
              throw new PmRuntimeException("Unable to initialize PM '" + reportName + "' without defined pmParent.\n" +
                  "Please make sure that the pmParent is defined either by using an initializing constructor\n" +
                  "or by using the method setpmParent(pmParent) before using this instance.\n" +
                  "PM class: " + getClass().getCanonicalName());
            }

            pmMetaData.absoluteName = key;

            // Perform the subclass specific meta data initialization after having defined names.
            try {
              initMetaData(pmMetaData);
            }
            catch (RuntimeException e) {
              throw new PmRuntimeException(this, e);
            }
            pmKeyToMetaDataMap.put(key, pmMetaData);


            // -- Meta data initialization for PM fields --
            List<BeanAttrAccessor> allFields = new ArrayList<BeanAttrAccessor>();
            List<BeanAttrAccessor> dataInputFields = new ArrayList<BeanAttrAccessor>();
            List<BeanAttrAccessor> attrFields = new ArrayList<BeanAttrAccessor>();
            List<BeanAttrAccessor> cmdFields = new ArrayList<BeanAttrAccessor>();
            List<BeanAttrAccessor> elemFields = new ArrayList<BeanAttrAccessor>();
            List<BeanAttrAccessor> columnFields = new ArrayList<BeanAttrAccessor>();
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
                  if (fieldValue instanceof PmDataInput)
                    dataInputFields.add(accessor);
                  if (fieldValue instanceof PmAttr)
                    attrFields.add(accessor);
                  if (fieldValue instanceof PmCommand)
                    cmdFields.add(accessor);
                  if (fieldValue instanceof PmElement)
                    elemFields.add(accessor);
                  if (fieldValue instanceof PmTableCol)
                    columnFields.add(accessor);
                }
              }
            }

            int numOfPmFields = allFields.size();
            if (numOfPmFields > 0) {
              pmMetaData.childFieldAccessorArray = new BeanAttrAccessor[numOfPmFields];
              allFields.toArray(pmMetaData.childFieldAccessorArray);
              pmMetaData.childFieldDataInputAccessorArray = new BeanAttrAccessor[dataInputFields.size()];
              dataInputFields.toArray(pmMetaData.childFieldDataInputAccessorArray);
              pmMetaData.childFieldAttrAccessorArray = new BeanAttrAccessor[attrFields.size()];
              attrFields.toArray(pmMetaData.childFieldAttrAccessorArray);
              pmMetaData.childFieldCommandAccessorArray = new BeanAttrAccessor[cmdFields.size()];
              cmdFields.toArray(pmMetaData.childFieldCommandAccessorArray);
              pmMetaData.childFieldElementAccessorArray = new BeanAttrAccessor[elemFields.size()];
              elemFields.toArray(pmMetaData.childFieldElementAccessorArray);
              pmMetaData.childFieldColumnAccessorArray = new BeanAttrAccessor[columnFields.size()];
              columnFields.toArray(pmMetaData.childFieldColumnAccessorArray);

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

  private void initPmResourceAnnotatedFields() {
    for (Map.Entry<Field, PathResolver> e : pmMetaData.fieldInjectionMap.entrySet()) {
      Field f = e.getKey();
      PathResolver r = e.getValue();
      Object value = r.getValue(this);

      if (value == null && ! r.isNullAllowed()) {
        throw new PmRuntimeException(this, "Found value for dependency injection of field '" + f +
            "' was null. But null value is not allowed. " +
            "You may configure null-value handling using @PmInject(nullAllowed=...).");
      }

      try {
        // TODO olaf: Check if there is a public setter to prevent some trouble
        //            in case of enabled security manager...
        if (! f.isAccessible()) {
          f.setAccessible(true);
        }
        f.set(this, value);
      } catch (Exception ex) {
        throw new PmRuntimeException(this, "Can't initialize field '" + f.getName() + "' in class '"
            + getClass().getName() + "'.", ex);
      }
    }

    for (Map.Entry<Method, PathResolver> e : pmMetaData.methodInjectionMap.entrySet()) {
      Method m = e.getKey();
      PathResolver r = e.getValue();
      Object value = r.getValue(this);

      if (value == null && ! r.isNullAllowed()) {
        throw new PmRuntimeException(this, "Found value for dependency injection of method '" + m +
            "' was null. But null value is not allowed. " +
            "You may configure null-value handling using @PmInject(nullAllowed=...).");
      }

      try {
        // TODO olaf: Check if there is a public setter to prevent some trouble
        //            in case of enabled security manager...
        if (! m.isAccessible()) {
          m.setAccessible(true);
        }
        m.invoke(this, value);
      } catch (Exception ex) {
        throw new PmRuntimeException(this, "Can't initialize field '" + m.getName() + "' in class '"
            + getClass().getName() + "' with value '" + value + "'.", ex);
      }
    }
  }

  /**
   * Gets called when the meta data part of this PM is initialized and assigned
   * to this instance.
   */
  protected void onPmInit() {
  };

  /**
   * Initializes this PM runtime instance.
   */
  /* package */ void zz_ensurePmInitialization() {
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
            initPmResourceAnnotatedFields();
            pmInitState = PmInitState.BEFORE_ON_PM_INIT;
            try {
              onPmInit();
            }
            catch (RuntimeException e) {
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
   * @return A static data container for this presentation model.
   */
  protected abstract MetaData makeMetaData();

  @SuppressWarnings("rawtypes")
  protected void initMetaData(MetaData metaData) {
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

    metaData.compositeChildName = metaData.isSubPm
          ? (pmParent.getPmCompositeChildName().length() == 0)
              ? metaData.name
              : StringUtils.join(new String[]{ pmParent.getPmCompositeChildName(), metaData.name }, MetaData.NAME_PATH_DELIMITER)
          : "";

    metaData.relativeName = metaData.isSubPm
          ? metaData.compositeChildName
          : StringUtils.replaceChars(
              StringUtils.uncapitalize(ClassUtils.getShortClassName(getClass())),
              '.', MetaData.NAME_PATH_DELIMITER);

    if (metaData.resKeyBase == null) {
      metaData.resKeyBase = metaData.isSubPm
            ? pmParent.getPmResKeyBase() + "." + metaData.name
            : StringUtils.uncapitalize(ClassUtils.getShortClassName(getClass()));
    }


    // The parent's resource path and the path set of the own inheritance hierarchy.
    // XXX olaf: This code could be optimized to stop in pm4j base classes.
    //           But this would break some pm4j unit tests...
    ArrayList<Class<?>> ownHierarchyClasses = new ArrayList<Class<?>>();
    for (Class<?> c = getClass(); c != null && c != PmObjectBase.class; c = c.getSuperclass()) {
      ownHierarchyClasses.add(c);
    }

    if (metaData.isSubPm) {
      metaData.resLoaderCtxtClasses = new ArrayList<Class<?>>(pmParent.getPmResLoaderCtxtClasses());
      metaData.resLoaderCtxtClasses.addAll(ownHierarchyClasses);
    }
    else {
      metaData.resLoaderCtxtClasses = ownHierarchyClasses;
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
    List<PmCacheCfg> cacheAnnotations = new ArrayList<PmCacheCfg>();
    findAnnotationsInPmHierarchy(PmCacheCfg.class, cacheAnnotations);

    metaData.cacheStrategyForTitle = readCacheStrategy(PmCacheCfg.ATTR_TITLE, cacheAnnotations, CACHE_STRATEGIES_FOR_TITLE);
    metaData.cacheStrategyForVisibility = readCacheStrategy(PmCacheCfg.ATTR_VISIBILITY, cacheAnnotations, CACHE_STRATEGIES_FOR_VISIBILITY);
    metaData.cacheStrategyForEnablement = readCacheStrategy(PmCacheCfg.ATTR_ENABLEMENT, cacheAnnotations, CACHE_STRATEGIES_FOR_ENABLEMENT);

    // -- Dependency injection configuration --
    metaData.fieldInjectionMap = new HashMap<Field, PathResolver>();
    for (Field f : ClassUtil.getAllFields(getClass())) {
      PmInject a = f.getAnnotation(PmInject.class);
      if (a != null) {
        String propName = StringUtils.isNotBlank(a.value())
                            ? a.value()
                            : f.getName();

        PathResolver r = PmExpressionPathResolver.parse(propName, false);
        r.setNullAllowed(a.nullAllowed());

        metaData.fieldInjectionMap.put(f, r);
      }
    }

    metaData.methodInjectionMap = new HashMap<Method, PathResolver>();
    for (Method m : ClassUtil.findMethods(getClass(), "set.*")) {
      PmInject a = m.getAnnotation(PmInject.class);
      if (a != null) {
        String propName = StringUtils.isNotBlank(a.value())
                            ? a.value()
                            : StringUtils.uncapitalize(m.getName().substring(3));

        PathResolver r = PmExpressionPathResolver.parse(propName, false);
        r.setNullAllowed(a.nullAllowed());

        metaData.methodInjectionMap.put(m, r);
      }
    }


    // don't keep unused empty map instances:
    if (metaData.fieldInjectionMap.isEmpty()) {
      metaData.fieldInjectionMap = Collections.emptyMap();
    }
    if (metaData.methodInjectionMap.isEmpty()) {
      metaData.methodInjectionMap = Collections.emptyMap();
    }
  }

  /**
   * Shared PM meta data for all attributes of the same kind. E.g. for all
   * 'myapp.User.name' attributes.
   */
  protected static class MetaData {

    protected static final char NAME_PATH_DELIMITER = '_';

    public MetaData() {}

    /** Initializes the some attributes based on PM-default settings. */
    protected void init(PmDefaults pmDefaults) {
      this.addErrorMessagesToTooltip = pmDefaults.addErrorMessagesToTooltip;
      this.validationChangeEventMask = pmDefaults.validationChangeEventMask;
    }

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

    /**
     * The event mask to be fired on validation state changes.<br>
     * Is configurable to support information about changing style classes,
     * tooltips etc.<br>
     * This allows to fire only a single event with an event mask that
     * informs all relevant listeners.
     */
    public int validationChangeEventMask;

    private PmCacheStrategy cacheStrategyForTitle = PmCacheStrategyNoCache.INSTANCE;
    private PmCacheStrategy cacheStrategyForEnablement = PmCacheStrategyNoCache.INSTANCE;
    private PmCacheStrategy cacheStrategyForVisibility = PmCacheStrategyNoCache.INSTANCE;
//    private boolean cacheTooltip = false;
    /** An optional factory that is responsible for creating PMs for beans. */
    private BeanPmFactory pmElementFactory;

    private Map<Field, PathResolver> fieldInjectionMap;
    private Map<Method, PathResolver> methodInjectionMap;

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
    private BeanAttrAccessor[] childFieldDataInputAccessorArray = {};
    private BeanAttrAccessor[] childFieldAttrAccessorArray = {};
    private BeanAttrAccessor[] childFieldCommandAccessorArray = {};
    private BeanAttrAccessor[] childFieldElementAccessorArray = {};
    private BeanAttrAccessor[] childFieldColumnAccessorArray = {};
    private Map<String, BeanAttrAccessor> nameToChildAccessorMap = Collections.emptyMap();

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

    public boolean isReadOnly() { return readOnly; }
    public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }
  }

  // ======== Annotation support ======== //

  /**
   * Searches an annotation within the attribute-element-session hierarchy. Adds
   * all found annotations to the given collection. Adds nothing when no
   * annotation was found in the hierarchy.
   *
   * @param annotationClass
   *          The annotation to find.
   * @param foundAnnotations
   *          The set to add the found annotations to. The lowest level
   *          annotation (e.g. bound to an attribute) is at the first position.
   *          The highest level annotation (e.g. bound to the root session) is
   *          at the last position.
   */
  protected <T extends Annotation> void findAnnotationsInPmHierarchy(Class<T> annotationClass, Collection<T> foundAnnotations) {
    T cfg = AnnotationUtil.findAnnotation(this, annotationClass);
    if (cfg != null) {
      foundAnnotations.add(cfg);
    }

    if (pmParent != null &&
        ! (this instanceof PmConversation)) {
      pmParent.findAnnotationsInPmHierarchy(annotationClass, foundAnnotations);
    }
  }

  @Override
  public void accept(PmVisitor visitor) {
    visitor.visit(this);
  }

  /**
   * The default implementation always returns zero.
   */
  @Override
  public int compareTo(PmObject otherPm) {
    return 0;
  }

  /**
   * Clears not yet validated values within the scope of this PM.
   */
  public void clearPmInvalidValues() {
    PmEventApi.ensureThreadEventSource(this);
    for (PmObject p : getPmChildren()) {
      ((PmObjectBase)p).clearPmInvalidValues();
    }
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
   *
   * @param styleClassSet The container to add the style classes to.
   */
  protected void getPmStyleClassesImpl(Set<String> styleClassSet) {
    for (PmMessage m : PmMessageUtil.getPmMessages(this)) {
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
    pmProperties.put(propName, value);
  }

  // ====== Cache strategies ====== //

  protected PmCacheStrategy readCacheStrategy(
      String cacheCfgAttrName,
      List<PmCacheCfg> cacheAnnotations,
      Map<CacheMode, PmCacheStrategy> modeToStrategyMap)
  {
    CacheMode cacheMode = AnnotationUtil.getCacheModeFromCacheAnnotations(
        cacheCfgAttrName, cacheAnnotations, CacheMode.OFF);
    PmCacheStrategy s = modeToStrategyMap.get(cacheMode);
    if (s == null) {
      throw new PmRuntimeException(this, "Unable to find cache strategy for CacheMode '" + cacheMode + "'.");
    }
    return s;
  }

  private static final PmCacheStrategy CACHE_TITLE_LOCAL = new PmCacheStrategyBase<PmObjectBase>("CACHE_TITLE_LOCAL") {
    @Override protected Object readRawValue(PmObjectBase pm) {
      return pm.pmCachedTitle;
    }
    @Override protected void writeRawValue(PmObjectBase pm, Object value) {
      pm.pmCachedTitle = (String)value;
    }
    @Override protected void clearImpl(PmObjectBase pm) {
      pm.pmCachedTitle = null;
    }
  };

  private static final PmCacheStrategy CACHE_VISIBLE_LOCAL = new PmCacheStrategyBase<PmObjectBase>("CACHE_VISIBLE_LOCAL") {
    @Override protected Object readRawValue(PmObjectBase pm) {
      return pm.pmVisibleCache;
    }
    @Override protected void writeRawValue(PmObjectBase pm, Object value) {
      pm.pmVisibleCache = value;
    }
    @Override protected void clearImpl(PmObjectBase pm) {
      pm.pmVisibleCache = null;
    }
  };

  private static final PmCacheStrategy CACHE_ENABLED_LOCAL = new PmCacheStrategyBase<PmObjectBase>("CACHE_ENABLED_LOCAL") {
    @Override protected Object readRawValue(PmObjectBase pm) {
      return pm.pmEnabledCache;
    }
    @Override protected void writeRawValue(PmObjectBase pm, Object value) {
      pm.pmEnabledCache = value;
    }
    @Override protected void clearImpl(PmObjectBase pm) {
      pm.pmEnabledCache = null;
    }
  };

  private static final Map<CacheMode, PmCacheStrategy> CACHE_STRATEGIES_FOR_TITLE =
    MapUtil.makeFixHashMap(
      CacheMode.OFF,      PmCacheStrategyNoCache.INSTANCE,
      CacheMode.ON,    CACHE_TITLE_LOCAL,
      CacheMode.REQUEST,  new PmCacheStrategyRequest("CACHE_TITLE_IN_REQUEST", "ti")
    );

  private static final Map<CacheMode, PmCacheStrategy> CACHE_STRATEGIES_FOR_ENABLEMENT =
    MapUtil.makeFixHashMap(
      CacheMode.OFF,      PmCacheStrategyNoCache.INSTANCE,
      CacheMode.ON,    CACHE_ENABLED_LOCAL,
      CacheMode.REQUEST,  new PmCacheStrategyRequest("CACHE_ENABLED_IN_REQUEST", "en")
    );

  private static final Map<CacheMode, PmCacheStrategy> CACHE_STRATEGIES_FOR_VISIBILITY =
    MapUtil.makeFixHashMap(
      CacheMode.OFF,      PmCacheStrategyNoCache.INSTANCE,
      CacheMode.ON,    CACHE_VISIBLE_LOCAL,
      CacheMode.REQUEST,  new PmCacheStrategyRequest("CACHE_VISIBLE_IN_REQUEST", "vi")
    );

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
    private List<PmDataInput> dataInputPms = Collections.emptyList();
    private List<PmAttr<?>> attrs = Collections.emptyList();
    private List<PmCommand> commands = Collections.emptyList();
    private List<PmElement> elements = Collections.emptyList();
    private List<PmTableCol> columns = Collections.emptyList();
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

      if (pm instanceof PmDataInput) {
        if (dataInputPms.isEmpty())
          dataInputPms = new ArrayList<PmDataInput>();
        dataInputPms.add((PmAttr<?>)pm);
      }

      if (pm instanceof PmAttr) {
        if (attrs.isEmpty())
          attrs = new ArrayList<PmAttr<?>>();
        attrs.add((PmAttr<?>)pm);
      }
      else if (pm instanceof PmCommand) {
        if (commands.isEmpty())
          commands = new ArrayList<PmCommand>();
        commands.add((PmCommand)pm);
      }
      else if (pm instanceof PmElement) {
        if (elements.isEmpty())
          elements = new ArrayList<PmElement>();
        elements.add((PmElement)pm);
      }
      else if (pm instanceof PmTableCol) {
        if (columns.isEmpty())
          columns = new ArrayList<PmTableCol>();
        columns.add((PmTableCol)pm);
      }
    }

    public void removePm(PmObject pm) {
      nameToPmMap.remove(pm.getPmName());
      all.remove(pm);
      if (pm instanceof PmDataInput)
        dataInputPms.remove(pm);
      if (pm instanceof PmAttr)
        attrs.remove(pm);
      else if (pm instanceof PmCommand)
        commands.remove(pm);
      else if (pm instanceof PmElement)
        elements.remove(pm);
      else if (pm instanceof PmTableCol)
        columns.remove(pm);
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
    pmEventListeners = isWeak
          ? new WeakHashMap<PmEventListener, Integer>()
          : new ConcurrentHashMap<PmEventListener, Integer>();
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

  @SuppressWarnings("unchecked")
  public void fireEvent(final PmEvent event) {
    boolean hasListeners = !pmEventListeners.isEmpty();

    if (log.isTraceEnabled())
      log.trace("fireChange[" + event + "] for event source   : " + PmEventApi.getThreadEventSource() +
          (hasListeners ? "\n\teventListeners: " + pmEventListeners : ""));

    if (hasListeners) {
      boolean isPropagationEvent = event.isPropagationEvent();
      // copy the listener list to prevent problems with listener
      // set changes within the notification processing loop.
      for (Map.Entry<PmEventListener, Integer> e : pmEventListeners.entrySet().toArray(new Map.Entry[pmEventListeners.size()])) {
        int listenerMask = e.getValue().intValue();
        boolean isPropagationListener = ((listenerMask & PmEvent.IS_EVENT_PROPAGATION) != 0);
        // Propagation events have to be passed only to listeners that observe that special flag.
        // Standard events will be passed to listeners that don't have set this flag.
        if (isPropagationEvent) {
          if (isPropagationListener &&
              (listenerMask & event.getChangeMask()) != 0)
            e.getKey().handleEvent(event);
        }
        else {
          if ((! isPropagationListener) &&
              (listenerMask & event.getChangeMask()) != 0)
            e.getKey().handleEvent(event);
        }
      }
    }
  }

  boolean isEmpty() {
    return pmEventListeners.isEmpty();
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


package org.pm4j.core.pm.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.cache.CacheStrategy;
import org.pm4j.common.util.reflection.GenericTypeUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTreeNode;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmCacheCfg2;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Cache;
import org.pm4j.core.pm.annotation.PmCacheCfg2.CacheMode;
import org.pm4j.core.pm.annotation.PmValidationCfg;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.impl.InternalPmCacheCfgUtil.CacheMetaData;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;
import org.pm4j.core.pm.impl.pathresolver.PmExpressionPathResolver;

/**
 * A PM that handles a bean.
 *
 * @param <T_BEAN> The backing bean type.
 *
 * @author Olaf Boede
 */
public class PmBeanImpl2<T_BEAN>
      extends PmDataInputBase
      implements PmBean<T_BEAN> {

  /** A cached data object behind this PM. */
  /* package */ T_BEAN pmBeanCache;

  /**
   * Default constructor for PmBeans used in factories.
   */
  public PmBeanImpl2() {
    this(null);
  }

  /**
   * Creates a PM with an initial <code>null</code>-bean.
   *
   * @param pmParent
   *          the PM hierarchy parent.
   */
  public PmBeanImpl2(PmObject pmParent) {
    super(pmParent);
  }

// TODO oboede: needs to be checked.
//  /**
//   * Initializing constructor. Allows to access the bean behind this PM at
//   * construction time.
//   *
//   * @param pmParent
//   *          The context this PM is created in (e.g. field, session).
//   * @param bean
//   *          The bean this PM is constructed for.
//   */
//  public PmBeanImpl2(PmObject pmParent, T_BEAN bean) {
//    super(pmParent);
//    this.pmBean = bean;
//
//    if (bean != null) {
//      if (PmFactoryApi.findPmForBean(pmParent, bean) != null) {
//        throw new PmRuntimeException(pmParent, "PM for bean already exists. " +
//            "\nTry to get the PM using PmFactory.getPmForBean(pm, bean) instead of using the PM constructor." +
//            "\nThe bean: " + bean);
//      }
//      else {
//        ((PmObjectBase)pmParent).registerInPmBeanCache(this);
//      }
//    }
//  }

  @SuppressWarnings("unchecked")
  @Override
  public final T_BEAN getPmBean() {
    // Postponed value change events need to be executed at least now. The PM now gets used.
    BroadcastPmEventProcessor.doDeferredPmEventExecution(this);

    CacheStrategy cache = getOwnMetaData().valueCache.cacheStrategy;
    T_BEAN bean = (T_BEAN) cache.getCachedValue(this);
    // just return the cache hit (if there was one)
    if (bean != CacheStrategy.NO_CACHE_VALUE) {
      return bean;
    }
    bean = getPmBeanImpl();
    InternalPmBeanUtil.checkBeanClass(this, bean);
    return cache.setAndReturnCachedValue(this, bean);
  }

  /**
   * Will be called if {@link #pmBeanCache} is <code>null</code>.
   * <p>
   * The default implementation calls {@link #findPmBeanImpl()}. If that returns
   * <code>null</code> and {@link PmBeanCfg#autoCreateBean()} is configured a
   * bean gets created by calling the default constructor.
   *
   * @return The found (or new) bean.
   */
  @SuppressWarnings("unchecked")
  protected T_BEAN getPmBeanImpl() {
    T_BEAN bean = null;
    MetaData md = getOwnMetaData();
    // A bean may be referenced by valuePath
    if (md.beanPathResolver != null) {
      bean = (T_BEAN) md.beanPathResolver.getValue(getPmParent());
    }

    // @PmBeanCfg(autocreate=true) calls the default ctor if there is no bean:
    if ((bean == null) &&
        md.autoCreateBean) {
      try {
        bean = (T_BEAN)getPmBeanClass().newInstance();
      } catch (Exception e) {
        throw new PmRuntimeException(this, "Unable to execute the autoCreateBean operation specified in @PmBeanCfg.");
      }
    }

    return bean;
  }

  /**
   * Re-associates the PM to another bean instance and fires all change events
   * for this instance and all children.
   *
   * @param bean The new bean behind this PM.
   */
  @Override
  public void setPmBean(T_BEAN bean) {
    if (doSetPmBean(bean)) {
      int changeFlags = (bean != null) ? 0 : PmEvent.VALUE_CHANGE_TO_NULL;
      BroadcastPmEventProcessor.broadcastAllChangeEvent(this, changeFlags);
    }
  }

  /**
   * Re-associates the PM to a reloaded bean instance and fires all change events
   * for this instance and all children.<br>
   * The fired change event also has the flag {@link PmEvent#RELOAD}.
   *
   * @param reloadedBean The new bean state to represent.
   */
  @Override
  public void reloadPmBean(T_BEAN reloadedBean) {
    doSetPmBean(reloadedBean);
    BroadcastPmEventProcessor.broadcastAllChangeEvent(this, PmEvent.RELOAD);
  }

  boolean doSetPmBean(T_BEAN bean) {
    T_BEAN oldBean = getPmBean();
    if (oldBean == bean) {
      return false;
    }

    if (bean != null) {
      InternalPmBeanUtil.checkBeanClass(this, bean);
    }

    MetaData md = getOwnMetaData();
    // XXX oboede: A bean may be referenced by valuePath. Verify if that feature is really needed.
    //    if (md.beanPathResolver != null) {
    //      md.beanPathResolver.setValue(getPmParent(), bean);
    //    }

    if (!md.valueCache.cacheStrategy.isCaching()) {
      throw new PmRuntimeException(this, "Unable to set a bean if the PmBean no caching is configured.\n" +
            "\tPlease check if your task may be solved by providing a getPmBeanImpl() implementation.\n" +
            "\tIn some cased a permanent cache configuration @PmCacheCfg(@Cache(VALUE, clear=NEVER)) may be considered to support fix bean assignments.");
    }

    md.valueCache.cacheStrategy.setAndReturnCachedValue(this, bean);

    T_BEAN newCurrentBean = getPmBean();
    if (newCurrentBean != bean) {
      throw new PmRuntimeException(this, "The set operation was not successful. A get-call does not provide the assigned instance.\n" +
          "\tPlease check if your task may be solved by providing a getPmBeanImpl() implementation.\n" +
          "\tIn some cased a permanent cache configuration @PmCacheCfg(@Cache(VALUE, clear=NEVER)) may be considered to support fix bean assignments." +
          "\tInstance used as setPmBean parameter: " + bean +
          "\tInstance provided by get (after set): " + newCurrentBean);
    }

    if (bean != null) {
      // Re-register the bean to PM association to keep the PM system
      // intact.
      // TODO: check scope. The bean cache should be in the parent instance only.
      synchronized (getPmConversation()) {
        // FIXME olaf: what about un-registration in case of bean==null ?
        registerInPmBeanCache(this);
      }
    }

    return true;
  }

  @Override
  protected void clearCachedPmValues(Set<CacheKind> cacheSet) {
    if (pmInitState != PmInitState.INITIALIZED)
      return;
    super.clearCachedPmValues(cacheSet);
    if (cacheSet.contains(PmCacheApi.CacheKind.VALUE)) {
      getOwnMetaData().valueCache.cacheStrategy.clear(this);
    }
  }

  /**
   * @return the supported bean class.
   */
  @Override
  public Class<?> getPmBeanClass() {
    return getOwnMetaDataWithoutPmInitCall().beanClass;
  }

  /**
   * A PM that evaluates {@link #hasPmBean()} to <code>false</code> is by default read-only.
   */
  @Override
  protected boolean isPmReadonlyImpl() {
    return !hasPmBean() ||
           super.isPmReadonlyImpl();
  }

  /**
   * The default implementation assumes that the PM is unchanged if there is no bean behind it.
   */
  @Override
  protected boolean isPmValueChangedImpl() {
    // TODO oboede: Does not yet work for request scoped value caches (quite unusual).
    // Same problem for valuePath.
    return pmBeanCache != null &&
           super.isPmValueChangedImpl();
  }

  // TODO: deferred event handling is a common PM aspect. Should be placed in a common PM class.
  // Check annotation support.
  /**
   * @return <code>true</code> if {@link PmEvent} broadcasts may be deferred
   *         till the next {@link #getPmBean()} call.
   */
  protected boolean hasDeferredPmEventHandling() {
    return false;
  }

  /**
   * The default implementation checks if {@link #getPmBean()} returns null.<br>
   * Is internally used in {@link #isPmReadonlyImpl()}.
   * <p>
   * In lazy load scenarios a call to {@link #getPmBean()} may be a problem, because it
   * performs a slow bean load operation that should be prevented.<br>
   * Domain specific implemenation may provide here another (cheaper) implementation.
   *
   * @return <code>true</code> if this PM currently has a backing bean.
   */
  public boolean hasPmBean() {
    return getPmBean() != null;
  }

  @Override
  protected Validator makePmValidator() {
    return new BeanPmValidator<T_BEAN>();
  }

  /**
   * A special {@link PmBeanImpl2} class that provides access to the bean that
   * is handled by the embedding {@link PmBeanImpl2} instance.
   * <p>
   * If this class is not used in such an embedded context, it acts just like
   * its base class {@link PmBeanImpl2} without an embedding {@link PmBean}.
   * This functionality is intended for loose coupled unit test scenarios.
   *
   * @param <T_BEAN>
   *          Type of the backing bean.
   */
  @PmCacheCfg2(@Cache(property=CacheKind.VALUE, mode=CacheMode.OFF))
  public static class Nested<T_BEAN> extends PmBeanImpl2<T_BEAN> {

    /** The optional exisiting embedding context PM. */
    private PmBean<Object> embeddingBeanPm;

    /**
     * Creates the PM bound to a <code>null</code>-pmBean .
     *
     * @param parentPm The parent context PM.
     */
    public Nested(PmObject parentPm) {
      super(parentPm);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onPmInit() {
      super.onPmInit();
      embeddingBeanPm = PmUtil.findPmParentOfType(this, PmBean.class);
    }

    /**
     * Converts the bean found in the parent {@link PmBean} to the bean type handled here.<br>
     * The default implementation just passes the parent bean through.
     *
     * @param parentBean
     * @return
     */
    @SuppressWarnings("unchecked")
    protected T_BEAN parentBeanToOwnBean(Object parentBean) {
      return (T_BEAN) parentBean;
    }

    /** Finalized. Should not be overridden for this very special base class. */
    @Override
    protected final T_BEAN getPmBeanImpl() {
      return embeddingBeanPm != null
          ? parentBeanToOwnBean(embeddingBeanPm.getPmBean())
          : super.getPmBean();
    }

    /** Ensure that nobody sets a bean if this instance is used within an embedded context. */
    @Override
    public void setPmBean(T_BEAN bean) {
      zz_ensurePmInitialization();
      if (embeddingBeanPm != null) {
        throw new PmRuntimeException(this, "Unable to set a bean to a nested PmBean. Please provide a bean for the embedding PmBean " + embeddingBeanPm.getPmRelativeName());
      }
      super.setPmBean(bean);
    }

  }

  // ======== meta data ======== //

  @Override
  protected MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;

    // evaluate annotation
    PmBeanCfg annotation = AnnotationUtil.findAnnotation(this, PmBeanCfg.class);
    myMetaData.beanClass = InternalPmBeanUtil.readBeanClass(this, annotation);

    if (annotation != null) {
      myMetaData.autoCreateBean = annotation.autoCreateBean();
      if (StringUtils.isNotBlank(annotation.findBeanExpr())) {
        myMetaData.beanPathResolver = PmExpressionPathResolver.parse(annotation.findBeanExpr());
      }
      myMetaData.setReadOnly(annotation.readOnly());
    }

    // Check if bean validation can be used:
    // Use the nearest definition found within the PM parent hierarchy.
    // If not found: Use the default (true)
    List<PmObject> list = PmUtil.getPmHierarchy(this, true);
    for (PmObject p : list) {
      PmValidationCfg vcfg = AnnotationUtil.findAnnotation((PmObjectBase)p, PmValidationCfg.class);
      if (vcfg != null && vcfg.useJavaxValidationForBeans() != PmBoolean.UNDEFINED) {
        myMetaData.validateUsesBeanValidation = (vcfg.useJavaxValidationForBeans() == PmBoolean.TRUE);
        break;
      }
    }

    myMetaData.valueCache = InternalPmCacheCfgUtil.readCacheMetaData(this, CacheKind.VALUE, InternalPmBeanCacheStrategyFactory.INSTANCE);
  }

  /**
   * Shared meta data for all attributes of the same kind.
   * E.g. for all 'myapp.User.name' attributes.
   */
  protected static class MetaData extends PmElementBase.MetaData {
    private Class<?>        beanClass;
    private boolean         autoCreateBean             = false;
    private PathResolver    beanPathResolver           = null;
    private boolean         validateUsesBeanValidation = true;
    private CacheMetaData   valueCache                 = CacheMetaData.NO_CACHE;

    @Override
    protected void onPmInit(PmObjectBase pm) {
      super.onPmInit(pm);
      InternalPmCacheCfgUtil.registerClearOnListeners(pm, CacheKind.VALUE, valueCache.cacheClearOn);
    }

  }

  final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }

  private final MetaData getOwnMetaDataWithoutPmInitCall() {
    return (MetaData) getPmMetaDataWithoutPmInitCall();
  }

  // FIXME: check how to support PM trees / tree tables next. Move to PmObjectBase?
  @Override
  public List<PmTreeNode> getPmChildNodes() {
    return Collections.emptyList();
  }

  @Override
  public PmObject getNodeDetailsPm() {
    return this;
  }

  @Override
  public boolean isPmTreeLeaf() {
    return true;
  }


  /**
   * {@link PmBeanImpl2} validator logic.<br>
   * Validates all sub-PMs first.<br>
   * If the validation of all sub-PM's did not provide an error it performs a bean-validation on
   * the bean provided by {@link #getPmBean()}.
   */
  public static class BeanPmValidator<T_BEAN> extends ObjectValidator<PmBeanImpl2<T_BEAN>> {

    @Override
    protected void validateImpl(PmBeanImpl2<T_BEAN> pm) throws PmValidationException {
      super.validateImpl(pm);
      if (pm.getPmBean() != null &&
          pm.getOwnMetaDataWithoutPmInitCall().validateUsesBeanValidation &&
          // To prevent double problem reports, the bean validation
          // is triggered only if the PM logic did not find problems.
          PmMessageApi.getPmTreeMessages(pm, Severity.ERROR).size() == 0) {
        BeanValidationPmUtil.validateBean(pm, pm.getPmBean());
      }
    }
  }

}

class InternalPmBeanUtil {

  static void checkBeanClass(PmBeanImpl2<?> pm, Object bean) {
    if (bean != null &&
        ! pm.getPmBeanClass().isAssignableFrom(bean.getClass())) {
      throw new PmRuntimeException(pm, "Class '" + bean.getClass()
          + "' is not assignable to '" + pm.getPmBeanClass() + "'.");
    }
  }

  /** Reads the bean class information from the generics parameter or {@link PmBeanCfg#beanClass()}. */
  static Class<?> readBeanClass(PmBeanImpl2<?> pm, PmBeanCfg annotation) {
    Class<?> beanClass = null;
    Class<?> beanClassFromGeneric = GenericTypeUtil.resolveGenericArgument(PmBean.class, pm.getClass(), 0);
    Class<?> beanClassFromAnnotation = (annotation != null && annotation.beanClass() != Void.class) ? annotation.beanClass() : null;

    if (beanClassFromAnnotation != null) {
      beanClass = beanClassFromAnnotation;

      if (beanClassFromGeneric != null) {
        if (!beanClassFromGeneric.isAssignableFrom(beanClassFromAnnotation)) {
          throw new PmRuntimeException(pm, "Bean class specified as generics parameter '" + beanClassFromGeneric.getSimpleName()
              + "' and @PmBeanCfg(beanClass=" + beanClassFromAnnotation.getSimpleName() + ") are not compatible.");
        }
      }
    } else {
      beanClass = beanClassFromGeneric;
    }

    if (beanClass == null) {
      throw new PmRuntimeException(pm, "Unable to determine bean class. Please check if you have specified the generic type parameter. You may also specify @PmBeanCfg(beanClass=...).");
    }
    return beanClass;
  }

}

// XXX oboede: check if needed.
//static PmMatcher PM_BEAN_WITH_CACHED_GETTER_BASED_BEAN = new PmMatcher() {
//@Override
//public boolean doesMatch(PmObject pm) {
//if (pm instanceof PmBeanImpl2) {
//  PmBeanImpl2<?> pmb = (PmBeanImpl2<?>) pm;
//  CacheMetaData cmd = pmb.getOwnMetaData().valueCache;
//  return (cmd.cacheStrategy.isCaching() &&
//          cmd.clear != PmCacheCfg2.Clear.NEVER);
//}
//return false;
//}
//};
//
//public static final PmMatcher BEAN_PM_WITHOUT_CACHED_VALUE = new PmMatcher() {
//@Override
//public boolean doesMatch(PmObject pm) {
//if (!(pm instanceof PmBeanImpl2)) {
//  return false;
//}
//return ((PmBeanImpl2<?>)pm).pmBean == null;
//}
//};


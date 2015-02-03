package org.pm4j.core.pm.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.util.reflection.GenericTypeUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEvent.ValueChangeKind;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmObjectCfg.Visible;
import org.pm4j.core.pm.annotation.PmValidationCfg;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.api.PmMessageApi;

/**
 * A PM that handles a bean.
 *
 *
 * @param <T_BEAN> The backing bean type.
 *
 * @author Olaf Boede
 */
public class PmBeanBase<T_BEAN>
      extends PmElementBase
      implements PmBean<T_BEAN> {

  /** Logger of this class. */
  private final static Log LOG = LogFactory.getLog(PmBeanBase.class);

  /** The bean data object behind this PM. */
  private T_BEAN pmBean;

  /**
   * Default constructor for dependency injected PM's.
   * The referenced bean can be accessed only after construction time.
   */
  public PmBeanBase() {
  }

  /**
   * Creates a PM with an initial <code>null</code>-bean.
   *
   * @param pmParent
   *          the PM hierarchy parent.
   */
  public PmBeanBase(PmObject pmParent) {
    this(pmParent, null);
  }

  /**
   * Initializing constructor. Allows to access the bean behind this PM at
   * construction time.
   *
   * @param pmParent
   *          The context this PM is created in (e.g. field, session).
   * @param bean
   *          The bean this PM is constructed for.
   */
  public PmBeanBase(PmObject pmParent, T_BEAN bean) {
    initPmBean(pmParent, bean);
  }

  void initPmBean(PmObject pmParent, T_BEAN bean) {
    setPmParent(pmParent);

    this.pmBean = bean;

    if (bean != null) {
      if (PmFactoryApi.findPmForBean(pmParent, bean) != null) {
        throw new PmRuntimeException(pmParent, "PM for bean already exists. " +
            "\nTry to get the PM using PmFactory.getPmForBean(pm, bean) instead of using the PM constructor." +
            "\nThe bean: " + bean);
      }
      else {
        ((PmObjectBase)pmParent).registerInPmBeanCache(this);
      }
    }
  }

  @Override
  public T_BEAN getPmBean() {
    if (pmBean == null) {
      zz_ensurePmInitialization();
      pmBean = getPmBeanImpl();
      checkBeanClass(pmBean);
    }
    return pmBean;
  }

  /**
   * Looks up for the bean behind this PM.
   * <p>
   * The default implementation looks for a PM property for the expression
   * provided by {@link #getPmBeanKey()}.
   * <p>
   * Subclasses may provide alternative implementations.
   *
   * @return The found bean or <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  protected T_BEAN findPmBeanImpl() {
    T_BEAN bean = null;
    String propKey = getOwnMetaDataAndEnsurePmInit().beanPropertyKey;
    if (StringUtils.isNotBlank(propKey)) {
      zz_ensurePmInitialization();
      bean = (T_BEAN) PmExpressionApi.findByExpression(this, propKey, getPmBeanClass());
    }
    return bean;
  }

  /**
   * Will be called if {@link #pmBean} is <code>null</code>.
   * <p>
   * The default implementation calls {@link #findPmBeanImpl()}. If that returns
   * <code>null</code> and {@link PmBeanCfg#autoCreateBean()} is configured a
   * bean gets created by calling the default constructor.
   *
   * @return The found (or new) bean.
   */
  @SuppressWarnings("unchecked")
  protected T_BEAN getPmBeanImpl() {
    T_BEAN bean = findPmBeanImpl();

    if ((bean == null) &&
        getOwnMetaDataAndEnsurePmInit().autoCreateBean) {
      try {
        bean = (T_BEAN)getPmBeanClass().newInstance();
      } catch (Exception e) {
        throw new PmRuntimeException(this, "Unable to execute the autoCreateBean action specified in @PmCommandCfg.");
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
  //
  @Override
  public void setPmBean(T_BEAN bean) {
    if (doSetPmBean(bean)) {
      // Inform all sub PMs.
      // This is not done if the bean gets set within the initialization phase.
      // Otherwise we get the risk if initialization race conditions.
      if (pmInitState == PmInitState.INITIALIZED) {
        // the cached dynamic sub PMs are obsolete after switching to a new bean value.
        new ValueChangeEventProcessor(this, false).doIt();
      }
    }
  }

  /**
   * Re-associates the PM to a reloaded bean instance and fires all change events
   * for this instance and all children.<br>
   * The fired change event also has the flag {@link PmEvent#RELOAD}.
   *
   * @param bean The new bean behind this PM.
   */
  @Override
  public void reloadPmBean(T_BEAN reloadedBean) {
    doSetPmBean(reloadedBean);

    // Inform all sub PMs.
    // This is not done if the bean gets set within the initialization phase.
    // Otherwise we get the risk if initialization race conditions.
    if (pmInitState == PmInitState.INITIALIZED) {
      // the cached dynamic sub PMs are obsolete after switching to a new bean value.
      new ValueChangeEventProcessor(this, true).doIt();
    }
  }

  /* package */ boolean doSetPmBean(T_BEAN bean) {
    if (pmBean == bean) {
      return false;
    }

    pmBean = null;

    if (bean != null) {
      checkBeanClass(bean);
      pmBean = bean;

      // Re-register the bean to PM association to keep the PM system
      // intact.
      synchronized (getPmConversation()) {
        // FIXME olaf: what about un-registration in case of bean==null ?
        registerInPmBeanCache(this);
      }
    }

    return true;
  }

  /**
   * @return The supported bean class.
   */
  @Override
  public Class<?> getPmBeanClass() {
    return getOwnMetaData().beanClass;
  }

  /**
   * A PM for a <code>null</code> bean is by default read-only.
   */
  @Override
  protected boolean isPmReadonlyImpl() {
    return (getPmBean() == null) ||
           super.isPmReadonlyImpl();
  }

  @Override
  protected boolean isPmVisibleImpl() {
    boolean visible = super.isPmVisibleImpl();
    if (visible && (getOwnMetaData().getVisibilityCfg() == Visible.IF_NOT_EMPTY)) {
      visible = getPmBean() != null;
    }
    return visible;
  }

  /**
   * The default implementation assumes that the PM is unchanged if there is no bean behind it.
   */
  @Override
  protected boolean isPmValueChangedImpl() {
    return (getPmBean() != null) &&
           super.isPmValueChangedImpl();
  }

  /**
   * {@link PmBeanBase} validator logic.<br>
   * Validates all sub-PMs first.<br>
   * If the validation of all sub-PM's did not provide an error it performs a bean-validation on
   * the bean provided by {@link #getPmBean()}.
   */
  public static class BeanPmValidator<T_BEAN> extends ObjectValidator<PmBeanBase<T_BEAN>> {

    @Override
    protected void validateImpl(PmBeanBase<T_BEAN> pm) throws PmValidationException {
      super.validateImpl(pm);
      if (pm.getPmBean() != null &&
          pm.getOwnMetaData().validateUsesBeanValidation &&
          // To prevent double problem reports, the bean validation
          // is triggered only if the PM logic did not find problems.
          PmMessageApi.getPmTreeMessages(pm, Severity.ERROR).size() == 0) {
        BeanValidationPmUtil.validateBean(pm, pm.getPmBean());
      }
    }
  }

  @Override
  protected Validator makePmValidator() {
    return new BeanPmValidator<T_BEAN>();
  }

  private void checkBeanClass(Object bean) {
    if (bean != null &&
        ! getPmBeanClass().isAssignableFrom(bean.getClass())) {
      throw new PmRuntimeException(this, "Class '" + bean.getClass()
          + "' is not assignable to '" + getPmBeanClass() + "'.");
    }
  }

  /**
   * A special {@link PmBeanBase} class that provides access to the bean that is handled by the embedding
   * {@link PmBeanBase} instance.
   * <p>
   * If this class is not used in such an embedded context, it acts just like its base class {@link PmBeanBase}.
   *
   * @param <T_BEAN> Type of the backing bean.
   */
  public static class Nested<T_BEAN> extends PmBeanBase<T_BEAN> {

    /** The optional exisiting embedding context PM. */
    private PmBean<T_BEAN> embeddingBeanPm;

    /**
     * Creates the PM bound to a <code>null</code>-pmBean .
     *
     * @param parentPm The parent context PM.
     */
    public Nested(PmObject parentPm) {
      super(parentPm, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onPmInit() {
      super.onPmInit();
      embeddingBeanPm = PmUtil.findPmParentOfType(this, PmBean.class);
    }

    @Override
    public T_BEAN getPmBean() {
      zz_ensurePmInitialization();
      return embeddingBeanPm != null
              ? embeddingBeanPm.getPmBean()
              : super.getPmBean();
    }

    /** Finalized. Should not be overridden for this very special base class. */
    @Override
    protected final T_BEAN findPmBeanImpl() {
      return super.findPmBeanImpl();
    }

    /** Finalized. Should not be overridden for this very special base class. */
    @Override
    protected final T_BEAN getPmBeanImpl() {
      return super.getPmBeanImpl();
    }

    /** Ensure that nobody sets a bean if this instance is used within an embedded context. */
    @Override
    boolean doSetPmBean(T_BEAN bean) {
      zz_ensurePmInitialization();
      if (embeddingBeanPm != null) {
        // TODO oboede: throw an exception with the next breaking change delivery.
        LOG.warn("#####################################################################################\n" +
                 "# Unable to set a bean to a nested PmBean. Please assign the bean to the embedding PmBean.\n" +
                 "# The assigned bean will be ignored! \n" +
                 "#####################################################################################");
        // throw new PmRuntimeException(this, "Unable to set a bean to a nested PmBean. Please assign the bean to the embedding PmBean.");
      }
      return super.doSetPmBean(bean);
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

    // evaluate myMetaData.beanClass
    {
      // evaluate bean class from annotation PmBeanCfg
      Class<?> beanClassFromAnnotation = (annotation != null) ? annotation.beanClass() : null;

      // evaluate bean class from type of generic parameter of PmBean
      Class<?> beanClassFromGeneric = GenericTypeUtil.resolveGenericArgument(PmBean.class, this.getClass(), 0);

      if ((beanClassFromAnnotation != null) && (beanClassFromGeneric != null)) {
        // annotation parameter PmBeanCfg.beanClass shall override generic parameter of interface PmBean
        myMetaData.beanClass = beanClassFromAnnotation;

        // if PmBeanCfg.beanClass is not assignable from PmBean.genericParameter, throw PmRuntimeException
        if (!beanClassFromGeneric.isAssignableFrom(beanClassFromAnnotation)) {
          // FIXME oboede: is disabled to be able to deliversomething.
          // Needs to be reactivated asap.
          LOG.error(PmUtil.getPmLogString(this) + ": PmBeanCfg.beanClass " + beanClassFromGeneric.getSimpleName()
              + " is not assignable from PmBean.genericParameter " + beanClassFromAnnotation.getSimpleName());
//          throw new PmRuntimeException(this, ": PmBeanCfg.beanClass " + beanClassFromGeneric.getSimpleName()
//              + " is not assignable from PmBean.genericParameter " + beanClassFromAnnotation.getSimpleName());
        } else if (!beanClassFromAnnotation.equals(beanClassFromGeneric)) {
          // if PmBeanCfg.beanClass and PmBean.genericParameter are assignable, but of different type, log debug
          if (LOG.isDebugEnabled()) {
            LOG.debug(this.toString() + ": PmBeanCfg.beanClass " + beanClassFromGeneric.getSimpleName()
                + " is assignable from PmBean.genericParameter " + beanClassFromAnnotation.getSimpleName() + ", but of different type");
          }
        }

      } else if (beanClassFromAnnotation != null && beanClassFromGeneric == null) {
        // take bean class from PmBeanCfg.beanClass
        myMetaData.beanClass = beanClassFromAnnotation;

        // if PmBean.genericParameter is unknown, log warning
        LOG.warn(this.toString() + ": PmBean.genericParameter is unknown");

      } else if (beanClassFromAnnotation == null && beanClassFromGeneric != null) {
        // if PmBeanCfg.beanClass is Void, take bean class from PmBean.genericParameter
        myMetaData.beanClass = beanClassFromGeneric;

      } else if (beanClassFromAnnotation == null && beanClassFromGeneric == null) {
        // if PmBeanCfg.beanClass is Void and PmBean.genericParameter is unknown, throw PmRuntimeException
        throw new PmRuntimeException(this, "PmBeanCfg.beanClass and PmBean.genericParameter are both unknown");
      }
    }
    // myMetaData.beanClass is defined now

    // create BeanAttrAccessor
    if (annotation != null) {
      myMetaData.autoCreateBean = annotation.autoCreateBean();
      myMetaData.beanPropertyKey = StringUtils.trimToEmpty(annotation.findBeanExpr());
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

  }

  /**
   * Shared meta data for all attributes of the same kind.
   * E.g. for all 'myapp.User.name' attributes.
   */
  protected static class MetaData extends PmElementBase.MetaData {
    private Class<?> beanClass;
    private boolean autoCreateBean = false;
    private String beanPropertyKey = "";
    private boolean validateUsesBeanValidation = true;

  }

  private final MetaData getOwnMetaDataAndEnsurePmInit() {
    return (MetaData) getPmMetaData();
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaDataWithoutPmInitCall();
  }

  // TODO oboede: remove in v0.9x, keep it for now to check whether we break old code.
  public static class ValueChangeEventProcessor extends BroadcastPmEventProcessor {
    public ValueChangeEventProcessor(PmDataInput rootPm, boolean isReloadEvent) {
      super(rootPm,
          isReloadEvent
              ? PmEvent.ALL_CHANGE_EVENTS | PmEvent.RELOAD
              : PmEvent.ALL_CHANGE_EVENTS,
          isReloadEvent ? ValueChangeKind.RELOAD : ValueChangeKind.VALUE);
    }

    @Override
    protected void fireEvents() {
      // == Cleanup all PM state, because the bean to show is a new one. ==

      // Forget all changes and dynamic PM's below this instance.
      BeanPmCacheUtil.clearBeanPmCachesOfSubtree(rootPm);

      // All sub PM messages are no longer relevant.
      PmMessageApi.clearPmTreeMessages(rootPm);

      // Old cache values are related to the old bean.
      // This cache cleanup can only be done AFTER visiting the tree because
      // it cleans the current-row information that is relevant.
      PmCacheApi.clearPmCache(rootPm);

      // Visit the sub-tree to inform all PM listeners.
      super.fireEvents();

      // Another (postponed) cleanup:
      // Mark the whole sub tree as unchanged.

      // FIXME olaf: this is done AFTER the main visit to keep some old code alive.
      // See: RoleEditorBeanTabPm...
      ((PmDataInput)rootPm).setPmValueChanged(false);
    }

  };


}

package org.pm4j.core.pm.impl;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.util.reflection.BeanAttrAccessor;
import org.pm4j.core.util.reflection.BeanAttrAccessorImpl;

public abstract class PmBeanBase<T_BEAN>
      extends PmElementBase
      implements PmBean<T_BEAN> {

  private T_BEAN pmBean;

  /**
   * Default constructor for dependency injected PM's.
   * The referenced bean can be accessed only after construction time.
   */
  public PmBeanBase() {
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
    String propKey = getOwnMetaData().beanPropertyKey;
    if (StringUtils.isNotBlank(propKey)) {
      bean = (T_BEAN) PmExpressionApi.findByExpression(this, propKey, getOwnMetaData().beanClass);
    }
    return bean;
  }

  /**
   * Will be called if {@link #pmBean} is <code>null</code>.
   * <p>
   * The default implementation tries to get a the bean addressed by
   * {@link #getPmBeanKey()}.
   *
   * @return The found bean.
   */
  @SuppressWarnings("unchecked")
  protected T_BEAN getPmBeanImpl() {
    T_BEAN bean = findPmBeanImpl();

    if ((bean == null) &&
        getOwnMetaData().autoCreateBean) {
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
  public void setPmBean(T_BEAN bean) {
    Object eventSource = PmEventApi.ensureThreadEventSource(this);

    if (bean != pmBean) {
      pmBean = null;

      // Old cache values are related to the old bean.
      PmMessageUtil.clearPmMessages(this);
      PmCacheApi.clearCachedPmValues(this);
      new PmVisitorSetToUnchanged().visit(this);

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

      // Inform all sub PMs.
      // This is not done if the bean gets set within the initialization phase.
      // Otherwise we get the risk if initialization race conditions.
      if (pmInitState == PmInitState.INITIALIZED) {
        new PmVisitorFireEvent(new PmEvent(eventSource, this, PmEvent.ALL_CHANGE_EVENTS))
              .visit(this);
      }
    }
  }

  /**
   * The default implementation provides a unique identifier
   * for the memory bean behind this model.
   */
  @Override
  public Serializable getPmKey() {
    Serializable id = getPmBeanId();
    return (id != null)
        ? id
        : super.getPmKey();
  }

  /**
   * FIXME olaf: isn't that something that has only to live in the implementation interface?
   * <p>
   * Provides a key that identifies the bean behind this model.<p>
   * That key is usually the same that is used within the persistence context.
   * <p>
   * TODOC: Different requirements for root and owned entities.
   * <p>
   * The bean identifier attributes are usually defined using the annotation
   * {@link PmBeanCfg#key()}.<br>
   * The default implementation of this method returns an instance that represents
   * the content of the attribute(s) defined using that annotation.
   * <p>
   * If this method returns <code>null</code>, the default implementation assumes that
   * the bean behind this model is transient.
   *
   * @return A key that identifies the bean behind this model.
   */
  // TODO olaf: rename or remove.
  private Serializable getPmBeanId() {
    // TODO olaf: add support for multi attribute keys.
    BeanAttrAccessor idAttrAccessor = getOwnMetaData().idAttrAccessor;
    if (idAttrAccessor != null) {
      return idAttrAccessor.getBeanAttrValue(getPmBean());
    }
    else {
      return null;
    }
  }

  /**
   * @return The supported bean class.
   */
  @Override
  public Class<?> getPmBeanClass() {
    return getOwnMetaData().beanClass;
  }

  private void checkBeanClass(Object bean) {
    if (bean != null &&
        ! getOwnMetaData().beanClass.isAssignableFrom(bean.getClass())) {
      throw new PmRuntimeException(this, "Class '" + bean.getClass()
          + "' is not assignable to '" + getOwnMetaData().beanClass + "'.");
    }
  }

  /**
   * A special {@link PmBeanBase} class that provides access to the bean that is handled by the embedding
   * {@link PmBeanBase} instance.
   *
   * @param <T_BEAN> Type of the backing bean.
   */
  public static class Nested<T_BEAN> extends PmBeanBase<T_BEAN> {

    public Nested(PmObject parentPm) {
      super(parentPm, null);
    }

    /** A reference that binds the listener life cycle to this instance. */
    private PmEventListener pmEmbeddingBeanPmValueChangeListener;

    @Override
    protected void onPmInit() {
      super.onPmInit();

      pmEmbeddingBeanPmValueChangeListener = new PmEventListener() {
        @Override
        public void handleEvent(PmEvent event) {
          setPmBean(null);
        }
      };

      PmBeanBase<T_BEAN> embeddingBeanPm = getEmbeddingBeanPm();
      PmEventApi.addWeakPmEventListener(embeddingBeanPm, PmEvent.VALUE_CHANGE, pmEmbeddingBeanPmValueChangeListener);
    }

    @Override
    protected T_BEAN findPmBeanImpl() {
      PmBeanBase<T_BEAN> embeddingBeanPm = getEmbeddingBeanPm();
      return embeddingBeanPm.getPmBean();
    }

    @SuppressWarnings("unchecked")
    private PmBeanBase<T_BEAN> getEmbeddingBeanPm() {
      return PmUtil.getPmParentOfType(this, PmBeanBase.class);
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

    PmBeanCfg annotation = AnnotationUtil.findAnnotation(this, PmBeanCfg.class);

    if (annotation != null) {
      myMetaData.beanClass = annotation.beanClass();
      if (StringUtils.isNotBlank(annotation.key())) {
        try {
          myMetaData.idAttrAccessor = new BeanAttrAccessorImpl(myMetaData.beanClass, annotation.key());
        }
        catch (org.pm4j.core.util.reflection.ReflectionException e) {
          if (annotation.key().equals(PmBeanCfg.DEFAULT_BEAN_ID_ATTR)) {
            // Ok, there was not specific id configuration
          }
          else {
            PmObjectUtil.throwAsPmRuntimeException(this, e);
          }
        }
      }
      myMetaData.autoCreateBean = annotation.autoCreateBean();
      myMetaData.beanPropertyKey = StringUtils.trimToEmpty(annotation.findBeanExpr());
      myMetaData.setReadOnly(annotation.readOnly());
    }

    if (myMetaData.beanClass == null) {
      throw new IllegalArgumentException("Missing annotation " + PmBeanCfg.class.getSimpleName() + " for class " + getClass());
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
    private BeanAttrAccessor idAttrAccessor;
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }

}

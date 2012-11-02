package org.pm4j.core.pm.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEvent.ValueChangeKind;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTable2;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmValidationCfg;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.impl.changehandler.ChangeSetHandler.ChangeKind;
import org.pm4j.core.util.reflection.BeanAttrAccessor;
import org.pm4j.core.util.reflection.BeanAttrAccessorImpl;

public abstract class PmBeanBase<T_BEAN>
      extends PmElementBase
      implements PmBean<T_BEAN> {

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
  @Override
  public void setPmBean(T_BEAN bean) {
    if (doSetPmBean(bean)) {
      // Inform all sub PMs.
      // This is not done if the bean gets set within the initialization phase.
      // Otherwise we get the risk if initialization race conditions.
      if (pmInitState == PmInitState.INITIALIZED) {
        // the cached dynamic sub PMs are obsolete after switching to a new bean value.
        BeanPmCacheUtil.clearBeanPmCachesOfSubtree(this);
        new SetPmBeanEventVisitor(PmEvent.ALL_CHANGE_EVENTS).visit(this);
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
      BeanPmCacheUtil.clearBeanPmCachesOfSubtree(this);
      new SetPmBeanEventVisitor(PmEvent.ALL_CHANGE_EVENTS | PmEvent.RELOAD).visit(this);
    }
  }

  /* package */ boolean doSetPmBean(T_BEAN bean) {
    if (pmBean == bean) {
      return false;
    }

    PmEventApi.ensureThreadEventSource(this);

    pmBean = null;

    // Old cache values are related to the old bean.
    PmMessageUtil.clearSubTreeMessages(this);
    PmCacheApi.clearCachedPmValues(this);

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

  public class SetPmBeanEventVisitor extends PmVisitorAdapter {

    private final int eventMask;
    private ValueChangeKind changeKind;

    public SetPmBeanEventVisitor(int eventMask) {
      this.eventMask = eventMask;
      this.changeKind = ((eventMask & PmEvent.RELOAD) != 0) ? ValueChangeKind.RELOAD : ValueChangeKind.VALUE;
    }

    @Override
    protected void onVisit(PmObject pm) {
      PmEventApi.firePmEvent(pm, eventMask, changeKind);
      for (PmObject child : PmUtil.getPmChildren(pm)) {
        // The children may have relevant event handling code. Thus we make
        // sure that each child receives the call.
        PmInitApi.ensurePmInitialization(child);

        // Switch for each child to an event that is related to the child.
        // Registered listeners for the child will expect that the event contains a
        // reference to the PM it was registered for.
        // TODO olaf: add a event cause to PmEvent.
        SetPmBeanEventVisitor childVisitor = new SetPmBeanEventVisitor(eventMask);
        child.accept(childVisitor);
      }
    }

    @Override
    public void visit(@SuppressWarnings("rawtypes") PmTable table) {
      @SuppressWarnings("unchecked")
      List<Object> changedRows = new ArrayList<Object>(table.getRowsWithChanges());

      onVisit(table);
      ((PmTableImpl<?>)table).setPmValueChanged(false);

      // Changed rows get informed to make sure that all invalid PM states get cleared.
      // Informs the ChangedChildStateRegistry of the table.
      // TODO olaf: check if the PMs of the current page need to be informed individually too.
      //            I suspect not, since the all-change-event causes a re-binding of all table rows PMs.
      PmEventApi.firePmEvent(table, eventMask);

      // Inform the changed rows to make sure that all invalid PM states get cleared.
      for (Object row : changedRows) {
        if (row instanceof PmObject) {
          onVisit((PmObject) row);
        }
      }
    }

    @Override
    public void visit(PmTable2<?> table) {
      List<Object> changedRows = new ArrayList<Object>(table.getPmChangeSetHandler().getChangedItems(ChangeKind.ADD, ChangeKind.UPDATE));
      onVisit(table);
      table.updatePmTable();

      // Changed rows get informed to make sure that all invalid PM states get cleared.
      // Informs the ChangedChildStateRegistry of the table.
      // TODO olaf: check if the PMs of the current page need to be informed individually too.
      //            I suspect not, since the all-change-event causes a re-binding of all table rows PMs.
      PmEventApi.firePmEvent(table, eventMask);

      // Inform the changed rows to make sure that all invalid PM states get cleared.
      for (Object row : changedRows) {
        onVisit((PmObject) row);
      }
    }

    @Override
    public void visit(PmAttr<?> attr) {
      attr.setPmValueChanged(false);
      onVisit(attr);
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
    BeanAttrAccessor idAttrAccessor = getOwnMetaDataAndEnsurePmInit().idAttrAccessor;
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

  /**
   * A PM for a <code>null</code> bean is by default read-only.
   */
  @Override
  protected boolean isPmReadonlyImpl() {
    return (getPmBean() == null) ||
           super.isPmReadonlyImpl();
  }

  /**
   * Validates all sub-PMs.<br>
   * It the validation of all sub-PM's did not provide an error it performs a bean-validation on
   * the bean provided by {@link #getPmBean()}.
   */
  @Override
  public void pmValidate() {
    super.pmValidate();
    if (getPmBean() != null &&
        getOwnMetaData().validateUsesBeanValidation &&
        PmMessageUtil.getSubTreeMessages(this, Severity.ERROR).size() == 0) {
      Validator validator = PmImplUtil.getBeanValidator();
      if (validator != null) {
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>)(Object) validator.validate(getPmBean());
        PmImplUtil.beanConstraintViolationsToPmMessages(this, violations);
      }
    }
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
    private BeanAttrAccessor idAttrAccessor;
    private boolean validateUsesBeanValidation = true;

  }

  private final MetaData getOwnMetaDataAndEnsurePmInit() {
    return (MetaData) getPmMetaData();
  }

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaDataWithoutPmInitCall();
  }

}

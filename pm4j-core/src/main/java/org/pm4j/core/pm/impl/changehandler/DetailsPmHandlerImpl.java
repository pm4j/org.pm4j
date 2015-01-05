package org.pm4j.core.pm.impl.changehandler;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.api.PmValidationApi;

/**
 * A details handler that allows a master record switch only if the details area is valid.
 * <p>
 * It clears all details area related messages and cache states after a master record selection change.
 * <p>
 * It has a set of {@link PmCommandDecorator}s that may be used to plug-in additional logic
 * (see {@link #addDecorator(PmCommandDecorator)}.
 *
 * @author Olaf Boede
 *
 * @param <T_DETAILS_PM>
 *          Type of the supported details PM.
 */
public class DetailsPmHandlerImpl<T_DETAILS_PM extends PmObject, T_MASTER_RECORD> implements DetailsPmHandler {

  /** The details area PM. */
  private final T_DETAILS_PM detailsPm;

  /** Additional decorators to apply on {@link #canSwitchMasterRecord()} and {@link #afterMasterRecordChange(Object)}. */
  private final List<PmCommandDecorator> decorators = new ArrayList<PmCommandDecorator>();

  /**
   * Constructor for a handler that just observes the master record switches without handling a details
   * PM directly.
   */
  public DetailsPmHandlerImpl() {
    this.detailsPm = null;
  }

  /**
   * @param detailsPm The details PM to handle.
   */
  public DetailsPmHandlerImpl(T_DETAILS_PM detailsPm) {
    assert detailsPm != null;
    this.detailsPm = detailsPm;
  }

  @Override
  public void startObservers() {
  }

  /** Calls <code>beforeDo</code> for all decorators and {@link #beforeMasterRecordChangeImpl(Object)}. */
  @SuppressWarnings("unchecked")
  public final boolean beforeMasterRecordChange(Object oldMasterRecord, Object newMasterRecord) {
    for (PmCommandDecorator d : decorators) {
      if (!d.beforeDo(null)) {
        return false;
      }
    }
    return beforeMasterRecordChangeImpl((T_MASTER_RECORD) oldMasterRecord, (T_MASTER_RECORD) newMasterRecord);
  }

  /**
   * Defines the checks and logic to be applied before the master selection may be switched
   * from the given master record to another one.<br>
   * The record switch may be prevented by returning <code>false</code>.
   * <p>
   * The default implementation validates the details PM and prevents the switch if the details
   * area is not valid.
   *
   * @param oldMasterBean The master bean to deselect.
   * @param newMasterBean The master bean to select.
   * @return <code>true</code> if this handler agrees to the switch. <code>false</code> prevents the switch.
   */
  protected boolean beforeMasterRecordChangeImpl(T_MASTER_RECORD oldMasterBean, T_MASTER_RECORD newMasterRecord) {
    if (!canSwitchMasterRecord()) {
      return false;
    }
    for (PmCommandDecorator d : decorators) {
      if (!d.beforeDo(null)) {
        return false;
      }
    }
    return true;
  }

  @Override
  @Deprecated
  public boolean canSwitchMasterRecord() {
    return detailsPm != null
        ? PmValidationApi.validateSubTree(detailsPm)
        : true;
  }

  /**
   * Calls {@link #afterMasterRecordChangeImpl(Object)} and calls the <code>afterDo()</code> method for
   * each configured decorator.
   */
  @SuppressWarnings("unchecked")
  @Override
  public final void afterMasterRecordChange(Object newMasterBean) {
    afterMasterRecordChangeImpl((T_MASTER_RECORD) newMasterBean);
    for (PmCommandDecorator d : decorators) {
        d.afterDo(null);
    }
  }

  /**
   * The default implementation just clears the details PM.
   * <br>
   * More specific details hander implementations may add here their details area specific
   * logic.
   */
  protected void afterMasterRecordChangeImpl(T_MASTER_RECORD newMasterBean) {
    if (detailsPm != null) {
      // The details area has now a new content to handle. The old messages of
      // that area where related to the record that is no longer active.
      PmMessageApi.clearPmTreeMessages(detailsPm);
      // All cached information within the details area should be refreshed.
      PmCacheApi.clearPmCache(detailsPm, CacheKind.ALL);
    }
  }

  /**
   * Adds a decorator to consider in {@link #beforeMasterRecordChange(Object)} and {@link #afterMasterRecordChange(Object)}.
   *
   * @param decorator
   */
  public final void addDecorator(PmCommandDecorator decorator) {
    decorators.add(decorator);
  }

  @Override
  public final PmObject getDetailsPm() {
    return detailsPm;
  }

  /**
   * Provides type save access to the handled details PM.
   *
   * @return the handled details PM. Never <code>null</code>.
   */
  protected final T_DETAILS_PM getTypedDetailsPm() {
    return detailsPm;
  }
}

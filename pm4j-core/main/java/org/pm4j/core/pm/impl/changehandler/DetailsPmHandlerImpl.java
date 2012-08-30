package org.pm4j.core.pm.impl.changehandler;

import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;

/**
 * Default details handler implementation.
 * <p>
 * Just clears all messages after a master record selection change.
 *
 * @author olaf boede
 *
 * @param <T_DETAILS_PM>
 *          Type of the supported details PM.
 */
public class DetailsPmHandlerImpl<T_DETAILS_PM extends PmDataInput, T_MASTER_RECORD> implements DetailsPmHandler<T_DETAILS_PM> {

  private final T_DETAILS_PM detailsPm;

  /**
   * @param detailsPm The details PM to handle.
   */
  public DetailsPmHandlerImpl(T_DETAILS_PM detailsPm) {
    this.detailsPm = detailsPm;
  }

  @Override
  public T_DETAILS_PM getDetailsPm() {
    return detailsPm;
  }

  /**
   * Just calls the type safe method {@link #afterMasterRecordChangeImpl(Object)}.
   */
  @SuppressWarnings("unchecked")
  @Override
  public final void afterMasterRecordChange(Object newMasterBean) {
    afterMasterRecordChangeImpl((T_MASTER_RECORD) newMasterBean);
  }

  /**
   * The default implementation just clears the details PM.
   * <br>
   * The default implementation does not set some master record related content to the details
   * area.
   * This may be done within a more concrete sub class or within the details area itself.
   */
  protected void afterMasterRecordChangeImpl(T_MASTER_RECORD newMasterBean) {
    // The details area has now a new content to handle. The old messages of
    // that area where related to the record that is no longer active.
    PmMessageUtil.clearSubTreeMessages(detailsPm);
    // All cached information within the details area should be refreshed.
    PmCacheApi.clearCachedPmValues(detailsPm, CacheKind.ALL);
  }

  @Override
  public void onResetMasterContent() {
    PmMessageUtil.clearSubTreeMessages(detailsPm);
  }

}

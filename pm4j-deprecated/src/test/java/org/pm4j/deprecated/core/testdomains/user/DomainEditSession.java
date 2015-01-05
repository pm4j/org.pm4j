package org.pm4j.deprecated.core.testdomains.user;

import static org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO.CLEAR;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.api.PmValidationApi;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmDataInputUtil;
import org.pm4j.deprecated.core.pm.DeprPmAttrPmRef;

public class DomainEditSession extends PmConversationImpl.ChildSession<AdminSession> {

  // -------- Session attributes -------- //

  private DomainPm editedDomain;

  /**
   * FIXME: Die ParentSession Selektion ist auszutauschen sobald ein Konversationsskope
   * zur Verfuegung steht.
   *
   * @return The model of the domain selection.
   */
  public DeprPmAttrPmRef<DomainPm> getSelectedDomainRef() {
    return getParentSessionImpl().selectedDomain;
  }

  /**
   * @return The model of the edited instance.
   */
  public DomainPm getEditedDomain() {
    if (editedDomain == null) {
      Domain selectedDomain = (Domain)getSelectedDomainRef().getValueAsBean();
      if (selectedDomain != null) {
        PmConversationImpl editSession = new PmConversationImpl(this, DomainPm.class);
        editSession.setBufferedPmValueMode(true);
        editedDomain = PmFactoryApi.getPmForBean(editSession, selectedDomain);
      }
    }
    return editedDomain;
  }

//  @PmOptions(param="$selectedDomain/value/users")
//  public final PmAttrPmRef<DomainPm> selectedUser = makePmAttrRef("selectedUser");

  // -------- Commands -------- //

  public final PmCommand cmdSave = new PmCommandImpl(this) {
    @Override protected void doItImpl() {
      DomainPm pm = getEditedDomain();
      if (pm != null && PmValidationApi.hasValidAttributes(pm)) {
        PmDataInputUtil.commitBufferedPmChanges(pm);
        getParentSessionImpl().getDomainService().saveDomain(pm.getPmBean());
        clearEdited();
      }
    }
  };

  @PmCommandCfg(beforeDo=CLEAR)
  public final PmCommand cmdCancel = new PmCommandImpl(this) {
    @Override protected void doItImpl() {
      clearEdited();
    }
  };

  public final PmCommand cmdNew = new PmCommandImpl(this) {
    @Override protected void doItImpl() {
      clearEdited();
      getSelectedDomainRef().setValueAsBean(new Domain(null));
    }
  };

  @Override
  protected void onPmInit() {
    setBufferedPmValueMode(true);
  }

  protected void clearEdited() {
    editedDomain = null;
    clearPmInvalidValues();
  }

  // -------- Generated stuff -------- //


}

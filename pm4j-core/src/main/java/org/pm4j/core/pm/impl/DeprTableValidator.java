package org.pm4j.core.pm.impl;

import java.util.List;

import org.pm4j.common.modifications.Modifications;
import org.pm4j.common.util.collection.ListUtil;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmValidationApi;

/**
 * Implements the validation logic that was formerly in {@link PmTableImpl#pmValidate()}.
 */
class DeprTableValidator implements PmObjectBase.Validator {

  @Override
  public void validate(PmObject pm) {
    @SuppressWarnings("unchecked")
    PmTableImpl<PmBean<?>, ?> tablePm = (PmTableImpl<PmBean<?>, ?>) pm;
    Modifications<PmBean<?>> m = tablePm.getPmPageableCollection().getModifications();
    List<PmBean<?>> changes = ListUtil.collectionsToList(m.getAddedItems(), m.getUpdatedItems());
    for (PmBean<?> itemPm : changes) {
      PmValidationApi.validateSubTree(itemPm);
    }
  }
}
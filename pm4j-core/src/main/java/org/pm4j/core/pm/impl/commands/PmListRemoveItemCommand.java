package org.pm4j.core.pm.impl.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.impl.PmCommandImpl;

/**
 * Revomes items from a list attribute.
 *
 * @author olaf boede
 */
public class PmListRemoveItemCommand extends PmCommandImpl implements PmCommand {

  private final Collection<PmBean<?>> removedItems;

  @SuppressWarnings("unchecked")
  public PmListRemoveItemCommand(PmAttrPmList<?> pmListAttr, List<?> removedItems) {
    super(pmListAttr);
    this.removedItems = (Collection<PmBean<?>>)removedItems;
//    setUndoCommand(new PmListAddItemCommand());
  }

  public PmListRemoveItemCommand(PmAttrPmList<?> presentationModel, PmBean<?> removedItem) {
    this(presentationModel, Arrays.asList(removedItem));
  }

  @Override
  protected void doItImpl() {
    @SuppressWarnings("unchecked")
    PmAttrPmList<PmBean<?>> pmAttrList = (PmAttrPmList<PmBean<?>>) getPmParent();
    for (PmBean<?> pm : removedItems) {
      pmAttrList.remove(pm);
    }
  }

}

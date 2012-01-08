package org.pm4j.core.pm.impl.commands;

import java.util.Collection;
import java.util.List;

import org.pm4j.common.util.collection.ListUtil;
import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.impl.PmCommandImpl;

/**
 * Revomes items from a list attribute.
 *
 * @author olaf boede
 */
public class PmListRemoveItemCommand extends PmCommandImpl implements PmCommand {

  private final Collection<PmElement> removedItems;

  @SuppressWarnings("unchecked")
  public PmListRemoveItemCommand(PmAttrPmList<?> pmListAttr, List<?> removedItems) {
    super(pmListAttr);
    this.removedItems = (Collection<PmElement>)removedItems;
//    setUndoCommand(new PmListAddItemCommand());
  }

  public PmListRemoveItemCommand(PmAttrPmList<?> presentationModel, PmElement removedItem) {
    this(presentationModel, ListUtil.itemToList(removedItem));
  }

  @Override
  protected void doItImpl() throws Exception {
    @SuppressWarnings("unchecked")
    PmAttrPmList<PmElement> pmAttrList = (PmAttrPmList<PmElement>) getPmParent();
    for (PmElement pm : removedItems) {
      pmAttrList.remove(pm);
    }
  }

}

package org.pm4j.core.pm.impl.commands;

import java.util.Collection;

import org.pm4j.common.util.collection.ListUtil;
import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmCommandImpl;

/**
 * Command that changes an attribute value.
 *
 * @author olaf boede
 */
public class PmListAddItemCommand extends PmCommandImpl implements PmCommand {

  private final Collection<PmElement> addedItems;

  @SuppressWarnings("unchecked")
  public PmListAddItemCommand(PmAttrPmList<?> pmListAttr, Collection<?> addedItems) {
    super(pmListAttr);
    this.addedItems = (Collection<PmElement>)addedItems;

//    setUndoCommand(undoCommand)
  }

//  @SuppressWarnings("unchecked")
//  PmListAddItemCommand(PmListRemoveItemCommand doCmd, PmAttrPmList<?> pmListAttr, Collection<?> addedItems) {
//    super(pmListAttr, PmListAddItemCommand.class.getName(), null, CmdLiveTime.TEMPORARY);
//    this.addedItems = (Collection<PmElement>)addedItems;
//
//    setUndoCommand(doCmd);
//  }

  public PmListAddItemCommand(PmAttrPmList<?> presentationModel, Object addedItem) {
    // FIXME olaf: mark it as a temporary command!
    this(presentationModel, ListUtil.itemToList(addedItem));
  }

  @Override
  protected void doItImpl() throws Exception {
    @SuppressWarnings("unchecked")
    PmAttrPmList<PmElement> pmListAttr = (PmAttrPmList<PmElement>) getPmParent();

    PmValueChangeCommand undoCommand = new PmValueChangeCommand(this, (PmAttrBase<?,?>)pmListAttr, pmListAttr.getValue());
    undoCommand.setUndoCommand(this);
    this.setUndoCommand(undoCommand);

    for (PmElement pm : addedItems) {
      pmListAttr.add(pm);
    }
  }

  public Collection<PmElement> getAddedItems() {
    return addedItems;
  }

}

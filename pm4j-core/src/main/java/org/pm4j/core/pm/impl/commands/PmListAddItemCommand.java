package org.pm4j.core.pm.impl.commands;

import java.util.Arrays;
import java.util.Collection;

import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmCommandImpl;

/**
 * Command that changes an attribute value.
 *
 * @author olaf boede
 */
public class PmListAddItemCommand extends PmCommandImpl implements PmCommand {

  private final Collection<PmBean<?>> addedItems;

  @SuppressWarnings("unchecked")
  public PmListAddItemCommand(PmAttrPmList<?> pmListAttr, Collection<?> addedItems) {
    super(pmListAttr);
    this.addedItems = (Collection<PmBean<?>>)addedItems;

//    setUndoCommand(undoCommand)
  }

  public PmListAddItemCommand(PmAttrPmList<?> presentationModel, Object addedItem) {
    // FIXME olaf: mark it as a temporary command!
    this(presentationModel, Arrays.asList(addedItem));
  }

  @Override
  protected void doItImpl() {
    @SuppressWarnings("unchecked")
    PmAttrPmList<PmBean<?>> pmListAttr = (PmAttrPmList<PmBean<?>>) getPmParent();

    PmCommandImpl undoCommand = new PmCommandImpl((PmAttrBase<?,?>)pmListAttr);
    undoCommand.setUndoCommand(this);
    this.setUndoCommand(undoCommand);

    for (PmBean<?> pm : addedItems) {
      pmListAttr.add(pm);
    }
  }

  public Collection<PmBean<?>> getAddedItems() {
    return addedItems;
  }

}

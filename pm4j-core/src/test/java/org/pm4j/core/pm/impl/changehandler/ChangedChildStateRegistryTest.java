package org.pm4j.core.pm.impl.changehandler;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;

public class ChangedChildStateRegistryTest {

  private ChangedChildStateRegistry registry;
  private PmObject parent;
  private PmAttrString item1, item2;
  private ComplexItemPm complexItem;

  @Before
  public void setUp() {
    parent = new PmConversationImpl();
    item1 = new PmAttrStringImpl(parent);
    item2 = new PmAttrStringImpl(parent);
    complexItem = new ComplexItemPm(parent);
    registry = new ChangedChildStateRegistry(parent);
  }

  @Test
  public void testChangeValueOfItem1() {
    assertEquals(false, registry.isAChangeRegistered());

    item1.setValue("a");
    assertEquals("After changing an item value the registry should know about a change.", true, registry.isAChangeRegistered());
    assertEquals("There should be a changed item within the registred changed item set.", 1, registry.getChangedItems().size());
  }

  @Test
  public void testChangeValueOfComplexItem() {
    assertEquals(false, registry.isAChangeRegistered());

    complexItem.i.setValue(8);
    assertEquals("After changing an item value the registry should know about a change.", true, registry.isAChangeRegistered());
    assertEquals("There should be a changed item within the registred changed item set.", 1, registry.getChangedItems().size());
  }


  @Test
  public void testChangeValueOfItem1AndClearChanges() {
    testChangeValueOfItem1();

    registry.clearChangedItems();
    assertEquals("No change should be reported after clearing the changes.", false, registry.isAChangeRegistered());
    assertEquals("No changed item should be reported after clearing the changes.", 0, registry.getChangedItems().size());
  }

  @Test
  public void testChangeValueOfItem1AndChangeItToItsOriginalValue() {
    testChangeValueOfItem1();

    item1.setValue(null);
    assertEquals("After resetting the item to its original value it should be in state 'unchanged'.", false, item1.isPmValueChanged());
    assertEquals("The registry should also reflect the unchanged state of the item.", false, registry.isAChangeRegistered());
  }

  @Test
  public void testChangeValueOfItem1AndSimulateDeleteItem1() {
    testChangeValueOfItem1();

    registry.onDeleteItem(item1);
    assertEquals("The registry should still report a change (caused by changing the item set).", true, registry.isAChangeRegistered());
    assertEquals("The set of changed items should be empty. The deleted item is not part of the changed items to consider.", 0, registry.getChangedItems().size());
  }

  @Test
  public void testChangeValueOfItem1AndAddItem2() {
    testChangeValueOfItem1();

    registry.onAddNewItem(item2);
    assertEquals("The registry should still report a change.", true, registry.isAChangeRegistered());
    assertEquals("The set of changed items should contain both items.", 2, registry.getChangedItems().size());
  }

  @Test
  public void testAddItem1() {
    registry.onAddNewItem(item1);
    assertEquals("The registry should report a change.", true, registry.isAChangeRegistered());
    assertEquals("The set of changed items should a single item.", 1, registry.getChangedItems().size());
  }

  @Test
  public void testAddItem1AndChangeItem1() {
    testAddItem1();

    item1.setValue("abc");
    assertEquals("The registry should still report a change.", true, registry.isAChangeRegistered());
    assertEquals("The set of changed items should still contain only a single item.", 1, registry.getChangedItems().size());
  }

  @Test
  public void testAddItem1AndDeleteItem1() {
    testAddItem1();

    registry.onDeleteItem(item1);
    assertEquals("The registry should no longer report a change (the add operation was undone).", false, registry.isAChangeRegistered());
    assertEquals("The set of changed items should be empty", 0, registry.getChangedItems().size());
  }

  @Test
  public void testClearChangesOnParentValueChange() {
    testAddItem1();

    PmEventApi.firePmEvent(parent, PmEvent.VALUE_CHANGE);
    assertEquals("The registry should no longer report a change.", false, registry.isAChangeRegistered());
      }


  public static class ComplexItemPm extends PmElementImpl {
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);
    public ComplexItemPm(PmObject parentPm) {
      super(parentPm);
    }
  };

}

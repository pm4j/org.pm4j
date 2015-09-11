package org.pm4j.common.modifications;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.pm4j.common.selection.ItemSetSelection;

public class ModificationsUtilTest {

  ModificationsImpl<String> modifications = new ModificationsImpl<String>();
  
  @Test
  public void testRegisterAddedItems() {
    ModificationsUtil.registerAddedItems(modifications, Arrays.asList("hello", "world"));
    assertEquals("{added: 2, updated: 0, removed: 0}", modifications.toString());
    assertEquals("[hello, world]", modifications.getAddedItems().toString());
  }
  
  @Test
  public void testRegisterUpdatedItems() {
    ModificationsUtil.registerUpdatedItems(modifications, Arrays.asList("hello", "world"));
    assertEquals("{added: 0, updated: 2, removed: 0}", modifications.toString());
    assertEquals("[hello, world]", modifications.getUpdatedItems().toString());
  }
  
  @Test
  public void testRegisterRemovedItems() {
    ModificationsUtil.registerRemovedItems(modifications, Arrays.asList("hello", "world"));
    assertEquals("{added: 0, updated: 0, removed: 2}", modifications.toString());
    assertEquals("[hello, world]", modifications.getRemovedItems().toString());
  }
  
  @Test
  public void testJoinModifications() {
    ModificationsImpl<String> m1 = new ModificationsImpl<String>();
    ModificationsImpl<String> m2 = new ModificationsImpl<String>();
    
    m1.registerAddedItem("a1");
    m1.registerUpdatedItem("u1", true);
    m1.setRemovedItems(new ItemSetSelection<String>("r1"));

    m2.registerAddedItem("a2");
    m2.registerUpdatedItem("u2", true);
    m2.setRemovedItems(new ItemSetSelection<String>("r2"));

    Modifications<String> m3 = ModificationsUtil.joinModifications(Arrays.asList(m1, m2));
    assertEquals("{added: 2, updated: 2, removed: 2}", m3.toString());
    assertEquals("[a1, a2]", m3.getAddedItems().toString());
    assertEquals("[u1, u2]", m3.getUpdatedItems().toString());
    assertEquals("[r1, r2]", m3.getRemovedItems().toString());
  }
  
}

package org.pm4j.common.modifications;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;


public class ModificationsImplTest {

  ModificationsImpl<String> modifications = new ModificationsImpl<>();
  ModificationsImpl<String> modifAbc = (ModificationsImpl<String>) ModificationsUtil.createModfications(Arrays.asList("a"), Arrays.asList("b"), Arrays.asList("c"));
  
  @Test
  public void testRegisterAddedItem() {
    modifications.registerAddedItem("a");
    assertEquals("{added: 1, updated: 0, removed: 0}", modifications.toString());
  }
  
  @Test
  public void testRegisterAddedItemTwice() {
    modifications.registerAddedItem("a");
    modifications.registerAddedItem("a");
    assertEquals("{added: 1, updated: 0, removed: 0}", modifications.toString());
  }

  @Test
  public void testUnRegisterAddedItemOnEmptyModification() {
    modifications.unregisterAddedItem("a");
    assertEquals("{}", modifications.toString());
  }
  
  @Test
  public void testUnRegisterAddedItemOnFilledModification() {
    modifAbc.unregisterAddedItem("a");
    assertEquals("{added: 0, updated: 1, removed: 1}", modifAbc.toString());
  }
  

  @Test
  public void testUnregisterUpdatedItem() {
    modifAbc.registerUpdatedItem("b", false);
    assertEquals("{added: 1, updated: 0, removed: 1}", modifAbc.toString());
  }

  @Test
  public void testUnregisterUpdatedItemTwice() {
    modifAbc.registerUpdatedItem("b", true);
    assertEquals("no change", "{added: 1, updated: 1, removed: 1}", modifAbc.toString());
  }
  
  @Test
  public void testUnregisterUpdatedItemOnEmptyModification() {
    modifications.registerUpdatedItem("b", false);
    assertEquals("{}", modifications.toString());
  }

  @Test
  public void testUnregisterUnknownUpdatedItem() {
    modifAbc.registerUpdatedItem("y", false);
    assertEquals("modification stays as it was", "{added: 1, updated: 1, removed: 1}", modifAbc.toString());
  }
  

  @Test
  public void testClone() {
    Modifications<String> m = modifAbc.clone(); 
    assertEquals("{added: 1, updated: 1, removed: 1}", m.toString());
  }
  
}

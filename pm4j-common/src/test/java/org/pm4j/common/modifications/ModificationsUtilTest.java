package org.pm4j.common.modifications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Assert;
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
  
  @Test
  public void testJoinSingleModification() {
    modifications.registerAddedItem("a");
    Modifications<String> m = ModificationsUtil.joinModifications(Arrays.asList(modifications));
    assertEquals("{added: 1, updated: 0, removed: 0}", m.toString());
    assertEquals("[a]", m.getAddedItems().toString());
  }
  
  @Test
  public void testConvertModifications() {
    modifications.registerAddedItem("a");
    modifications.registerUpdatedItem("b", true);
    modifications.setRemovedItems(new ItemSetSelection<>("c"));
    
    Modifications<String> converted = ModificationsUtil.convertModifications(modifications, new ModificationsUtil.Converter<String, String>() {
      @Override
      public String convert(String src) {
        return src + "'";
      }
    });
    
    assertEquals("{added: 1, updated: 1, removed: 1}", converted.toString());
    assertEquals("[a']", converted.getAddedItems().toString());
    assertEquals("[b']", converted.getUpdatedItems().toString());
    assertEquals("[c']", converted.getRemovedItems().toString());
  }
  
  @Test
  public void testCreateAndCheckEmptyModifications() {
    Modifications<String> m = ModificationsUtil.createModfications(null, null, null);
    assertEquals("{}", m.toString());
    assertFalse(ModificationsUtil.isModified(m, "x"));
  }
  
  @Test
  public void testCreateAndCheckAddModifications() {
    Modifications<String> m = ModificationsUtil.createModfications(Arrays.asList("x"), null, null);
    assertEquals("{added: 1, updated: 0, removed: 0}", m.toString());
    assertTrue(ModificationsUtil.isModified(m, "x"));
  }
  
  @Test
  public void testCreateAndCheckUpdateModifications() {
    Modifications<String> m = ModificationsUtil.createModfications(null, Arrays.asList("x"), null);
    assertEquals("{added: 0, updated: 1, removed: 0}", m.toString());
    assertTrue(ModificationsUtil.isModified(m, "x"));
  }
  
  @Test
  public void testCreateAndCheckRemoveModifications() {
    Modifications<String> m = ModificationsUtil.createModfications(null, null, Arrays.asList("x"));
    assertEquals("{added: 0, updated: 0, removed: 1}", m.toString());
    assertTrue(ModificationsUtil.isModified(m, "x"));
  }
  
  
  
}

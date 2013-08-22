package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.impl.PmAttrListImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmAttrListTest {

  @Test
  public void testGetAndSetValueForAListOfLongs() {
    MyTestElement e = new MyTestElement();

    e.listOfLongs.setValueAsString("1,2,3");

    assertEquals("native value list", Arrays.asList(1L,2L,3L), e.listOfLongs.getValue());
    assertEquals("as a single string", "1,2,3", e.listOfLongs.getValueAsString());
    assertEquals("as list of strings", Arrays.asList("1","2","3"), e.listOfLongs.getValueAsStringList());

    assertEquals("first 2-item subset", Arrays.asList(1L,2L), e.listOfLongs.getValueSubset(0, 2));
    assertEquals("second 2-item subset", Arrays.asList(3L), e.listOfLongs.getValueSubset(2, 2));
  }

  @Test @Ignore("FIXME oboede: The generics parameter is here the list item type. Check that.")
  public void testValueType() {
    Class<?> t = new MyTestElement().listOfLongs.getValueType();
    assertEquals(List.class, t);
  }

  enum MyEnum { V1, V2, V3 };
  public static class MyTestElement extends PmConversationImpl {
    public final PmAttrList<Long> listOfLongs = new PmAttrListImpl.Longs(this);
  }

}

package org.pm4j.core.pm;

import java.util.Arrays;

import junit.framework.TestCase;

import org.pm4j.core.pm.PmAttrList;
import org.pm4j.core.pm.annotation.PmAttrListCfg;
import org.pm4j.core.pm.impl.PmAttrListImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.converter.PmConverterLong;

public class PmAttrListTest extends TestCase {

  public void testGetAndSetValue() {
    MyTestElement e = new MyTestElement();

    e.listOfLongs.setValueAsString("1,2,3");

    assertEquals("native value list", Arrays.asList(1L,2L,3L), e.listOfLongs.getValue());
    assertEquals("as a single string", "1,2,3", e.listOfLongs.getValueAsString());
    assertEquals("as list of strings", Arrays.asList("1","2","3"), e.listOfLongs.getValueAsStringList());

    assertEquals("first 2-item subset", Arrays.asList(1L,2L), e.listOfLongs.getValueSubset(0, 2));
    assertEquals("second 2-item subset", Arrays.asList(3L), e.listOfLongs.getValueSubset(2, 2));
  }

  public static class MyTestElement extends PmConversationImpl {
    @PmAttrListCfg(itemConverter=PmConverterLong.class)
    public final PmAttrList<Long> listOfLongs = new PmAttrListImpl<Long>(this);
  }

}

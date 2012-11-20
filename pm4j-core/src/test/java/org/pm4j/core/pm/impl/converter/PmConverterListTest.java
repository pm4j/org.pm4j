package org.pm4j.core.pm.impl.converter;

import java.util.Arrays;
import java.util.List;

import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.converter.PmConverterInteger;
import org.pm4j.core.pm.impl.converter.PmConverterList;

import junit.framework.TestCase;

public class PmConverterListTest extends TestCase {

  public void testConvertAListOfIntegers() {
    PmConverterList<Integer> converter = new PmConverterList<Integer>(PmConverterInteger.INSTANCE);

    List<Integer> intList = Arrays.asList(1, 2, 3, 4);
    PmAttrImpl<Object> dummy = new PmAttrImpl<Object>(new PmConversationImpl());
    assertEquals("1,2,3,4", converter.valueToString(dummy, intList));
//    assertEquals(intList, converter.stringToValue(null, "1,2,3,4"));
  }

}

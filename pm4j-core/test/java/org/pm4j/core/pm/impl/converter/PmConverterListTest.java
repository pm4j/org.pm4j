package org.pm4j.core.pm.impl.converter;

import java.util.Arrays;
import java.util.List;

import org.pm4j.core.pm.impl.converter.PmConverterInteger;
import org.pm4j.core.pm.impl.converter.PmConverterList;

import junit.framework.TestCase;

public class PmConverterListTest extends TestCase {

  public void testConvertAListOfIntegers() {
    PmConverterList<Integer> converter = new PmConverterList<Integer>(PmConverterInteger.INSTANCE);

    List<Integer> intList = Arrays.asList(1, 2, 3, 4);
    assertEquals("1,2,3,4", converter.valueToString(null, intList));
//    assertEquals(intList, converter.stringToValue(null, "1,2,3,4"));
  }

}

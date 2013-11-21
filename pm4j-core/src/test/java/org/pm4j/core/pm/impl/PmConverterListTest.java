package org.pm4j.core.pm.impl;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.pm4j.common.converter.string.StringConverterInteger;
import org.pm4j.common.converter.string.StringConverterList;
import org.pm4j.core.pm.PmAttrNumber;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmConverterListTest extends TestCase {

  public void testConvertAListOfIntegers() {
    StringConverterList<Integer> converter = new StringConverterList<Integer>(StringConverterInteger.INSTANCE);

    List<Integer> intList = Arrays.asList(1, 2, 3, 4);
    PmAttrImpl<Object> dummy = new PmAttrImpl<Object>(new PmConversationImpl()) {
      // The attribute needs to define the item format:
      @Override
      protected String getFormatDefaultResKey() {
        return PmAttrNumber.RESKEY_DEFAULT_INTEGER_FORMAT_PATTERN;
      }
    };
    assertEquals("1,2,3,4", converter.valueToString(dummy.getConverterCtxt(), intList));
  }


}

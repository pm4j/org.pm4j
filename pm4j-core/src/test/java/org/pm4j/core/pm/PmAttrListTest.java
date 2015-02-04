package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pm4j.common.converter.string.StringConverterLong;
import org.pm4j.common.converter.string.StringConverterString;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.annotation.PmAttrListCfg;
import org.pm4j.core.pm.impl.PmAttrListImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.tools.test._PmAssert;

public class PmAttrListTest {

  private MyTestElement testPm = new MyTestElement();

  @Test
  public void testGetAndSetValueForAListOfLongs() {
    testGetAndSetValueForAListOfLongs(testPm.listOfLongs, ",");
  }
  
  @Test
  public void testGetAndSetValueForAListOfLongsWithLineBreak() {
    testGetAndSetValueForAListOfLongs(testPm.listOfLongsWithLineBreak, "\n");
  }
  
  private void testGetAndSetValueForAListOfLongs(PmAttrList<Long> pm, String itemSeparator) {
    pm.setValueAsString("1"+itemSeparator+"2"+itemSeparator+"3");

    assertEquals("native value list", Arrays.asList(1L,2L,3L), pm.getValue());
    assertEquals("as a single string", "1"+itemSeparator+"2"+itemSeparator+"3", pm.getValueAsString());
    assertEquals("as list of strings", Arrays.asList("1","2","3"), pm.getValueAsStringList());

    assertEquals("first 2-item subset", Arrays.asList(1L,2L), pm.getValueSubset(0, 2));
    assertEquals("second 2-item subset", Arrays.asList(3L), pm.getValueSubset(2, 2));
  }

  @Test
  public void testValueType() {
    assertEquals(List.class, testPm.listOfStrings.getValueType());
    assertEquals(List.class, testPm.listOfLongs.getValueType());
  }

  @Test
  public void setAndGetListOfStringValues() {
      _PmAssert.setValue(testPm.listOfStrings, Arrays.asList("a", "b"));
  }

  @Test
  public void setAndGetListOfStringValuesAsString() {
      _PmAssert.setValueAsString(testPm.listOfStrings, "a,b");
      assertEquals("[a, b]", testPm.listOfStrings.getValue().toString());
  }

  @Test(expected=PmRuntimeException.class)
  public void getValueAsStringDoesNotWorkWithoutItemConverter() {
      testPm.listOfStringsWithoutItemConverter.getValueAsString();
  }

  /** Test PM */
  public static class MyTestElement extends PmConversationImpl {
    public final PmAttrList<Long> listOfLongs = new PmAttrListImpl.Longs(this);
    
    @PmAttrListCfg(itemStringConverter = StringConverterLong.class, valueStringSeparator = "\n")
    public final PmAttrList<Long> listOfLongsWithLineBreak = new PmAttrListImpl.Longs(this);

    @PmAttrListCfg(itemStringConverter=StringConverterString.class)
    public final PmAttrList<String> listOfStrings = new PmAttrListImpl<String>(this);

    public final PmAttrList<String> listOfStringsWithoutItemConverter = new PmAttrListImpl<String>(this);

  }

}

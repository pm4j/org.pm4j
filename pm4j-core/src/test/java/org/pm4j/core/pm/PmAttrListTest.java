package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.pm4j.tools.test.PmAssert.setValue;

import java.util.Arrays;

import org.junit.Test;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.impl.PmAttrListImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmAttrListTest {

  private MyTestElement e = new MyTestElement();

  @Test
  public void testGetAndSetValueForAListOfLongs() {
    e.listOfLongs.setValueAsString("1,2,3");

    assertEquals("native value list", Arrays.asList(1L,2L,3L), e.listOfLongs.getValue());
    assertEquals("as a single string", "1,2,3", e.listOfLongs.getValueAsString());
    assertEquals("as list of strings", Arrays.asList("1","2","3"), e.listOfLongs.getValueAsStringList());

    assertEquals("first 2-item subset", Arrays.asList(1L,2L), e.listOfLongs.getValueSubset(0, 2));
    assertEquals("second 2-item subset", Arrays.asList(3L), e.listOfLongs.getValueSubset(2, 2));
  }

  @Test
  public void testGetDefaultValue() {
    assertEquals("-1,-2", e.listOfLongsWithDefault.getValueAsString());
  }

  @Test
  public void testListDefaultValueHasNoEffectOnManuallyChangedAttribute() {
    setValue(e.listOfLongsWithDefault, null);
    assertEquals(null, e.listOfLongsWithDefault.getValueAsString());
  }


  public static class MyTestElement extends PmConversationImpl {
    public final PmAttrList<Long> listOfLongs = new PmAttrListImpl.Longs(this);

    @PmAttrCfg(defaultValue="-1,-2")
    public final PmAttrList<Long> listOfLongsWithDefault = new PmAttrListImpl.Longs(this);
  }

}

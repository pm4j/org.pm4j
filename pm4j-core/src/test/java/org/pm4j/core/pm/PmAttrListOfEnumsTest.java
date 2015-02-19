package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.pm4j.core.pm.impl.PmAttrListOfEnumsImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.options.PmOptionSetUtil;

public class PmAttrListOfEnumsTest {

  @Test
  public void testGetAndSetValueForAListOfEnums() {
    MyPm e = new MyPm();

    assertEquals("An empty list will be provided as default value of the list attribute.", 0, e.listOfEnums.getValue().size());

    e.listOfEnums.setValueAsString("V1,V2,V3");
    assertEquals("A set of strings creates a corresponding native value list.", Arrays.asList(MyEnum.V1,MyEnum.V2,MyEnum.V3), e.listOfEnums.getValue());
  }

  @Test
  public void testGetOptions() {
    MyPm e = new MyPm();

    assertEquals("All enums should be part of the option set if the list is empty.",
                 "[V1, V2, V3]", PmOptionSetUtil.getOptionIds(e.listOfEnums.getOptionSet()).toString());

    e.listOfEnums.setValue(Arrays.asList(MyEnum.V1, MyEnum.V2));
    assertEquals("Only the not yet added enums should be part of the option set.",
                 "[V3]", PmOptionSetUtil.getOptionIds(e.listOfEnums.getOptionSet()).toString());

    e.listOfEnums.setValue(Arrays.asList(MyEnum.V1, MyEnum.V2, MyEnum.V3));
    assertEquals("No option should be available if all enum values are part of the list.",
                 "[]", PmOptionSetUtil.getOptionIds(e.listOfEnums.getOptionSet()).toString());

  }


  enum MyEnum { V1, V2, V3 };

  public static class MyPm extends PmConversationImpl {
    public final PmAttrList<MyEnum> listOfEnums = new PmAttrListOfEnumsImpl<MyEnum>(this, MyEnum.class);
  }

}

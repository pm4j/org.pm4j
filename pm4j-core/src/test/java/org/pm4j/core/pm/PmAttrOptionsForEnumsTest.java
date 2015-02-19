package org.pm4j.core.pm;

import java.util.Arrays;

import junit.framework.TestCase;

import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.options.PmOptionSetUtil;

public class PmAttrOptionsForEnumsTest extends TestCase {

  // see Resources.properties for resource definitions
  enum EnumWithNullResKey { ONE, TWO, THREE };
  // see Resources.properties for resource definitions
  enum EnumWithoutNullResKey { ONE, TWO };


  public static class TestPm extends PmConversationImpl {

    @PmOptionCfg(nullOption=NullOption.YES)
    public final PmAttrEnum<EnumWithNullResKey> enumAttrWithNullResKey = new PmAttrEnumImpl<EnumWithNullResKey>(this, EnumWithNullResKey.class);

    @PmOptionCfg(nullOption=NullOption.YES)
    public final PmAttrEnum<EnumWithoutNullResKey> enumAttrWithoutNullResKey = new PmAttrEnumImpl<EnumWithoutNullResKey>(this, EnumWithoutNullResKey.class);

    public final PmAttrEnum<EnumWithNullResKey> enumAttrSubSet = new PmAttrEnumImpl<EnumWithNullResKey>(this, EnumWithNullResKey.class) {
      @Override
      public Iterable<?> getOptionValues() {
        return Arrays.asList(EnumWithNullResKey.THREE, EnumWithNullResKey.ONE);
      }
    };

    public final PmAttrEnum<EnumWithNullResKey> enumAttrSubSetWithNullOption = new PmAttrEnumImpl<EnumWithNullResKey>(this, EnumWithNullResKey.class) {
      @Override
      @PmOptionCfg(nullOption=NullOption.YES)
      public Iterable<?> getOptionValues() {
        return Arrays.asList(EnumWithNullResKey.THREE, EnumWithNullResKey.ONE);
      }
    };

    public final PmAttrEnum<EnumWithNullResKey> enumAttrWithoutAdditionlSpec = new PmAttrEnumImpl<EnumWithNullResKey>(this, EnumWithNullResKey.class);

  }



  // -- Tests --

  private TestPm testPm;

  @Override
  protected void setUp() {
    testPm = new TestPm();
  }

  public void testEnumOptionsWithNullOptionTitleResource() {
    assertEqualOptions("[No selection, One, Two, Three]", testPm.enumAttrWithNullResKey);
  }

  public void testEnumOptionsWithDefaultNullOptionTitleResource() {
    assertEqualOptions("[, One, Two]", testPm.enumAttrWithoutNullResKey);
  }

  public void testEnumOptionsWithoutAdditionalSpec() {
    assertEqualOptions("[No selection, One, Two, Three]", testPm.enumAttrWithoutAdditionlSpec);
  }

  public void testEnumOptionsSubset() {
    assertEqualOptions("[No selection, Three, One]", testPm.enumAttrSubSet);
  }

  public void testEnumOptionsSubsetWithNullOption() {
    assertEqualOptions("[No selection, Three, One]", testPm.enumAttrSubSetWithNullOption);
  }


  private void assertEqualOptions(String optionsString, PmAttr<?> attr) {
    assertEquals(optionsString, PmOptionSetUtil.getOptionTitles(attr.getOptionSet()).toString());
  }

}

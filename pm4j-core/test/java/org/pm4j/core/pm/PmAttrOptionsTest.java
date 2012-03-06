package org.pm4j.core.pm;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.options.PmOptionSetUtil;

public class PmAttrOptionsTest extends TestCase {

  // -- Tests --

  public void testUsePmMethodToGetOptionset() {
    assertEqualOptions("[---, 1:a, 2:b]", testPm.item);
    testPm.item.setValueAsString("1");
    assertEquals("1:a", testPm.item.getValue().toString());
  }

  public void testSortOptionsByExpression() {
    assertEqualOptions("[---, 2:b, 1:a]", testPm.itemWithOptsSortedByNameDesc);
  }

  public void testUseNamedObjectToGetOptionset() {
    assertEqualOptions("[---, 1:a, 2:b]", testPm.itemWithNamedObjectOptions);
    testPm.itemWithNamedObjectOptions.setValueAsString("1");
    assertEquals("1:a", testPm.itemWithNamedObjectOptions.getValue().toString());
  }

  public void testNullOptionTitle() {
    assertEqualOptions("[Please select, a, b]", testPm.itemWithTitledNullOption);
  }

  public void testEnumOptionsWithNullOptionTitleResource() {
    assertEqualOptions("[No selection, One, Two, Three]", testPm.enumAttrWithNullResKey);
  }

  public void testEnumOptionsWithDefaultNullOptionTitleResource() {
    assertEqualOptions("[---, One, Two]", testPm.enumAttrWithoutNullResKey);
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

  // -- Domain structure --

  public static class Item {
    public int id;
    public String name;
    public Item(int id, String name) {
      this.id = id;
      this.name = name;
    }
    @Override
    public String toString() {
      return id+":"+name;
    }
  }

  // see Resources.properties for resource definitions
  enum EnumWithNullResKey { ONE, TWO, THREE };
  // see Resources.properties for resource definitions
  enum EnumWithoutNullResKey { ONE, TWO };

  // -- Presentation model --

  public static class TestPm extends PmConversationImpl {
    @PmOptionCfg(values="itemOpts", id="id")
    public final PmAttr<Item> item = new PmAttrImpl<Item>(this);

    public final PmAttr<Item> itemWithGetOptionValuesMethod = new PmAttrImpl<Item>(this) {
      @Override
      @PmOptionCfg(id="id", title="name")
      public Iterable<?> getOptionValues() {
        return getItemOpts();
      }
    };

    @PmOptionCfg(values="itemOpts", id="id", sortBy="title desc")
    public final PmAttr<Item> itemWithOptsSortedByNameDesc = new PmAttrImpl<Item>(this);

    @PmOptionCfg(values="#sessionPropOptions", id="id")
    public final PmAttr<Item> itemWithNamedObjectOptions = new PmAttrImpl<Item>(this);

    @PmOptionCfg(values="itemOpts", id="id", title="name", nullOptionResKey="myOption.please_select")
    public final PmAttr<Item> itemWithTitledNullOption = new PmAttrImpl<Item>(this);

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

    // - Methods that provide options in different ways: -

    /** A method that provides options. */
    public List<Item> getItemOpts() {
      return Arrays.asList(new Item(1, "a"), new Item(2, "b"));
    }

    /** Put options to a PM session property. */
    @Override
    protected void handleNamedPmObjectNotFound(String name) {
      if (name.equals("sessionPropOptions")) {
        setPmNamedObject("sessionPropOptions", getItemOpts());
      }
      super.handleNamedPmObjectNotFound(name);
    }
  }

  private TestPm testPm;

  @Override
  protected void setUp() throws Exception {
    testPm = new TestPm();
  }

}

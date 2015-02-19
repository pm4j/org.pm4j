package org.pm4j.core.pm;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.options.PmOptionSetUtil;

public class PmAttrOptionsProvidedByCfgTest extends TestCase {

  public static class TestPm extends PmConversationImpl {

    /**
     * Uses the 'values' expression to get the option objects from the method {@link #getItemOpts()}.<br>
     * The 'id' of each provided item is used to identify the object.
     */
    @PmOptionCfg(values="#myItemService.itemOpts", id="id", title="name")
    public final PmAttr<Item> attributeWithExpressionBasedOptions = new PmAttrImpl<Item>(this);

    /**
     * Extends the option configuration above by sorting the options by the
     * values provided by 'getTitle()' in descending order.<br>
     * In this case no 'title' is defined. In this case the 'toString' method of the item will be displayed as title.
     */
    @PmOptionCfg(values="#myItemService.itemOpts", id="id", sortBy="title desc")
    public final PmAttr<Item> attributeWithOptsSortedByNameDesc = new PmAttrImpl<Item>(this);

    /**
     * The attribute {@link PmOptionCfg#nullOptionResKey()} may be used to
     * define a special title for the <code>null</code> option.
     */
    @PmOptionCfg(values="#myItemService.itemOpts", id="id", title="name", nullOptionResKey="myOption.please_select")
    public final PmAttr<Item> attributeWithTitledNullOption = new PmAttrImpl<Item>(this);

  }

  public static class MyItemService {
    /** A method that provides options. */
    public List<Item> getItemOpts() {
      return Arrays.asList(new Item(1, "a"), new Item(2, "b"));
    }
  }

  private TestPm testPm;

  @Override
  protected void setUp() throws Exception {
    testPm = new TestPm();
    testPm.setPmNamedObject("myItemService", new MyItemService());
  }

  // -- Tests --

  public void testAttributeWithExpressionBasedOptions() {
    assertEqualOptions("[, a, b]", testPm.attributeWithExpressionBasedOptions);
    testPm.attributeWithExpressionBasedOptions.setValueAsString("1");
    assertEquals("1:a", testPm.attributeWithExpressionBasedOptions.getValue().toString());
  }

  public void testAttributeWithOptsSortedByNameDesc() {
    assertEqualOptions("[, 2:b, 1:a]", testPm.attributeWithOptsSortedByNameDesc);
  }

  public void testAttributeWithTitledNullOption() {
    assertEqualOptions("[Please select, a, b]", testPm.attributeWithTitledNullOption);
  }

  /** Internal helper. */
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

}

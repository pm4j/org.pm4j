package org.pm4j.core.pm;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.options.PmOptionSetUtil;

public class PmAttrOptionsProvidedByGetOptionValuesMethodTest extends TestCase {

  public static class TestPm extends PmConversationImpl {

    @PmInject
    private MyItemService myItemService;

    /**
     * Uses a service call to get the item data objects to provide as options.<br>
     * The configuration {@link PmOptionCfg} defines, what to display for each data object.
     */
    public final PmAttr<Item> attributeWithSimpleOptionSet = new PmAttrImpl<Item>(this) {
      @Override
      @PmOptionCfg(id="id", title="name")
      public Iterable<?> getOptionValues() {
        return myItemService.getItemOpts();
      }
    };

    /**
     * An example that extends the option configuration shown above by sorting the options by the
     * values provided by 'getTitle()' in descending order.
     */
    @PmOptionCfg(id="id", title="name", sortBy="title desc")
    public final PmAttr<Item> attributeWithOptsSortedByNameDesc = new PmAttrImpl<Item>(this) {
      @Override
      public Iterable<?> getOptionValues() {
        return myItemService.getItemOpts();
      }
    };

    /**
     * The attribute {@link PmOptionCfg#nullOptionResKey()} may be used to
     * define a special title for the <code>null</code> option.
     */
    public final PmAttr<Item> attributeWithTitledNullOption = new PmAttrImpl<Item>(this) {
      @Override
      @PmOptionCfg(id="id", title="name", nullOptionResKey="myOption.please_select")
      public Iterable<?> getOptionValues() {
        return myItemService.getItemOpts();
      }
    };

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
    assertEqualOptions("[, a, b]", testPm.attributeWithSimpleOptionSet);
    testPm.attributeWithSimpleOptionSet.setValueAsString("1");
    assertEquals("1:a", testPm.attributeWithSimpleOptionSet.getValue().toString());
  }

  public void testAttributeWithOptsSortedByNameDesc() {
    assertEqualOptions("[, b, a]", testPm.attributeWithOptsSortedByNameDesc);
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

package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.options.PmOptionImpl;
import org.pm4j.core.pm.impl.options.PmOptionSetImpl;
import org.pm4j.core.pm.impl.options.PmOptionSetUtil;

public class PmAttrOptionsProvidedByGetOptionsImplTest {

  public static class TestPm extends PmConversationImpl {

    @PmInject
    private MyItemService myItemService;

    /**
     * Overrides <code>getOptionSetImpl()</code> to provide the attribute options.
     */
    public final PmAttr<Item> attributeWithSimpleOptionSet = new PmAttrImpl<Item>(this) {

      @Override
      protected PmOptionSet getOptionSetImpl() {
        PmOptionSetImpl os = new PmOptionSetImpl();

        // a null-option
        os.addOption(null, "---");

        // some value options
        for (Item i : myItemService.getItemOpts()) {
          os.addOption(new PmOptionImpl(i.id, i.name, i));
        }

        return os;
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

  @Before
  public void setUp() throws Exception {
    testPm = new TestPm();
    testPm.setPmNamedObject("myItemService", new MyItemService());
  }

  // -- Tests --

  @Test
  @Ignore("FIXME olaf: Check why automatic option based conversion is not enabled in this case.")
  public void testAttributeWithSimpleOptionSet() {
    assertEqualOptions("[---, a, b]", testPm.attributeWithSimpleOptionSet);
    testPm.attributeWithSimpleOptionSet.setValueAsString("1");
    assertEquals("1:a", testPm.attributeWithSimpleOptionSet.getValue().toString());
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

package org.pm4j.core.pm.impl.options;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmAttrListImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class GenericOptionSetBuilderTest extends TestCase {

  // -- Domain structure --

  public static class Item {
    public int id;
    public String name;
    public Item subItem;

    public Item(int id, String name) {
      this(id, name, null);
    }

    public Item(int id, String name, Item subItem) {
      this.id = id;
      this.name = name;
      this.subItem = subItem;
    }

    @Override
    public String toString() {
      return "" + id + ":" + name;
    }
  }

  private Collection<Item> pojoCollection = Arrays.asList(new Item(1, "abc"), new Item(2, "def"), new Item(3, "xyz"));

  public static class TestPm extends PmConversationImpl {
    public final PmAttrStringImpl stringAttr = new PmAttrStringImpl(this);
    public final PmAttrListImpl<String> listAttr = new PmAttrListImpl<String>(this);
  }

  private static final TestPm testPm = new TestPm();

  // -- Tests --

  public void testBuildFromAPojoCollection() {
    GenericOptionSetBuilder b = new GenericOptionSetBuilder("id", "name");
    PmOptionSet os = makeOptionSet2(b, testPm.listAttr, pojoCollection);

    assertEquals("[1, 2, 3]", PmOptionSetUtil.getOptionIds(os).toString());
    assertEquals("[abc, def, xyz]", PmOptionSetUtil.getOptionTitles(os).toString());
    assertEquals("[1:abc, 2:def, 3:xyz]", PmOptionSetUtil.getOptionValues(os).toString());
  }

  public void testBuildFromAPojoCollectionWithNullOptionForOptionalScalarAttr() {
    GenericOptionSetBuilder b = new GenericOptionSetBuilder("id", "name");
    PmOptionSet os = makeOptionSet2(b, testPm.stringAttr, pojoCollection);

    assertEquals("[null, 1, 2, 3]", PmOptionSetUtil.getOptionIds(os).toString());
    assertEquals("[, abc, def, xyz]", PmOptionSetUtil.getOptionTitles(os).toString());
    assertEquals("[null, 1:abc, 2:def, 3:xyz]", PmOptionSetUtil.getOptionValues(os).toString());
  }

  public void testBuildFromAPojoCollectionAndUseNameAsOptionValue() {
    GenericOptionSetBuilder b = new GenericOptionSetBuilder("id", "name", "name", NullOption.DEFAULT, null, PmOptionCfg.NO_SORT_SPEC);
    PmOptionSet os = makeOptionSet2(b, testPm.listAttr, pojoCollection);

    assertEquals("[1, 2, 3]", PmOptionSetUtil.getOptionIds(os).toString());
    assertEquals("[abc, def, xyz]", PmOptionSetUtil.getOptionTitles(os).toString());
    assertEquals("[abc, def, xyz]", PmOptionSetUtil.getOptionValues(os).toString());
  }

  public void testBuildFromAPojoCollectionAndGetValueFromSubobjects() {
    Collection<Item> pojoCollectionWithSubObjects = Arrays.asList(
        new Item(1, "abc", new Item(4, "four")),
        new Item(2, "def", new Item(5, "fife")),
        new Item(3, "xyz", new Item(6, "six")) );

    GenericOptionSetBuilder b = new GenericOptionSetBuilder("id", "name", "subItem.name", NullOption.DEFAULT, null, PmOptionCfg.NO_SORT_SPEC);
    PmOptionSet os = makeOptionSet2(b, testPm.listAttr, pojoCollectionWithSubObjects);

    assertEquals("[1, 2, 3]", PmOptionSetUtil.getOptionIds(os).toString());
    assertEquals("[abc, def, xyz]", PmOptionSetUtil.getOptionTitles(os).toString());
    assertEquals("[four, fife, six]", PmOptionSetUtil.getOptionValues(os).toString());
  }

  public void testBuildFromAnIntegerCollection() {
    GenericOptionSetBuilder b = new GenericOptionSetBuilder();
    PmOptionSet os = makeOptionSet(b, 1, 2, 3);

    assertEquals("[1, 2, 3]", PmOptionSetUtil.getOptionIds(os).toString());
    assertEquals("[1, 2, 3]", PmOptionSetUtil.getOptionTitles(os).toString());
    assertEquals("[1, 2, 3]", PmOptionSetUtil.getOptionValues(os).toString());
  }

  public void testBuildFromAnArrayOfLongs() {
    GenericOptionSetBuilder b = new GenericOptionSetBuilder();

    Long[] longArray = {1L, 2L, 3L };
    PmOptionSet os = makeOptionSet(b, longArray);

    assertEquals("[1, 2, 3]", PmOptionSetUtil.getOptionIds(os).toString());
    assertEquals("[1, 2, 3]", PmOptionSetUtil.getOptionTitles(os).toString());
    assertEquals("[1, 2, 3]", PmOptionSetUtil.getOptionValues(os).toString());
  }

  public void testBuildFromBooleanArgumentList() {
    GenericOptionSetBuilder b = new GenericOptionSetBuilder();
    PmOptionSet os = makeOptionSet(b, true, false, true);

    assertEquals("[true, false, true]", PmOptionSetUtil.getOptionIds(os).toString());
    assertEquals("[true, false, true]", PmOptionSetUtil.getOptionTitles(os).toString());
    assertEquals("[true, false, true]", PmOptionSetUtil.getOptionValues(os).toString());
  }

  // -- helper --

  private static <T extends Object> PmOptionSet makeOptionSet(GenericOptionSetBuilder b, T... objects) {
    return makeOptionSet2(b, testPm.listAttr, Arrays.asList(objects));
  }

  private static PmOptionSet makeOptionSet2(GenericOptionSetBuilder b, PmAttrBase<?,?> forAttr, Collection<?> objects) {
    return new PmOptionSetImpl(b.makeOptions(forAttr, objects));
  }

}

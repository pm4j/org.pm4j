package org.pm4j.common.pageable.inmem;

import static junit.framework.Assert.assertEquals;
import static org.pm4j.common.util.collection.IterableUtil.shallowCopy;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.pm4j.common.pageable.TestBean;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionTestBase;
import org.pm4j.common.pageable.PageableCollectionUtil;
import org.pm4j.common.query.CompOpStartsWith;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.filter.FilterDefinition;

public class InMemCollectionTest extends PageableCollectionTestBase<TestBean> {

  @Override
  public PageableCollection<TestBean> makePageableCollection(String... strings) {
    QueryOptions qo = new QueryOptions();
    qo.addFilterCompareDefinition(new FilterDefinition(new QueryAttr("name", String.class), new CompOpStartsWith()));
    return new InMemCollectionImpl<TestBean>(makeBeans(strings), qo);
  }

  @Override
  protected TestBean createItem(int id, String name) {
    return new TestBean(id, name);
  }

  @Test
  public void testRegisterExternallyAddedAndRemovedItems() {
    Collection<TestBean> backingCollection = ((InMemCollectionImpl<TestBean>)collection).getBackingCollection();

    assertCollectionItems("[a, b, c, d, e, f]");
    TestBean newBean = new TestBean(13, "x");
    backingCollection.add(newBean);

    // change is not yet externally visible (sorted objects cache provides the result).
    assertCollectionItems("[a, b, c, d, e, f]");
    assertEquals("[]", collection.getModifications().getAddedItems().toString());

    collection.getModificationHandler().registerAddedItem(newBean);
    // change gets visible.
    assertCollectionItems("[a, b, c, d, e, f, x]");
    assertEquals("[x]", collection.getModifications().getAddedItems().toString());

    TestBean firstBean = backingCollection.iterator().next();

    backingCollection.remove(firstBean);
    backingCollection.remove(newBean);

    assertCollectionItems("[a, b, c, d, e, f, x]");
    assertEquals("[x]", collection.getModifications().getAddedItems().toString());
    // provides the removed item that was deleted within the test setup.
    assertEquals("[ ]", shallowCopy(collection.getModifications().getRemovedItems()).toString());

    collection.getModificationHandler().registerRemovedItems(Arrays.asList(firstBean, newBean));

    assertCollectionItems("[b, c, d, e, f]");
    assertEquals("[]", collection.getModifications().getAddedItems().toString());
    // provides the removed item that was deleted within the test setup.
    assertEquals("[ , a]", shallowCopy(collection.getModifications().getRemovedItems()).toString());
  }

  protected void assertCollectionItems(String itemString) {
    assertEquals(itemString, PageableCollectionUtil.shallowCopy(collection).toString());
  }

}

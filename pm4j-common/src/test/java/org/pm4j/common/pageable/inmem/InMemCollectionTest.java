package org.pm4j.common.pageable.inmem;

import static junit.framework.Assert.assertEquals;
import static org.pm4j.common.util.collection.IterableUtil.shallowCopy;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionTestBase;
import org.pm4j.common.query.CompOpStartsWith;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.filter.FilterDefinition;

public class InMemCollectionTest extends PageableCollectionTestBase<PageableCollectionTestBase.Bean> {

  @Override
  public PageableCollection<Bean> makePageableCollection(String... strings) {
    QueryOptions qo = new QueryOptions();
    qo.addFilterCompareDefinition(new FilterDefinition(new QueryAttr("name", String.class), new CompOpStartsWith()));
    return new InMemCollectionImpl<Bean>(makeBeans(strings), qo);
  }

  @Override
  protected Bean createItem(int id, String name) {
    return new Bean(id, name);
  }

  @Test
  public void testRegisterExternallyAddedAndRemovedItems() {
    Collection<Bean> backingCollection = ((InMemCollectionImpl<Bean>)collection).getBackingCollection();

    assertCollectionItems("[a, b, c, d, e, f]");
    Bean newBean = new Bean(13, "x");
    backingCollection.add(newBean);

    // change is not yet externally visible (sorted objects cache provides the result).
    assertCollectionItems("[a, b, c, d, e, f]");
    assertEquals("[]", collection.getModifications().getAddedItems().toString());

    collection.getModificationHandler().registerAddedItem(newBean);
    // change gets visible.
    assertCollectionItems("[a, b, c, d, e, f, x]");
    assertEquals("[x]", collection.getModifications().getAddedItems().toString());

    Bean firstBean = backingCollection.iterator().next();

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
    assertEquals(itemString, shallowCopy(collection).toString());
  }

}

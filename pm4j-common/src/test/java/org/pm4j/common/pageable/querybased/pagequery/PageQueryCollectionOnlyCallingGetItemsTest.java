package org.pm4j.common.pageable.querybased.pagequery;

import org.junit.Test;

/**
 * Tests a {@link PageQueryCollection} having the restriction only to call 
 * {@link PageQueryService#getItems(org.pm4j.common.query.QueryParams, long, int)} on
 * selection iteration.
 *  
 * @author Olaf Boede
 */
public class PageQueryCollectionOnlyCallingGetItemsTest extends PageQueryCollectionTestBase {

  /** Test setup variation. */
  @Override
  protected PageQueryCollection<Bean, Integer> makePageableCollection(String... strings) {
    PageQueryCollection<Bean, Integer> pc = super.makePageableCollection(strings);
    pc.setUseGetItemForIdForSingleItem(false);
    return pc;
  }
  
  // Test having different call counts as other page query tests:
  
  @Override
  public void setUp() {
    super.setUp();
    service.callCounter.assertCalls("Precondition failed:", "{getItemCount=1, getItems=4}");
    service.callCounter.reset();
  }

  @Override
  public void testItemNavigator() {
    super.testItemNavigator();
    service.callCounter.assertCalls("{getItemCount=2, getItems=5}");
  }
  
  @Override @Test
  public void testIterateSingleSelection() {
    super.testIterateSingleSelection();
    service.callCounter.assertCalls("{getItems=1}");
  }
}

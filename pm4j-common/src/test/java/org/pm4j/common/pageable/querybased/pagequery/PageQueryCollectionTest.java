package org.pm4j.common.pageable.querybased.pagequery;

import org.junit.Test;
import org.pm4j.common.pageable.PageableCollection;

/**
 * Executes the set of standard operations to test for each
 * {@link PageableCollection} for the sub class {@link PageQueryCollection}.
 *
 * @author Olaf Boede
 */
public class PageQueryCollectionTest extends PageQueryCollectionTestBase {

  @Override
  public void setUp() {
    super.setUp();
    service.callCounter.assertCalls("Precondition failed:", "{getItemCount=1, getItemForId=2, getItems=2}");
    service.callCounter.reset();
  }

  @Override
  public void testItemNavigator() {
    super.testItemNavigator();
    service.callCounter.assertCalls("{getItemCount=2, getItemForId=1, getItems=4}");
  }

  @Override @Test
  public void testIterateSingleSelection() {
    super.testIterateSingleSelection();
    service.callCounter.assertCalls("{getItemForId=1}");
  }
  
}

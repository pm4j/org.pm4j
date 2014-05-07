package org.pm4j.core.pm.impl.pageable;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.pm4j.common.pageable.PageableCollection.EVENT_ITEM_UPDATE;
import static org.pm4j.tools.test.PmAssert.setValue;

import java.util.List;

import org.junit.Test;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionTestBase;
import org.pm4j.common.query.CompOpStartsWith;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.filter.FilterDefinition;
import org.pm4j.common.query.inmem.InMemSortOrder;
import org.pm4j.common.selection.SelectMode;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl;
import org.pm4j.core.pm.impl.PmTableImpl;
import org.pm4j.tools.test.RecordingPropertyChangeListener;

public class PmBeanCollectionTest extends PageableCollectionTestBase<PmBeanCollectionTest.BeanRowPm> {

  private BeanTablePm beanTablePm  = new BeanTablePm(new PmConversationImpl());

  @Test
  public void testUpdateEventPropagation() {
    PageableCollection<BeanRowPm> pmPc = beanTablePm.getPmPageableCollection();
    PageableCollection<Bean> beanPc = beanTablePm.getPmPageableBeanCollection();
    RecordingPropertyChangeListener pclPmUpdates = pmPc.addPropertyAndVetoableListener(EVENT_ITEM_UPDATE, new RecordingPropertyChangeListener());
    RecordingPropertyChangeListener pclBeanUpdates = beanPc.addPropertyAndVetoableListener(EVENT_ITEM_UPDATE, new RecordingPropertyChangeListener());

    Bean firstBean = beanPc.getItemsOnPage().get(0);

    beanPc.getModificationHandler().registerUpdatedItem(firstBean, true);

    assertEquals(1, beanPc.getModifications().getUpdatedItems().size());
    assertEquals(1, pclBeanUpdates.getNumOfPropertyChangesCalls());

    assertEquals(1, pclPmUpdates.getNumOfPropertyChangesCalls());
    assertEquals(1, pmPc.getModifications().getUpdatedItems().size());
  }

  /**
   * Extends the basic add test with an update after add. This can be tested
   * only on PM level because the update mechanism observed PM changes.
   */
  @Override
  public void testAddItem() {
    super.testAddItem();

    // extends the base test by mo
    BeanRowPm addedItem = collection.getModifications().getAddedItems().get(0);

    setValue(addedItem.name, "another name");

    assertEquals(1, collection.getModifications().getAddedItems().size());
    assertTrue(collection.getModifications().getAddedItems().contains(addedItem));
    assertEquals("The modified item should not be part of the updates because it is already in the set of added items.",
        0, collection.getModifications().getUpdatedItems().size());
  }

  @Override
  protected PageableCollection<BeanRowPm> makePageableCollection(String... strings) {
    List<Bean> beans = makeBeans(strings);

    QueryOptions qo = new QueryOptions();
    QueryAttr attrNameValue = new QueryAttr("name", String.class);

    FilterDefinition fcd = new FilterDefinition(attrNameValue, new CompOpStartsWith());
    qo.addFilterCompareDefinition(fcd);
    qo.addSortOrder("name", new InMemSortOrder(attrNameValue));

    // here PmTableUtil.setPmBeans() is not used because we fake here some query options.
    beanTablePm.setPmPageableCollection(new PmBeanCollection<BeanRowPm, Bean>(beanTablePm, BeanRowPm.class, beans, qo));
    beanTablePm.setPmRowSelectMode(SelectMode.SINGLE);

    return beanTablePm.getPmPageableCollection();
  }

  @Override
  protected BeanRowPm createItem(int id, String name) {
    return PmFactoryApi.getPmForBean(beanTablePm, new Bean(id, name));
  }


  @PmBeanCfg(beanClass=Bean.class)
  public static class BeanRowPm extends PmBeanImpl<PageableCollectionTestBase.Bean> {
    public final PmAttrString name = new PmAttrStringImpl(this);

    @Override
    public String toString() {
      return name.getValue();
    }
  }

  @PmFactoryCfg(beanPmClasses=BeanRowPm.class)
  public static class BeanTablePm extends PmTableImpl<BeanRowPm, PageableCollectionTestBase.Bean> {

//    @PmTableColCfg(sortable=PmBoolean.TRUE)
    public final PmTableCol name = new PmTableColImpl(this);

    public BeanTablePm(PmObject pmParent) {
      super(pmParent);
    }
  }

}

package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;
import static org.pm4j.tools.test._PmAssert.setValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.SortOrder;
import org.pm4j.core.pm.PmTable.UpdateAspect;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg.CacheMode;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl;
import org.pm4j.core.pm.impl.PmTableImpl;

public class PmTableCacheTest {

  private final List<MyRowBean> listWithTwoItems = new ArrayList<MyRowBean>(Arrays.asList(new MyRowBean(), new MyRowBean()));
  private final List<MyRowBean> listWithOneItem = new ArrayList<MyRowBean>(Arrays.asList(new MyRowBean()));
  private List<MyRowBean> list = listWithTwoItems;

  private MyTablePm tablePm = new MyTablePm(new PmConversationImpl()) {
    @Override
    protected Collection<MyRowBean> getPmBeansImpl() {
      return list;
    }
  };

  private MyTablePm tablePmWithCachedCollectionRef = new MyTablePmWithCachedCollectionRef(new PmConversationImpl()) {
    @Override
    protected Collection<MyRowBean> getPmBeansImpl() {
      return list;
    }
  };

  @Test
  public void testSimpleAttributeCacheInTableRows() {
    MyRowPm row1Pm = tablePm.getRowPms().get(0);

    setValue(row1Pm.s, "hi");
    assertEquals("hi", row1Pm.sCached.getValue());

    setValue(row1Pm.s, "hey");
    assertEquals("hi", row1Pm.sCached.getValue());

    PmCacheApi.clearPmCache(tablePm);
    assertEquals("hey", row1Pm.sCached.getValue());
  }

  @Test
  public void testBackingCollectionExchangeForCachedTable() {
    assertEquals(2, tablePmWithCachedCollectionRef.getTotalNumOfPmRows());
    list = listWithOneItem;
    // do something that needs to re-access the raw backing collection item set.
    tablePmWithCachedCollectionRef.getPmQueryParams().setSortOrder(new SortOrder(new QueryAttr("s", String.class)));
    assertEquals(2, tablePmWithCachedCollectionRef.getTotalNumOfPmRows());

    PmCacheApi.clearPmCache(tablePmWithCachedCollectionRef);
    assertEquals(1, tablePmWithCachedCollectionRef.getTotalNumOfPmRows());
  }

  @Test
  public void testBackingCollectionExchangeForUncachedTable() {
    assertEquals(2, tablePm.getTotalNumOfPmRows());
    list = listWithOneItem;
    // do something that needs to re-access the raw backing collection item set.
    tablePm.getPmQueryParams().setSortOrder(new SortOrder(new QueryAttr("s", String.class)));
    assertEquals(1, tablePm.getTotalNumOfPmRows());
  }

  // FIXME oboede: this behavior is currently undefined and may cause trouble on implementation changes!
  // This is just a poor stability check...
  @Test
  public void testBackingCollectionModificationForUncachedTable() {
    assertEquals(2, tablePm.getTotalNumOfPmRows());

    list.add(new MyRowBean());
    assertEquals("A changed backing item set will currently not be detected immediately.", 2, tablePm.getTotalNumOfPmRows());
    tablePm.getPmQueryParams().setSortOrder(new SortOrder(new QueryAttr("s", String.class)));
    assertEquals("A sort order of filter change will 'detect' the changed item set.", 3, tablePm.getTotalNumOfPmRows());

    list.add(new MyRowBean());
    tablePm.updatePmTable(UpdateAspect.CLEAR_CHANGES);
    assertEquals("A notification 'CLEAR_CHANGES' makes the change visible.", 4, tablePm.getTotalNumOfPmRows());

    list.add(new MyRowBean());
    PmCacheApi.clearPmCache(tablePm);
    assertEquals("A clearPmCache() call makes the change visible.", 5, tablePm.getTotalNumOfPmRows());
  }

  // -- Domain model --

  public static class MyRowBean {
    public String s;

    // Has getters and setters to allow xPath access (used by valuePath annotation in MyPojoPm).
    public String getS() { return s; }
    public void setS(String s) { this.s = s; }
  }

  // -- Presentation models --

  @PmBeanCfg(beanClass=MyRowBean.class)
  public static class MyRowPm extends PmBeanImpl<MyRowBean> {
    public final PmAttrString s = new PmAttrStringImpl(this);

    @PmCacheCfg(value=CacheMode.ON)
    @PmAttrCfg(valuePath="pmBean.s")
    public final PmAttrString sCached = new PmAttrStringImpl(this);

    public MyRowPm(PmObject pmParent, MyRowBean myPojo) {
      super(pmParent, myPojo);
    }
  }

  @PmFactoryCfg(beanPmClasses=MyRowPm.class)
  public static class MyTablePm extends PmTableImpl<MyRowPm, MyRowBean> {

    public final PmTableCol s = new PmTableColImpl(this);

    public MyTablePm(PmObject pmParent) {
      super(pmParent);
    }
  }

  @PmCacheCfg(value=CacheMode.ON)
  public static class MyTablePmWithCachedCollectionRef extends MyTablePm {
    public MyTablePmWithCachedCollectionRef(PmObject pmParent) {
      super(pmParent);
    }
  }

}

package org.pm4j.core.pm.impl.paging;

import java.util.Comparator;
import java.util.List;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.PageableCollectionTestBase;
import org.pm4j.common.query.AttrDefinition;
import org.pm4j.common.query.CompOpStringStartsWith;
import org.pm4j.common.query.FilterCompareDefinition;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.SortOrder;
import org.pm4j.common.query.inmem.InMemSortOrder;
import org.pm4j.common.util.CompareUtil;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTableCol2;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl2;
import org.pm4j.core.pm.impl.PmTableImpl2;
import org.pm4j.core.pm.pageable2.PageablePmBeanCollection;

public class PageablePmBeanCollectionTest extends PageableCollectionTestBase<PageablePmBeanCollectionTest.BeanRowPm> {

  private BeanTablePm beanTablePm  = new BeanTablePm(new PmConversationImpl());

  @Override
  protected PageableCollection2<BeanRowPm> makePageableCollection(String... strings) {
    List<Bean> beans = makeBeans(strings);

    QueryOptions qo = new QueryOptions();
    AttrDefinition attrNameValue = new AttrDefinition("name", "name.value", String.class);

    FilterCompareDefinition fcd = new FilterCompareDefinition(attrNameValue, new CompOpStringStartsWith());
    qo.addFilterCompareDefinition(fcd);

    qo.addSortOrder("name", new InMemSortOrder(attrNameValue));

    beanTablePm.setPmPageableCollection(new PageablePmBeanCollection<BeanRowPm, Bean>(beanTablePm, beans, qo));

    return beanTablePm.getPmPageableCollection();
  }

  @Override
  protected SortOrder getOrderByName() {
//    beanTablePm.getpm
    return new InMemSortOrder(new Comparator<BeanRowPm>() {
      @Override
      public int compare(BeanRowPm o1, BeanRowPm o2) {
        return CompareUtil.compare(o1.name, o2.name);
      }
    });
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
  public static class BeanTablePm extends PmTableImpl2<BeanRowPm, PageableCollectionTestBase.Bean> {

//    @PmTableColCfg(sortable=PmBoolean.TRUE)
    public final PmTableCol2 name = new PmTableColImpl2(this);

    public BeanTablePm(PmObject pmParent) {
      super(pmParent);
    }
  }

}

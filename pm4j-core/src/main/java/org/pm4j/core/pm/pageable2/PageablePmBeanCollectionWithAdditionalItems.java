package org.pm4j.core.pm.pageable2;

import java.io.Serializable;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.querybased.PageableQueryCollection;
import org.pm4j.common.pageable.querybased.PageableQueryService;
import org.pm4j.common.query.QueryParams;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;

public class PageablePmBeanCollectionWithAdditionalItems<T_PM extends PmBean<T_BEAN>, T_BEAN>
  extends PageablePmBeanCollection<T_PM, T_BEAN> {

  public PageablePmBeanCollectionWithAdditionalItems(PmObject pmCtxt, PageableCollection2<T_BEAN> pageableBeanCollection) {
    super(pmCtxt, pageableBeanCollection);
  }

  public <T_ID extends Serializable> PageablePmBeanCollectionWithAdditionalItems(PmObject pmCtxt,
      PageableQueryService<T_BEAN, T_ID> service, QueryParams query) {
    this(pmCtxt, new PageableQueryCollection<T_BEAN, T_ID>(service, query));
  }

  public <T_ID extends Serializable> PageablePmBeanCollectionWithAdditionalItems(PmObject pmCtxt,
      PageableQueryService<T_BEAN, T_ID> service) {
    this(pmCtxt, service, null);
  }


}

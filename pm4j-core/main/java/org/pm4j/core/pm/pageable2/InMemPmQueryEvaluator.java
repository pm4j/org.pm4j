package org.pm4j.core.pm.pageable2;

import org.pm4j.common.query.AttrDefinition;
import org.pm4j.common.query.inmem.InMemQueryEvaluator;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.api.PmFactoryApi;

/**
 * A filter evaluation context for a set of beans that has an associated set of PMs.
 * <p>
 * The filter attribute path expressions start to address realated values from the PM instance.
 *
 * @author olaf boede
 *
 * @param <T_BEAN>
 */
public class InMemPmQueryEvaluator<T_BEAN> extends InMemQueryEvaluator<T_BEAN> {

  private final PmObject pmCtxt;

  public InMemPmQueryEvaluator(PmObject pmCtxt) {
    assert pmCtxt != null;
    this.pmCtxt = pmCtxt;
  }

  @Override
  public Object getAttrValue(T_BEAN item, AttrDefinition attr) {
    PmBean<?> pmBean = PmFactoryApi.<Object, PmBean<Object>>getPmForBean(pmCtxt, item);
    Object value = PmExpressionApi.findByExpression(pmBean, attr.getPathName());
    return value;
  }

}

package org.pm4j.core.pm.pageable2;

import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryAttrMulti;
import org.pm4j.common.query.inmem.InMemQueryEvaluator;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.api.PmFactoryApi;

/**
 * A filter evaluation context for a set of beans that has an associated set of PMs.
 * <p>
 * The filter attribute path expressions start to address related values from the PM instance.
 *
 * @author olaf boede
 */
public class InMemPmQueryEvaluator<T_ITEM> extends InMemQueryEvaluator<T_ITEM> {

  private final PmObject pmCtxt;

  public InMemPmQueryEvaluator(PmObject pmCtxt) {
    assert pmCtxt != null;
    this.pmCtxt = pmCtxt;
  }

  @Override
  public Object getAttrValue(Object item, QueryAttr attr) {
    PmBean<?> pmBean = PmFactoryApi.<Object, PmBean<Object>>getPmForBean(pmCtxt, item);

    if (attr instanceof QueryAttrMulti) {
      // TODO olaf:
      throw new RuntimeException("generic value access for multi attributes is not yet implemented here.");
      //AttrMulti.makeValueObject(item, fd)
    }

    String pathName = attr.getPath();
    if (pathName == null) {
      throw new IllegalArgumentException("Can't get a value for an attribute without path information. Attr: " + attr);
    }

    Object value = PmExpressionApi.findByExpression(pmBean, pathName);
    return value;
  }

}

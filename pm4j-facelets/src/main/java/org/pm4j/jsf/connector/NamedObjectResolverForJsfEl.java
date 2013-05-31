package org.pm4j.jsf.connector;

import javax.faces.context.FacesContext;

import org.pm4j.core.pm.impl.connector.NamedObjectResolver;

public class NamedObjectResolverForJsfEl implements NamedObjectResolver {

  @Override
  public Object findObject(String attrName) {
    FacesContext fc = FacesContext.getCurrentInstance();

    // TODO: use ELcontext.
    // ELContext elCtxt = fc.getELContext();
    // fc.getApplication().getExpressionFactory().createValueExpression(elCtxt, name, Object.class);

    Object o = null;
    if (fc != null) {
      o = fc.getApplication().createValueBinding("#{" + attrName + "}").getValue(fc);
    }
    return o;
  }

}

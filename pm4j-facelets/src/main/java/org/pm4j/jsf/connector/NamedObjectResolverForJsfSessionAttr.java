package org.pm4j.jsf.connector;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.pm4j.core.pm.impl.inject.NamedObjectResolver;

public class NamedObjectResolverForJsfSessionAttr implements NamedObjectResolver {

  @Override
  public Object findObject(String name) {
    FacesContext fc = FacesContext.getCurrentInstance();
    HttpServletRequest request = (HttpServletRequest)fc.getExternalContext().getRequest();
    Object value = request.getSession().getAttribute(name);
    return value;
  }


}

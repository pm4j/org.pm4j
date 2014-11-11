package org.pm4j.core.pm.impl.inject;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.common.util.reflection.ClassUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.annotation.PmInject.Mode;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;
import org.pm4j.core.pm.impl.pathresolver.PmExpressionPathResolver;

/**
 * DI resolver for setter methods annotated with {@link PmInject} using {@link PmInject.Mode#EXPRESSION}.
 *
 * @author Olaf Boede
 */
public class DiResolverFactoryPmInjectSetterByExpression implements DiResolverFactory {

  @Override
  public DiResolver makeDiResolver(Class<?> classToInspect) {
    Map<Method, PathResolver> methodInjectionMap = new HashMap<Method, PathResolver>();

    for (Method m : ClassUtil.findMethods(classToInspect, "set.*")) {
      PmInject a = m.getAnnotation(PmInject.class);
      if (a != null && (a.mode() == Mode.EXPRESSION)) {
        String propName = StringUtils.isNotBlank(a.value())
                            ? a.value()
                            : "#" + StringUtils.uncapitalize(m.getName().substring(3));

        PathResolver r = PmExpressionPathResolver.parse(new ParseCtxt(propName));
        r.setNullAllowed(a.nullAllowed());

        methodInjectionMap.put(m, r);
        DiResolverUtil.ensureAccessibility(m);
      }
    }

    return !methodInjectionMap.isEmpty()
        ? new Resolver(methodInjectionMap)
        : null;
  }

  public static class Resolver implements DiResolver {
    private Map<Method, PathResolver> methodToPathResolverMap;

    public Resolver(Map<Method, PathResolver> methodToPathResolverMap) {
      this.methodToPathResolverMap = methodToPathResolverMap;
    }

    @Override
    public void resolveDi(PmObject pm) {
      for (Map.Entry<Method, PathResolver> e : methodToPathResolverMap.entrySet()) {
        Method m = e.getKey();
        DiResolverUtil.validateGetterReturnsNull(pm, m);
        PathResolver r = e.getValue();
        Object value = DiResolverUtil.resolveValue(pm, m, r);
        DiResolverUtil.validateValidValue(pm, r.isNullAllowed(), m, value);        
        DiResolverUtil.setValue(pm, m, value);
      }
    }
  }
}

package org.pm4j.core.pm.impl.inject;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.common.util.reflection.ClassUtil;
import org.pm4j.common.util.reflection.PrefixUtil;
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

  private static class MethodData {
    final Method setter;
    final Method getter;
    final PathResolver pathResolver;
    
    public MethodData(Method method, PathResolver pathResolver) {
      this.setter = method;
      this.getter = PrefixUtil.findGetterForSetter(setter);
      this.pathResolver = pathResolver;
    }
  }
  
  @Override
  public DiResolver makeDiResolver(Class<?> classToInspect) {
    Set<MethodData> methodInjectionMap = new HashSet<MethodData>();

    for (Method m : ClassUtil.findMethods(classToInspect, "set.*")) {
      PmInject a = m.getAnnotation(PmInject.class);
      if (a != null && (a.mode() == Mode.EXPRESSION)) {
        String propName = StringUtils.isNotBlank(a.value())
                            ? a.value()
                            : "#" + StringUtils.uncapitalize(m.getName().substring(3));

        PathResolver r = PmExpressionPathResolver.parse(new ParseCtxt(propName));
        r.setNullAllowed(a.nullAllowed());

        methodInjectionMap.add(new MethodData(m, r));
        DiResolverUtil.ensureAccessibility(m);
      }
    }

    return !methodInjectionMap.isEmpty()
        ? new Resolver(methodInjectionMap)
        : null;
  }

  public static class Resolver implements DiResolver {
    private Set<MethodData> methodToPathResolverSet;

    public Resolver(Set<MethodData> methodToPathResolverSet) {
      this.methodToPathResolverSet = methodToPathResolverSet;
    }

    @Override
    public void resolveDi(PmObject pm) {
      for (MethodData injectionPoint : methodToPathResolverSet) {
        DiResolverUtil.validateGetterReturnsNull(pm, injectionPoint.getter);
        Object value = DiResolverUtil.resolveValue(pm, injectionPoint.setter, injectionPoint.pathResolver);
        DiResolverUtil.setValue(pm, injectionPoint.setter, injectionPoint.pathResolver.isNullAllowed(), value);
      }
    }
  }
}

package org.pm4j.core.pm.impl.inject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.annotation.PmInject.Mode;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;
import org.pm4j.core.pm.impl.pathresolver.PmExpressionPathResolver;
import org.pm4j.core.util.reflection.ClassUtil;

/**
 * DI resolver for setter methods annotated with {@link PmInject} using {@link PmInject.Mode#EXPRESSION}.
 *
 * @author olaf boede
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

        PathResolver r = PmExpressionPathResolver.parse(propName, false);
        r.setNullAllowed(a.nullAllowed());

        methodInjectionMap.put(m, r);
      }
    }

    return !methodInjectionMap.isEmpty()
        ? new Resolver(methodInjectionMap)
        : null;
  }

  public static class Resolver implements DiResolver {
    private Map<Method, PathResolver> fieldInjectionMap;

    public Resolver(Map<Method, PathResolver> fieldInjectionMap) {
      this.fieldInjectionMap = fieldInjectionMap;
    }

    @Override
    public void resolveDi(PmObject pm) {
      for (Map.Entry<Method, PathResolver> e : fieldInjectionMap.entrySet()) {
        Method m = e.getKey();
        PathResolver r = e.getValue();
        Object value = r.getValue(pm);

        if (value == null && ! r.isNullAllowed()) {
          throw new PmRuntimeException(pm, "Found value for dependency injection of field '" + m +
              "' was null. But null value is not allowed. " +
              "You may configure null-value handling using @PmInject(nullAllowed=...).");
        }

        try {
          m.invoke(pm, value);
        } catch (Exception ex) {
          throw new PmRuntimeException(pm, "Can't invoke method '" + m.getName() + "' in class '"
              + getClass().getName() + "'.", ex);
        }
      }
    }
  }
}

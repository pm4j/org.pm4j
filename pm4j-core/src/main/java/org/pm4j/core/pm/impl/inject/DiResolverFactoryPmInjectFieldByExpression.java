package org.pm4j.core.pm.impl.inject;

import java.lang.reflect.Field;
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
 * DI resolver for fields annotated with {@link PmInject} using {@link PmInject.Mode#EXPRESSION}.
 *
 * @author olaf boede
 */
public class DiResolverFactoryPmInjectFieldByExpression implements DiResolverFactory {

  @Override
  public DiResolver makeDiResolver(Class<?> classToInspect) {
    Map<Field, PathResolver> fieldInjectionMap = new HashMap<Field, PathResolver>();

    for (Field f : ClassUtil.getAllFields(classToInspect)) {
      PmInject a = f.getAnnotation(PmInject.class);

      if ((a != null) && (a.mode() == Mode.EXPRESSION)) {
        String propName = StringUtils.isNotBlank(a.value())
                            ? a.value()
                            : "#" + f.getName();

        PathResolver r = PmExpressionPathResolver.parse(propName, false);
        r.setNullAllowed(a.nullAllowed());

        fieldInjectionMap.put(f, r);
        DiResolverUtil.ensureAccessibility(f);
      }
    }

    return !fieldInjectionMap.isEmpty()
        ? new Resolver(fieldInjectionMap)
        : null;
  }

  public static class Resolver implements DiResolver {
    private Map<Field, PathResolver> fieldInjectionMap;

    public Resolver(Map<Field, PathResolver> fieldInjectionMap) {
      this.fieldInjectionMap = fieldInjectionMap;
    }

    @Override
    public void resolveDi(PmObject pm) {
      for (Map.Entry<Field, PathResolver> e : fieldInjectionMap.entrySet()) {
        Field f = e.getKey();
        PathResolver r = e.getValue();
        Object value = r.getValue(pm);

        if (value == null && ! r.isNullAllowed()) {
          throw new PmRuntimeException(pm, "Found value for dependency injection of field '" + f +
              "' was null. But null value is not allowed. " +
              "You may configure null-value handling using @PmInject(nullAllowed=...).");
        }

        try {
          f.set(pm, value);
        } catch (Exception ex) {
          throw new PmRuntimeException(pm, "Can't initialize field '" + f.getName() + "' in class '"
              + getClass().getName() + "'.", ex);
        }
      }
    }
  }
}

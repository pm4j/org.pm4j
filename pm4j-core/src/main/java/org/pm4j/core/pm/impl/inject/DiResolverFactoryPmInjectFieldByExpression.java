package org.pm4j.core.pm.impl.inject;

import java.lang.reflect.Field;
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

        PathResolver r = PmExpressionPathResolver.parse(new ParseCtxt(propName));
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
    private Map<Field, PathResolver> fieldToPathResolverMap;

    public Resolver(Map<Field, PathResolver> fieldInjectionMap) {
      this.fieldToPathResolverMap = fieldInjectionMap;
    }

    @Override
    public void resolveDi(PmObject pm) {
      for (Map.Entry<Field, PathResolver> e : fieldToPathResolverMap.entrySet()) {
        Field f = e.getKey();
        DiResolverUtil.validateFieldIsNull(pm, f);
        PathResolver r = e.getValue();
        Object value = DiResolverUtil.resolveValue(pm, f, r);
        DiResolverUtil.setValue(pm, f, r.isNullAllowed(), value);
      }
    }
  }
}

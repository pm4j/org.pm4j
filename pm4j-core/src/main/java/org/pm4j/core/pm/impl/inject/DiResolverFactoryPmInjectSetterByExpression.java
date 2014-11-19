package org.pm4j.core.pm.impl.inject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.common.util.reflection.ClassUtil;
import org.pm4j.common.util.reflection.PrefixUtil;
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
    List<MethodData> methodDataList = new ArrayList<MethodData>();

    for (Method m : ClassUtil.findMethods(classToInspect, "set.*")) {
      PmInject a = m.getAnnotation(PmInject.class);
      if (a != null && (a.mode() == Mode.EXPRESSION)) {
        String propName = StringUtils.isNotBlank(a.value())
                            ? a.value()
                            : "#" + StringUtils.uncapitalize(m.getName().substring(3));

        PathResolver r = PmExpressionPathResolver.parse(new ParseCtxt(propName));
        r.setNullAllowed(a.nullAllowed());

        methodDataList.add(new MethodData(m, r));
        DiResolverUtil.ensureAccessibility(m);
      }
    }

    return !methodDataList.isEmpty()
        ? new Resolver(methodDataList)
        : null;
  }

  public static class Resolver implements DiResolver {
    private List<MethodData> methodDataList;

    public Resolver(List<MethodData> methodDataList) {
      this.methodDataList = methodDataList;
    }

    @Override
    public void resolveDi(PmObject pm) {
      for (MethodData injectionPoint : methodDataList) {
        DiResolverUtil.validateGetterReturnsNull(pm, injectionPoint.getter);
        Object value = DiResolverUtil.resolveValue(pm, injectionPoint.setter, injectionPoint.pathResolver);
        DiResolverUtil.setValue(pm, injectionPoint.setter, injectionPoint.pathResolver.isNullAllowed(), value);
      }
    }
  }
}

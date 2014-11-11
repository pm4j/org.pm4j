package org.pm4j.core.pm.impl.inject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.pm4j.common.util.reflection.ClassUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.annotation.PmInject.Mode;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * DI resolver for setter methods annotated with {@link PmInject} using {@link PmInject#mode()} PARENT_OF_TYPE.
 *
 * @author olaf boede
 */
public class DiResolverFactoryPmInjectSetterByParentOfType implements DiResolverFactory {

  @Override
  public DiResolver makeDiResolver(Class<?> classToInspect) {
    List<MethodData> annotatedMethods = new ArrayList<MethodData>();

    for (Method m : ClassUtil.findMethods(classToInspect, "set.*")) {
      PmInject a = m.getAnnotation(PmInject.class);

      if (a != null && (a.mode() == Mode.PARENT_OF_TYPE)) {
        annotatedMethods.add(new MethodData(m, a));
        DiResolverUtil.ensureAccessibility(m);
      }
    }

    return !annotatedMethods.isEmpty()
        ? new Resolver(annotatedMethods)
        : null;
  }

  public static class Resolver implements DiResolver {
    private final List<MethodData> annotatedMethods;

    public Resolver(List<MethodData> annotatedMethods) {
      this.annotatedMethods = annotatedMethods;
    }

    @Override
    public void resolveDi(PmObject pm) {
      for (MethodData md : annotatedMethods) {
        Object value = PmUtil.findPmParentOfType(pm, md.type);
        DiResolverUtil.validateGetterReturnsNull(pm, md.method);
        DiResolverUtil.validateValidValue(pm, md.annotation.nullAllowed(), md.method, value);
        DiResolverUtil.setValue(pm, md.method, value);
      }
    }
  }

  static class MethodData {
    final Method method;
    final PmInject annotation;
    final Class<?> type;

    public MethodData(Method method, PmInject annotation) {
      this.method = method;
      this.annotation = annotation;
      this.type = getSetterParamType(method, annotation);
    }

    private Class<?> getSetterParamType(Method m, PmInject annotation) {
      if (annotation.mode() != Mode.PARENT_OF_TYPE) {
        return null;
      }

      Class<?>[] params = m.getParameterTypes();
      if (params.length != 1) {
        throw new PmRuntimeException("Unable to apply @PmInject to a setter method with more than one parameter.\nMethod: " + m);
      }
      return params[0];
    }
  };

}

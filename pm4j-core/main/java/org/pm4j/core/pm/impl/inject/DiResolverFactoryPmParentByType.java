package org.pm4j.core.pm.impl.inject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.core.util.reflection.ClassUtil;

public class DiResolverFactoryPmParentByType implements DiResolverFactory {

  @Override
  public DiResolver makeDiResolver(Class<?> classToInspect) {
    Map<Field, Boolean> fieldsWithParentByTypeInjectionsToNullAllowedFlag = new HashMap<Field, Boolean>();

    for (Field f : ClassUtil.getAllFields(classToInspect)) {
      PmInject a = f.getAnnotation(PmInject.class);

      if (a != null && a.parentByType()) {
        fieldsWithParentByTypeInjectionsToNullAllowedFlag.put(f, a.nullAllowed());
      }
    }

    return !fieldsWithParentByTypeInjectionsToNullAllowedFlag.isEmpty()
        ? new Resolver(fieldsWithParentByTypeInjectionsToNullAllowedFlag)
        : null;
  }

  public static class Resolver implements DiResolver {
    Map<Field, Boolean> fieldsWithParentByTypeInjectionsToOptinalFlag;

    public Resolver(Map<Field, Boolean> fieldInjections) {
      this.fieldsWithParentByTypeInjectionsToOptinalFlag = fieldInjections;
    }

    @Override
    public void resolveDi(PmObject pm) {
      for (Map.Entry<Field, Boolean> e : fieldsWithParentByTypeInjectionsToOptinalFlag.entrySet()) {
        Field f = e.getKey();
        Object value = PmUtil.findPmParentOfType(pm, f.getType());
        if (value == null && !e.getValue()) {
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

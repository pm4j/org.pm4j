package org.pm4j.core.pm.impl.inject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.pm4j.common.util.reflection.ClassUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.annotation.PmInject.Mode;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * DI resolver for fields annotated with {@link PmInject} using {@link PmInject#mode()} PARENT_OF_TYPE.
 *
 * @author olaf boede
 */
public class DiResolverFactoryPmInjectFieldByParentOfType implements DiResolverFactory {

  @Override
  public DiResolver makeDiResolver(Class<?> classToInspect) {
    Map<Field, Boolean> fieldsWithParentByTypeInjectionsToNullAllowedFlag = new HashMap<Field, Boolean>();

    for (Field f : ClassUtil.getAllFields(classToInspect)) {
      PmInject a = f.getAnnotation(PmInject.class);

      if (a != null && (a.mode() == Mode.PARENT_OF_TYPE)) {
        fieldsWithParentByTypeInjectionsToNullAllowedFlag.put(f, a.nullAllowed());
        DiResolverUtil.ensureAccessibility(f);
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
        DiResolverUtil.validateFieldIsNull(pm, f);
        Object value = PmUtil.findPmParentOfType(pm, f.getType());
        DiResolverUtil.setValue(pm, f, e.getValue(), value);        
      }
    }
  }

}

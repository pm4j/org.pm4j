package org.pm4j.core.pm.impl.inject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.util.reflection.ClassUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmProduces;

/**
 * A resolver for {@link PmProduces} annotations located on {@link Field}s.
 * <p>
 * Places the produced values as named objects within the {@link PmConversation}.
 *
 * @author Olaf Boede
 */
public class DiResolverFactoryPmProduces implements DiResolverFactory {

  /**
   * Generates a {@link ProducesDiResolver} if it finds {@link PmProduces}
   * annotations within the given class.
   */
  @Override
  public DiResolver makeDiResolver(Class<?> classToInspect) {
    Map<String, Field> fieldInjectionMap = new HashMap<String, Field>();
    for (Field f : ClassUtil.getAllFields(classToInspect)) {
      PmProduces a = f.getAnnotation(PmProduces.class);
      if (a != null) {
        String propName = StringUtils.isNotBlank(a.name())
                            ? a.name()
                            : f.getName();
        fieldInjectionMap.put(propName, f);
        // TODO oboede:check if needed
        // DiResolverUtil.ensureAccessibility(f);
      }
    }
    return !fieldInjectionMap.isEmpty()
        ? new Resolver(fieldInjectionMap)
        : null;
  }

  static class Resolver implements DiResolver {
    private Map<String, Field> fieldInjectionMap = new HashMap<String, Field>();

    public Resolver(Map<String, Field> fieldInjectionMap) {
      this.fieldInjectionMap = fieldInjectionMap;
    }


    @Override
    public void resolveDi(PmObject object) {
      PmConversation conversation = object.getPmConversation();
      for (Map.Entry<String, Field> e : fieldInjectionMap.entrySet()) {
        Object existingObj = conversation.getPmNamedObject(e.getKey());
        if (existingObj != null) {
          throw new PmRuntimeException(object, "Named object '" + e.getKey() + "' already exists." +
                  "\nUnable to apply @PmProduces for field: " + e.getValue());
        }
        Object value;
        try {
          value = e.getValue().get(object);
        } catch (Exception e1) {
          throw new PmRuntimeException(object, "Unable to access value of field: " + e.getValue(), e1);
        }
        if (value == null) {
          throw new PmRuntimeException(object, "Field used in @PmProduces is null. " +
              "\nField: " + e.getValue());
        }
        conversation.setPmNamedObject(e.getKey(), value);
      }
    }

  }
}

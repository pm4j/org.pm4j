package org.pm4j.tools.scaffold;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;

import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.common.util.reflection.PrefixUtil;

public class AttrInfo {

  private static final Logger LOG = LoggerFactory.getLogger(AttrInfo.class);

  private final String name;
  private final Method getterMethod;

  public AttrInfo(String name, Method getterMethod) {
    this.name = name;
    this.getterMethod = getterMethod;
  }

  public String getName() {
    return this.name;
  }

  public Method getGetterMethod() {
    return this.getterMethod;
  }

  public boolean hasSetterMethod() {
    Class<?> type = getterMethod.getReturnType();
    String setterName = PrefixUtil.setterNameForGetter(getterMethod.getName());
    try {
      getterMethod.getDeclaringClass().getMethod(setterName, type);
      return true;
    } catch (NoSuchMethodException l_e) {
      return false;
    }
  }

  public boolean isCollection() {
    return Collection.class.isAssignableFrom(getterMethod.getReturnType());
  }

  public boolean isEnum() {
    return getterMethod.getReturnType().isEnum();
  }

  public Class<?> getNonPrimitiveReturnType() {
    Class<?> type = getterMethod.getReturnType();
    if (type.isPrimitive()) {
      type = ClassUtils.primitiveToWrapper(type);
    }
    return type;
  }

  public String getNonPrimitiveReturnTypeName() {
    return ClassUtils.getShortClassName(getNonPrimitiveReturnType());
  }

  public Class<?> getGenericsArgOfReturnType() {
    Type genType = getterMethod.getGenericReturnType();
    String name = genType.toString();
    String genArgClsName = null;

    int ltPos = name.indexOf('<');
    int gtPos = name.indexOf('>');
    if ((ltPos != -1) && (gtPos != -1)) {
      genArgClsName = name.substring(ltPos+1, gtPos);
    }

    try {
      Class<?> argCls = Class.forName(genArgClsName);
      if (argCls != null) {
        return argCls;
      }
    } catch (ClassNotFoundException l_e) {
      LOG.warn("Class '" + genArgClsName + "' not found for signature: " + genType);
    }

    LOG.warn("Was not able to find generic return type argument class not found for signature: " + genType);
    return Object.class;
  }


}

/**
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pm4j.common.util.reflection;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Enhanced type resolution utilities. Originally based on
 * org.springframework.core.GenericTypeResolver
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author oboede
 */
public final class GenericTypeUtil {
  private GenericTypeUtil() {
  }

  /** Cache of type variable/argument pairs */
  private static final Map<Class<?>, Reference<Map<TypeVariable<?>, Type>>> typeVariableCache = Collections
      .synchronizedMap(new WeakHashMap<Class<?>, Reference<Map<TypeVariable<?>, Type>>>());
  private static boolean cacheEnabled = true;

  /**
   * Enables the internal caching of resolved TypeVariables.
   */
  public static void enableCache() {
    cacheEnabled = true;
  }

  /**
   * Disables the internal caching of resolved TypeVariables.
   */
  public static void disableCache() {
    typeVariableCache.clear();
    cacheEnabled = false;
  }

  /**
   * Reads a the generic type argument.
   *
   * @param baseType
   *          The type that we know that it has a generic type argument that we
   *          want to read. E.g. Map<K, V>.
   * @param concreteSubTypeToAnalyze
   *          The concrete sub-type that provides the argument value to read.
   *          E.g. HashMap<String, Long>.
   * @param paramIdx
   *          Index of the generic type parameter to read. Zero for the first
   *          parameter.
   * @return The read generic type parameter.
   */
  public static <T, S extends T> Class<?> resolveGenericArgument(Class<T> baseType, Class<S> concreteSubTypeToAnalyze, int paramIdx) {
    assert paramIdx >= 0;

    Class<?>[] genParams = GenericTypeUtil.resolveRawArguments(baseType, concreteSubTypeToAnalyze);
    return genParams != null && genParams.length > paramIdx
        ? genParams[paramIdx]
        : null;
  }

  /**
   * Returns an array of raw classes representing type arguments for the
   * {@code type} using type variable information from the {@code subType}.
   * Arguments for {@code type} that cannot be resolved are returned as
   * {@code null}. If no arguments can be resolved then {@code null} is
   * returned.
   *
   * @param type
   *          to resolve arguments for
   * @param subType
   *          to extract type variable information from
   * @return array of raw classes representing type arguments for the
   *         {@code type} else {@code null} if no type arguments are declared
   */
  static <T, S extends T> Class<?>[] resolveRawArguments(Class<T> type, Class<S> subType) {
    return resolveRawArguments(resolveGenericType(type, subType), subType);
  }

  /**
   * Returns an array of raw classes representing type arguments for the
   * {@code genericType} using type variable information from the
   * {@code subType}. Arguments for {@code genericType} that cannot be resolved
   * are returned as {@code null}. If no arguments can be resolved then
   * {@code null} is returned.
   *
   * @param genericType
   *          to resolve arguments for
   * @param subType
   *          to extract type variable information from
   * @return array of raw classes representing type arguments for the
   *         {@code genericType} else {@code null} if no type arguments are
   *         declared
   */
  private static Class<?>[] resolveRawArguments(Type genericType, Class<?> subType) {
    Class<?>[] result = null;

    if (genericType instanceof ParameterizedType) {
      ParameterizedType paramType = (ParameterizedType) genericType;
      Type[] arguments = paramType.getActualTypeArguments();
      result = new Class[arguments.length];
      for (int i = 0; i < arguments.length; i++)
        result[i] = resolveRawClass(arguments[i], subType);
    } else if (genericType instanceof TypeVariable) {
      result = new Class[1];
      result[0] = resolveRawClass(genericType, subType);
    }

    return result;
  }

  /**
   * Returns the generic {@code type} using type variable information from the
   * {@code subType} else {@code null} if the generic type cannot be resolved.
   *
   * @param type
   *          to resolve generic type for
   * @param subType
   *          to extract type variable information from
   * @return generic {@code type} else {@code null} if it cannot be resolved
   */
  private static Type resolveGenericType(Class<?> type, Type subType) {
    Class<?> rawType;
    if (subType instanceof ParameterizedType)
      rawType = (Class<?>) ((ParameterizedType) subType).getRawType();
    else
      rawType = (Class<?>) subType;

    if (type.equals(rawType))
      return subType;

    Type result;
    if (type.isInterface()) {
      for (Type superInterface : rawType.getGenericInterfaces())
        if (superInterface != null && !superInterface.equals(Object.class))
          if ((result = resolveGenericType(type, superInterface)) != null)
            return result;
    }

    Type superClass = rawType.getGenericSuperclass();
    if (superClass != null && !superClass.equals(Object.class))
      if ((result = resolveGenericType(type, superClass)) != null)
        return result;

    return null;
  }

  /**
   * Resolves the raw class for the {@code genericType}, using the type variable
   * information from the {@code subType} else {@link null} if the raw class
   * cannot be resolved.
   *
   * @param type
   *          to resolve raw class for
   * @param subType
   *          to extract type variable information from
   * @return raw class for the {@code genericType} else {@link null} if it
   *         cannot be resolved
   */
  private static Class<?> resolveRawClass(Type genericType, Class<?> subType) {
    if (genericType instanceof Class) {
      return (Class<?>) genericType;
    } else if (genericType instanceof ParameterizedType) {
      return resolveRawClass(((ParameterizedType) genericType).getRawType(), subType);
    } else if (genericType instanceof GenericArrayType) {
      GenericArrayType arrayType = (GenericArrayType) genericType;
      Class<?> compoment = resolveRawClass(arrayType.getGenericComponentType(), subType);
      return Array.newInstance(compoment, 0).getClass();
    } else if (genericType instanceof TypeVariable) {
      TypeVariable<?> variable = (TypeVariable<?>) genericType;
      genericType = getTypeVariableMap(subType).get(variable);
      genericType = genericType == null ? resolveBound(variable) : resolveRawClass(genericType, subType);
    }

    return genericType instanceof Class ? (Class<?>) genericType : null;
  }

  private static Map<TypeVariable<?>, Type> getTypeVariableMap(final Class<?> targetType) {
    Reference<Map<TypeVariable<?>, Type>> ref = typeVariableCache.get(targetType);
    Map<TypeVariable<?>, Type> map = ref != null ? ref.get() : null;

    if (map == null) {
      map = new HashMap<TypeVariable<?>, Type>();

      // Populate interfaces
      buildTypeVariableMap(targetType.getGenericInterfaces(), map);

      // Populate super classes and interfaces
      Type genericType = targetType.getGenericSuperclass();
      Class<?> type = targetType.getSuperclass();
      while (type != null && !Object.class.equals(type)) {
        if (genericType instanceof ParameterizedType)
          buildTypeVariableMap((ParameterizedType) genericType, map);
        buildTypeVariableMap(type.getGenericInterfaces(), map);

        genericType = type.getGenericSuperclass();
        type = type.getSuperclass();
      }

      // Populate enclosing classes
      type = targetType;
      while (type.isMemberClass()) {
        genericType = type.getGenericSuperclass();
        if (genericType instanceof ParameterizedType)
          buildTypeVariableMap((ParameterizedType) genericType, map);

        type = type.getEnclosingClass();
      }

      if (cacheEnabled)
        typeVariableCache.put(targetType, new WeakReference<Map<TypeVariable<?>, Type>>(map));
    }

    return map;
  }

  /**
   * Populates the {@code map} with with variable/argument pairs for the given
   * {@code types}.
   */
  private static void buildTypeVariableMap(final Type[] types, final Map<TypeVariable<?>, Type> map) {
    for (Type type : types) {
      if (type instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        buildTypeVariableMap(parameterizedType, map);
        Type rawType = parameterizedType.getRawType();
        if (rawType instanceof Class)
          buildTypeVariableMap(((Class<?>) rawType).getGenericInterfaces(), map);
      } else if (type instanceof Class) {
        buildTypeVariableMap(((Class<?>) type).getGenericInterfaces(), map);
      }
    }
  }

  /**
   * Populates the {@code typeVariableMap} with type arguments and parameters
   * for the given {@code type}.
   */
  private static void buildTypeVariableMap(ParameterizedType type, Map<TypeVariable<?>, Type> typeVariableMap) {
    if (type.getRawType() instanceof Class) {
      TypeVariable<?>[] typeVariables = ((Class<?>) type.getRawType()).getTypeParameters();
      Type[] typeArguments = type.getActualTypeArguments();

      for (int i = 0; i < typeArguments.length; i++) {
        TypeVariable<?> variable = typeVariables[i];
        Type typeArgument = typeArguments[i];

        if (typeArgument instanceof Class) {
          typeVariableMap.put(variable, typeArgument);
        } else if (typeArgument instanceof GenericArrayType) {
          typeVariableMap.put(variable, typeArgument);
        } else if (typeArgument instanceof ParameterizedType) {
          typeVariableMap.put(variable, typeArgument);
        } else if (typeArgument instanceof TypeVariable) {
          TypeVariable<?> typeVariableArgument = (TypeVariable<?>) typeArgument;
          Type resolvedType = typeVariableMap.get(typeVariableArgument);
          if (resolvedType == null)
            resolvedType = resolveBound(typeVariableArgument);
          typeVariableMap.put(variable, resolvedType);
        }
      }
    }
  }

  /**
   * Resolves the first bound for the {@code typeVariable}, returning
   * {@code null} if none can be resolved.
   */
  private static Type resolveBound(TypeVariable<?> typeVariable) {
    Type[] bounds = typeVariable.getBounds();
    if (bounds.length == 0)
      return null;

    Type bound = bounds[0];
    if (bound instanceof TypeVariable)
      bound = resolveBound((TypeVariable<?>) bound);

    return bound == Object.class ? null : bound;
  }
}

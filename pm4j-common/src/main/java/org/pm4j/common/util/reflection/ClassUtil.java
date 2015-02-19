package org.pm4j.common.util.reflection;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.common.exception.CheckedExceptionWrapper;

/**
 * Some functions that make it easier to use of some reflection features.
 */
public class ClassUtil {

  private static final Logger LOG = LoggerFactory.getLogger(ClassUtil.class);

  private static final Class<?>[] EMPTY_CLASS_ARRAY = {};

  private static final Object[] EMPTY_OBJECT_ARRAY = {};

  private static final Set<String> EMPTY_STRING_SET = Collections.emptySet();

  /**
   * Creates an instance of the given class using the default constructor.
   *
   * @param <T>
   *          The instance fieldClass that the calling code expects.
   * @param forClass
   *          The class to get an instance for.
   * @return The created instance.
   * @throws IllegalStateException
   *           when the given class has no public default constructor.
   */
  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class<?> forClass, Object... args) {
    Object result = null;

    try {
      Constructor<Object> ctor = findConstructorByArgNum(forClass, args.length);

      if (ctor == null) {
        throw new IllegalStateException(args.length + "-argument constructor missing for class " + forClass);
      }

      result = ctor.newInstance(args);
    } catch (Exception e) {
      CheckedExceptionWrapper.throwAsRuntimeException(e);
    }

    return (T) result;
  }

  /**
   * Creates an instance with the given constructor instance.
   * <p>
   * Is a simple helper that just hides the exception code required by the
   * reflection api.
   *
   * @param <T>
   *          The fieldClass of the new instance.
   * @param constructor
   *          The constructor of the instance to create.
   * @param args
   *          The constructor arguments.
   * @return The new instance.
   */
  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Constructor<?> constructor, Object... args) {
    Object result = null;

    try {
      result = constructor.newInstance(args);
    } catch (Exception e) {
      CheckedExceptionWrapper.throwAsRuntimeException(e);
    }

    return (T) result;
  }

  /**
   * Checks if there is a public default constructor defined for the given
   * class.
   *
   * @param aClass
   * @return <code>true</code> if there is a public default ctor.
   */
  public static boolean hasDefaultConstructor(Class<?> aClass) {
    try {
      Constructor<?> ctor = aClass.getConstructor(EMPTY_CLASS_ARRAY);
      return (ctor != null);
    } catch (SecurityException e) {
      throw new CheckedExceptionWrapper(e);
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  /**
   * Searches the first matching constructor within the given class
   * <code>cls</code> that has an argument set that is assignable to the
   * arguments described by the parameter <code>args</code>.
   * <p>
   * TODO ob: this method still does not find the best matching constructor.
   * Check the apache commons bean utils for such functionality.
   *
   * @param cls
   *          The class that might contain the matching constructor.
   * @param args
   *          The requested argument set.
   * @return The found constructor or <code>null</code>
   */
  @SuppressWarnings("unchecked")
  public static <T> Constructor<T> findConstructor(Class<T> cls, Class<?>... args) {
    Constructor<T> constructor = null;

    Constructor<T>[] ctors = (Constructor<T>[]) cls.getConstructors();
    for (int i = 0; i < ctors.length; i++) {
      Class<?>[] ptypes = ctors[i].getParameterTypes();
      if (args.length == ptypes.length) {
        boolean doesMatch = true;
        for (int pidx = 0; pidx < args.length; ++pidx) {
          if (!args[pidx].isAssignableFrom(ptypes[pidx])) {
            doesMatch = false;
            break;
          }
        }

        if (doesMatch) {
          constructor = (Constructor<T>) ctors[i];
        }
      }
    }

    return constructor;
  }

  @SuppressWarnings("unchecked")
  private static Constructor<Object> findConstructorByArgNum(Class<?> cls, int argNum) {
    Constructor<Object> constructor = null;

    Constructor<?>[] ctors = cls.getConstructors();
    for (int i = 0; i < ctors.length; i++) {
      Class<?>[] ptypes = ctors[i].getParameterTypes();
      if (argNum == ptypes.length) {
        return (Constructor<Object>) ctors[i];
      }
    }

    return constructor;
  }

  /**
   * Searches the matching constructor of the given class <code>cls</code> that
   * has a matching argument set.
   * <p>
   *
   * @see #findConstructor(Class, Class[]) for details.
   *
   * @param cls
   *          The class that might contain the matching constructor.
   * @param args
   *          The requested argument set.
   * @return The found constructor or <code>null</code>.
   * @throws IllegalArgumentException
   *           when there is no match.
   */
  public static <T> Constructor<T> getConstructor(Class<T> cls, Class<?>... args) {
    Constructor<T> c = ClassUtil.findConstructor(cls, args);

    if (c == null) {
      throw new IllegalArgumentException("No matching constructor for class '" + cls + "' found.\n"
          + "Required constructor arguments: " + Arrays.toString(args));
    }

    return c;
  }

  /**
   * Searches a getter by checking all public getters to return the given
   * fieldValue reference.
   *
   * @param object
   *          The instance to find the value reference in.
   * @param fieldValue
   *          The value reference that a getter within the given instance should
   *          return.
   * @return The found getter method or <code>null</code> when there is no
   *         getter that provides the requested field value.
   */
  public static Method findPublicGetterByValueRef(Object object, Object fieldValue) {
    return findPublicGetterByValueRef(object, fieldValue, EMPTY_STRING_SET);
  }

  /**
   * Searches a getter by checking all public getters to return the given
   * fieldValue reference.
   *
   * @param object
   *          The instance to find the value reference in.
   * @param fieldValue
   *          The value reference that a getter within the given instance should
   *          return.
   * @param forbiddenClasses
   *          The getters declared in this class set will not be scanned by this
   *          method call.
   * @return The found getter method or <code>null</code> when there is no
   *         getter that provides the requested field value.
   */
  public static Method findPublicGetterByValueRef(Object object, Object fieldValue, Set<String> forbiddenGetterNames) {
    assert fieldValue != null;
    Class<?> cls = object.getClass();

    for (Method m : cls.getMethods()) {
      if ((!forbiddenGetterNames.contains(m.getName())) && PrefixUtil.hasGetterPrefix(m)) {
        try {
          Object getterResult = m.invoke(object, EMPTY_OBJECT_ARRAY);
          if (getterResult == fieldValue) {
            return m;
          }
        } catch (Exception e) {
          // ok. this candidate does not work...
          //
          // throw new ReflectionException("Unable to call getter '" +
          // cls.getName() + "." + m.getName() + "'", e);
        }
      }
    }

    // not found
    return null;
  }

  /**
   * Searches a field by checking all public fields of the given object to hold
   * the given fieldValue reference.
   *
   * @param object
   *          The instance to find the value reference in.
   * @param fieldValue
   *          The value reference to find the holder field for.
   * @return The found field or <code>null</code> when there is no public field
   *         that provides the requested field value.
   */
  public static Field findPublicFieldByValueRef(Object object, Object fieldValue) {
    assert fieldValue != null;
    Class<?> cls = object.getClass();
    for (Field f : cls.getFields()) {
      try {
        Object valueOfF;
        try {
          valueOfF = f.get(object);
        } catch (IllegalAccessException e) {
          // TODO olaf: Workaround for a security exception when trying to get
          // the value of a public final member. - Why shouldn't that be
          // accessible via reflection?
          // Try to get rid of this, because it prevents activation of the
          // security manager. (some server and nearly all applet scenarios!)
          f.setAccessible(true);
          valueOfF = f.get(object);
        }

        if (valueOfF == fieldValue) {
          return f;
        }
      } catch (Exception e) {
        throw new ReflectionException("Unable to access field '" + cls.getName() + "." + f.getName() + "'", e);
      }
    }

    // not found
    return null;
  }

  /**
   * Searches the name of an attribute by searching for a getter or public field
   * that holds the given fieldValue reference.
   *
   * @param object
   *          The instance to find the value reference in.
   * @param fieldValue
   *          The value reference that a getter within the given instance should
   *          return or a public field should hold.
   * @return The found attribute name or <code>null</code> when there is
   *         matching public getter or public field holding the given value
   *         reference.
   */
  public static String findPublicAttrName(Object object, Object fieldValue) {
    return findPublicAttrName(object, fieldValue, EMPTY_STRING_SET);
  }

  /**
   * Searches the name of an attribute by searching for a getter or public field
   * that holds the given fieldValue reference.
   *
   * @param object
   *          The instance to find the value reference in.
   * @param fieldValue
   *          The value reference that a getter within the given instance should
   *          return or a public field should hold.
   * @param forbiddenGetterNames
   *          The set of getters that shouldn't be inspected by this method.
   * @return The found attribute name or <code>null</code> when there is
   *         matching public getter or public field holding the given value
   *         reference.
   */
  public static String findPublicAttrName(Object object, Object fieldValue, Set<String> forbiddenGetterNames) {
    String name = null;

    Field field = findPublicFieldByValueRef(object, fieldValue);
    if (field != null) {
      name = field.getName();
    } else {
      Method getter = findPublicGetterByValueRef(object, fieldValue, forbiddenGetterNames);
      if (getter != null) {
        name = PrefixUtil.getterBaseName(getter.getName());
        if (name != null) {
          name = StringUtils.uncapitalize(name);
        }
      }
    }

    return name;
  }

  /**
   * Provides the set of all getters defined in the given class.
   *
   * @param forClass
   *          The class to inspect.
   * @return The set of getter method names.
   */
  public static Set<String> allPublicGetterNames(Class<?> forClass) {
    Set<String> nonAttributeGetterNames = new HashSet<String>();
    for (Method m : forClass.getMethods()) {
      if (PrefixUtil.hasGetterPrefix(m)) {
        nonAttributeGetterNames.add(m.getName());
      }
    }
    return nonAttributeGetterNames;
  }

  /**
   * Searches for a method signature.
   * <p>
   * In difference to {@link Class#getMethod(String, Class...)} it does not
   * throw an exception if the method does not exists. It returns only <code>null</code> in that case.
   *
   * @param beanClass The class to find the method in.
   * @param methodName Name of the method to find.
   * @param argClasses The method arguments.
   * @return the found method or <code>null</code> if there is no match.
   */
  public static Method findPublicMethod(Class<?> beanClass, String methodName, Class<?>... argClasses) {
    try {
      return beanClass.getMethod(methodName, argClasses);
    }
    catch (NoSuchMethodException e) {
      return null;
    }
  }

  /**
   * Provides all fields (incl. private ones) within the given class and its
   * super-classes.
   * <p>
   * The fields of the super classes appear as first list items.
   *
   * @param inClass
   *          The class to analyze.
   * @return All found fields.
   */
  public static List<Field> getAllFields(Class<?> inClass) {
    List<Field> allFields = new ArrayList<Field>();
    List<Class<?>> classes = new ArrayList<Class<?>>();

    Class<?> c = inClass;
    while (c != null) {
      classes.add(c);
      c = c.getSuperclass();
    }

    for (int i = classes.size() - 1; i >= 0; --i) {
      allFields.addAll(Arrays.asList(classes.get(i).getDeclaredFields()));
    }

    return allFields;
  }

  public static List<Method> findMethods(Class<?> inClass, String namePattern) {
    List<Method> matches = new ArrayList<Method>();
    List<Class<?>> classes = new ArrayList<Class<?>>();

    Class<?> c = inClass;
    while (c != null) {
      classes.add(c);
      c = c.getSuperclass();
    }

    for (int i = classes.size() - 1; i >= 0; --i) {
      for (Method m : classes.get(i).getDeclaredMethods()) {
        if (m.getName().matches(namePattern)) {
          matches.add(m);
        }
      }
    }

    return matches;
  }

  /**
   * Finds a field by name within the given class and its super-classes.<br>
   * Provides private fields too.
   *
   * @param inClass
   *          The class to check for the field.
   * @param fieldName
   *          Name of the field to find.
   * @return The found field or <code>null</code> if not found.
   * @deprecated is buggy. will not be replaced.
   */
  @Deprecated
  public static Field findField(Class<?> inClass, String fieldName) {
    try {
      return inClass.getField(fieldName);
    } catch(NoSuchFieldException e) {
      // no direct catch to prevent too much control flow changes for now.
    }

    // FIXME oboede: Now the old buggy mechanism to preserve some buggy screen expressions for a while.
    // The following code should be removed asap.
    try {
      Field f = inClass.getDeclaredField(fieldName);
      if (f != null) {
        LOG.warn( "!!! Non public field accessed by path. Please make the field public or provide getter/setter.\n"+
                  "  The illegally accessed field: " + f);
      }
      return f;
    } catch (SecurityException e) {
      throw new RuntimeException("Security does not allow to access field '" + fieldName + "' of class " + inClass, e);
    } catch (NoSuchFieldException e) {
      // Ok. Not declared by the checked class.
    }
    // not found
    return null;
  }

  /**
   * Provides the bin-package directory the given class is located in.
   *
   * @param forClass
   *          The class to get the directory for.
   * @return The package directory.
   */
  public static File getClassDir(Class<?> forClass) {
    URL classFileUrl = forClass.getResource(forClass.getSimpleName() + ".class");
    File f = new File(classFileUrl.getFile());
    return f.getParentFile();
  }


  /**
   * Walks up the class hierarchy starting with the given class to find all
   * methods with a particular annotation. Methods that are declared deeper in
   * the class hierarchy will be returned first.
   *
   * @param type
   *          start point in the class hierarchy
   * @param annotation
   *          the annotation in question
   * @return all methods with the given annotation, an empty list if no
   *         annotated methods where found
   */
  public static List<Method> findAnnotatedMethodsBottomUp(final Class<?> type,
      final Class<? extends Annotation> annotation) {
    assert type != null;
    assert annotation != null;

    List<Method> methods = new ArrayList<Method>();
    Class<?> clazz = type;

    // walk up the class hierarchy
    while (clazz != Object.class) {
      List<Method> allMethods = new ArrayList<Method>(Arrays.asList(clazz.getDeclaredMethods()));
      for (Method method : allMethods) {
        if (method.isAnnotationPresent(annotation)) {
          methods.add(method);
        }
      }
      clazz = clazz.getSuperclass();
    }

    return methods;
  }

  /**
   * Walks down the class hierarchy from java.lang Object to the given class to
   * find all methods with a particular annotation. Methods that are declared
   * higher in the class hierarchy will be returned first.
   *
   * @param type
   *          finish point in the class hierarchy
   * @param annotation
   *          the annotation in question
   * @return all methods with the given annotation, an empty list if no
   *         annotated methods where found
   */
  public static List<Method> findAnnotatedMethodsTopDown(final Class<?> type,
      final Class<? extends Annotation> annotation) {
    List<Method> methods = findAnnotatedMethodsBottomUp(type, annotation);
    Collections.reverse(methods);
    return methods;
  }

  /**
   *
   */
  private ClassUtil() {
    super();
  }

}

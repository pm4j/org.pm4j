package org.pm4j.core.util.reflection;

import java.io.File;
import java.io.Serializable;
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

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.util.lang.CloneUtil;

/**
 * Some functions that make it easier to use of some reflection features.
 */
public class ClassUtil {

  private static final Class<?>[]  EMPTY_CLASS_ARRAY  = {};

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
  public static <T> T newInstance(Class<?> forClass) {
    Object result = null;

    try {
      Constructor<?> ctor = forClass.getConstructor(EMPTY_CLASS_ARRAY);

      if (ctor == null) {
        throw new IllegalStateException("Default constructor missing for class " + forClass);
      }

      result = ctor.newInstance(EMPTY_OBJECT_ARRAY);
    }
    catch (Exception e) {
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
    }
    catch (Exception e) {
      CheckedExceptionWrapper.throwAsRuntimeException(e);
    }

    return (T) result;
  }

  /**
   * Creates an instance of the given class using a matching constructor that
   * accepts the given argument. If such constructor does not exist, the default
   * constructor will be used.
   *
   * @param <T>
   *          The instance fieldClass that the calling code expects.
   * @param forClass
   *          The class to get an instance for.
   * @param args
   *          The arguments to pass to the constructor.
   * @return The created instance.
   * @throws IllegalStateException
   *           when the given class has no public default constructor.
   */
  @SuppressWarnings("unchecked")
  public static <T> T newInstanceWithOptionalArg(Class<?> forClass, Object... args) {
    Object result = null;

    try {
      Class<?>[] argClasses = new Class[args.length];
      for (int i = 0; i < args.length; ++i) {
        if (args[i] != null) {
          argClasses[i] = args[i].getClass();
        }
      }

      Constructor< ? > ctor = SunReflectionUtils.getConstructor(forClass, argClasses);

      if (ctor != null) {
        result = ctor.newInstance(args);
      }
      else {
        // no constructor for the given argument found.
        // try the default one:
        result = newInstance(forClass);
      }
    }
    catch (Exception e) {
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
    }
    catch (SecurityException e) {
      throw new CheckedExceptionWrapper(e);
    }
    catch (NoSuchMethodException e) {
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
  public static Constructor<Object> findConstructor(Class< ? > cls, Class< ? >... args) {
    Constructor<Object> constructor = null;

    Constructor<?>[] ctors = cls.getConstructors();
    for (int i = 0; i < ctors.length; i++) {
      Class< ? >[] ptypes = ctors[i].getParameterTypes();
      if (args.length == ptypes.length) {
        boolean doesMatch = true;
        for (int pidx = 0; pidx < args.length; ++pidx) {
          if (!args[pidx].isAssignableFrom(ptypes[pidx])) {
            doesMatch = false;
            break;
          }
        }

        if (doesMatch) {
          constructor = (Constructor<Object>) ctors[i];
        }
      }
    }

    return constructor;
  }

  /**
   * Searches the matching constructor of the given class
   * <code>cls</code> that has a matching argument set.
   * <p>
   * @see #findConstructor(Class, Class[]) for details.
   *
   * @param cls
   *          The class that might contain the matching constructor.
   * @param args
   *          The requested argument set.
   * @return The found constructor or <code>null</code>.
   * @throws IllegalArgumentException when there is no match.
   */
  public static Constructor<Object> getConstructor(Class< ? > cls, Class<?>... args) {
    Constructor<Object> c = ClassUtil.findConstructor(cls, args);

    if (c == null) {
      throw new IllegalArgumentException("No matching constructor for class '" + cls
          + "' found.\n"
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
      if ((! forbiddenGetterNames.contains(m.getName())) &&
          PrefixUtil.isGetter(m)) {
        try {
          Object getterResult = m.invoke(object, EMPTY_OBJECT_ARRAY);
          if (getterResult == fieldValue) {
            return m;
          }
        } catch (Exception e) {
          // ok. this candidate does not work...
          //
          // throw new ReflectionException("Unable to call getter '" + cls.getName() + "." + m.getName() + "'", e);
        }
      }
    }

    // not found
    return null;
  }

  /**
   * Searches a field by checking all public fields of the given object to hold the
   * given fieldValue reference.
   *
   * @param object
   *          The instance to find the value reference in.
   * @param fieldValue
   *          The value reference to find the holder field for.
   * @return The found field or <code>null</code> when there is no public
   *         field that provides the requested field value.
   */
  public static Field findPublicFieldByValueRef(Object object, Object fieldValue) {
    assert fieldValue != null;
    Class<?> cls = object.getClass();
    for (Field f : cls.getFields()) {
      try {
        Object valueOfF;
        try {
          valueOfF = f.get(object);
        }
        catch (IllegalAccessException e) {
          // TODO olaf: Workaround for a security exception when trying to get
          //           the value of a public final member. - Why shouldn't that be
          //           accessible via reflection?
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
    }
    else {
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
      if (PrefixUtil.isGetter(m)) {
        nonAttributeGetterNames.add(m.getName());
      }
    }
    return nonAttributeGetterNames;
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

    for (int i=classes.size()-1; i>=0; --i) {
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

    for (int i=classes.size()-1; i>=0; --i) {
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
   */
  public static Field findField(Class<?> inClass, String fieldName) {
    Class<?> c = inClass;
    while (c != null) {
      try {
        return inClass.getDeclaredField(fieldName);
      } catch (SecurityException e) {
        throw new PmRuntimeException("Security does not allow to access field '" +
            fieldName + "' of class " + c, e);
      } catch (NoSuchFieldException e) {
        // Ok. Not declared by the checked class.
      }
      c = c.getSuperclass();
    }
    // not found
    return null;
  }

  // TODO olaf: finds currently only annotations, placed in interfaces
  //            directly attached to the concrete class declaration.
  /**
   * Finds the generic type parameter of an interface of a given class.
   *
   * @param clsToAnalyze
   *          The class with a generic interface parameter.
   * @param annotatedType
   *          An interface that <code>clsToAnalyze</code> implements.
   * @return The first generic type parameter of the interface in the context of
   *         the given class.<br>
   *         <code>null</code> if the interface or the type parameter could not
   *         be found within the given class.
   */
//  public static Class<?> findFirstGenericParameterOfInterface(Class<?> clsToAnalyze, Class<?> genericInterfaceToCheck) {
//    Class<?> result = null;
//    // try to find the interface parameter within the class hierarchy:
//    Class<?> c = clsToAnalyze;
//    while (! Object.class.equals(c)) {
//      result = _findFirstGenericParameterOfInterface(c, genericInterfaceToCheck);
//      if (result != null) {
//        return result;
//      }
//      else {
//        // check the interfaces
//        for (Class<?> i : c.getInterfaces()) {
//          result = _findFirstGenericParameterOfInterface(i, genericInterfaceToCheck);
//          if (result != null) {
//            return result;
//          }
//        }
//
//        c = c.getSuperclass();
//      }
//    }
//
//
//
//    return result;
//  }
//
//  public static Class<?> _findFirstGenericParameterOfInterface(Class<?> clsToAnalyze, Class<?> genericInterfaceToCheck) {
//    Type pmIf = null;
//    for (Type t : clsToAnalyze.getGenericInterfaces()) {
//      if (t instanceof ParameterizedType) {
//        ParameterizedType pt = (ParameterizedType)t;
//        Type rt = pt.getRawType();
//        if (genericInterfaceToCheck == rt) {
////        if (rt instanceof Class<?>
////          && genericInterfaceToCheck.isAssignableFrom((Class<?>)rt)) {
//          pmIf = t;
//          break;
//        }
//      }
//    }
//
//    if (pmIf == null) {
//      return null;
//    }
//
//    Type[] typeArgs = ((ParameterizedType)pmIf).getActualTypeArguments();
//    if (typeArgs.length != 1) {
//      return null;
//    }
//
//    return (Class<?>) typeArgs[0];
//  }

  /**
   * Provides the bin-package directory the given class is located in.
   *
   * @param forClass The class to get the directory for.
   * @return The package directory.
   */
  public static File getClassDir(Class<?> forClass) {
    URL classFileUrl = forClass.getResource(forClass.getSimpleName()+".class");
    File f = new File(classFileUrl.getFile());
    return f.getParentFile();
  }

  @SuppressWarnings("unchecked")
  private static final Set<Class<?>> IMMUTABLE_CLASSES = new HashSet<Class<?>>(Arrays.asList(
      String.class,
      Integer.class, Long.class, Short.class, Double.class, Float.class,
      Class.class));

  /**
   * @param o The instance to test. May be <code>null</code>.
   * @return <code>true</code> if the given object is a formally correct
   *         argument for {@link #cloneOrSerialize(Object)}.
   */
  public static boolean canCloneOrSerialize(Object o) {
    return o == null ||
           o instanceof Cloneable ||
           o instanceof Serializable ||
           // FIXME: make PM used as naviscope params (confirm dialog PMs)
           //        cloneable!
           //        Simplest (incomplete) solution: clone just passes a reference to self...
           o instanceof PmObject;
  }

  /**
   * Generates a clone for the given object.<br>
   * If the object is {@link Cloneable}, its <code>clone</code> method will be
   * called.<br>
   * If the object is not {@link Cloneable} but {@link Serializable}, a clone
   * will be created using the serialization and de-serialization.
   *
   * @param ori
   *          The object to copy.
   * @return The copy.
   * @throws IllegalArgumentException
   *           if the given object can't be copied because it is not cloneable
   *           or serializable.
   */
  @SuppressWarnings("unchecked")
  public static <T> T cloneOrSerialize(T ori) {
    if (ori == null) {
      return null;
    }

    Class<?> objClass = ori.getClass();

    if (IMMUTABLE_CLASSES.contains(objClass)) {
      return ori;
    }

    Object clone = null;

    if (ori instanceof Cloneable) {
      clone = CloneUtil.clone((Cloneable)ori);
    }
    else if (ori instanceof Serializable) {
      clone = SerializationUtils.clone((Serializable)ori);
    }
    else if (ori instanceof PmObject) {
      // Exception for PMs: If they are not marked as cloneable, they will be used
      // as shared instances.
      // FIXME olaf: this code is very PM logic specific. Find a way to express it not PM-specific
      //             in a technology utility.
      // alternative: Simply don't clone not cloneable stuff.
     	//              Disadvantage: Provides tricky error scenarios with unintended shared instances...
      clone = ori;
    }
    else {
      throw new IllegalArgumentException("Class '" + objClass.getName() + "' does neither implement Cloneable nor Serializable.");
    }

    return (T) clone;
  }

  /**
   *
   */
  private ClassUtil() {
    super();
  }

}

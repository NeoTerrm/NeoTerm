package io.neoterm.framework.reflection;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Make reflections easier and elegant.
 *
 * @author kiva
 */
public class Reflect {
  private final Object mObject;
  private final boolean isClass;

  private Reflect(Class<?> type) {
    this.mObject = type;
    this.isClass = true;
  }

  private Reflect(Object object) {
    this.mObject = object;
    this.isClass = false;
  }

  /**
   * Create reflector from class name.
   *
   * @param name Full class name
   * @return Reflector
   * @throws ReflectionException If any error occurs
   * @see #on(Class)
   */
  public static Reflect on(String name) throws ReflectionException {
    return on(forName(name));
  }

  /**
   * Create reflector from class name using given class loader.
   *
   * @param name        Full class name
   * @param classLoader Given class loader
   * @return Reflector
   * @throws ReflectionException If any error occurs
   * @see #on(Class)
   */
  public static Reflect on(String name, ClassLoader classLoader) throws ReflectionException {
    return on(forName(name, classLoader));
  }

  /**
   * Create reflector from given class type.
   * Helpful especially when you want to access static fields.
   *
   * @param clazz Given class type
   * @return Reflector
   */
  public static Reflect on(Class<?> clazz) {
    return new Reflect(clazz);
  }

  /**
   * Wrap an object and return its reflector.<p>
   * Helpful especially when you want to access instance fields and methods on any {@link Object}
   *
   * @param object The object to be wrapped
   * @return Reflector
   */
  public static Reflect on(Object object) {
    return new Reflect(object);
  }

  private static Reflect on(Method method, Object receiver, Object... args) throws ReflectionException {
    try {
      makeAccessible(method);

      if (method.getReturnType() == void.class) {
        method.invoke(receiver, args);
        return on(receiver);
      } else {
        return on(method.invoke(receiver, args));
      }
    } catch (Exception e) {
      throw new ReflectionException(e);
    }
  }

  /**
   * Make an {@link AccessibleObject} accessible.
   *
   * @param accessible
   * @param <T>
   * @return
   */
  public static <T extends AccessibleObject> T makeAccessible(T accessible) {
    if (accessible == null) {
      return null;
    }

    if (accessible instanceof Member) {
      Member member = (Member) accessible;

      if (Modifier.isPublic(member.getModifiers()) &&
        Modifier.isPublic(member.getDeclaringClass().getModifiers())) {

        return accessible;
      }
    }

    if (!accessible.isAccessible()) {
      accessible.setAccessible(true);
    }

    return accessible;
  }

  private static String property(String string) {
    int length = string.length();

    if (length == 0) {
      return "";
    } else if (length == 1) {
      return string.toLowerCase();
    } else {
      return string.substring(0, 1).toLowerCase() + string.substring(1);
    }
  }

  private static Reflect on(Constructor<?> constructor, Object... args) throws ReflectionException {
    try {
      return on(makeAccessible(constructor).newInstance(args));
    } catch (Exception e) {
      throw new ReflectionException(e);
    }
  }

  /**
   * If we are wrapping another reflector, get its real object.
   */
  private static Object unwrap(Object object) {
    if (object instanceof Reflect) {
      return ((Reflect) object).get();
    }

    return object;
  }

  /**
   * Convert object arrays into elements' class type arrays.
   * If encountered {@code null}, use {@link NullPointer}'s class type instead.
   *
   * @see Object#getClass()
   */
  private static Class<?>[] convertTypes(Object... values) {
    if (values == null) {
      return new Class[0];
    }

    Class<?>[] result = new Class[values.length];

    for (int i = 0; i < values.length; i++) {
      Object value = values[i];
      result[i] = value == null ? NullPointer.class : value.getClass();
    }

    return result;
  }

  /**
   * Get a class type of a class, which may cause its static-initialization
   *
   * @see Class#forName(String)
   */
  private static Class<?> forName(String name) throws ReflectionException {
    try {
      return Class.forName(name);
    } catch (Exception e) {
      throw new ReflectionException(e);
    }
  }

  private static Class<?> forName(String name, ClassLoader classLoader) throws ReflectionException {
    try {
      return Class.forName(name, true, classLoader);
    } catch (Exception e) {
      throw new ReflectionException(e);
    }
  }

  /**
   * Wrap primitive class types into object class types.
   *
   * @param type Class type that may be primitive class type
   * @return Wrapped class type
   */
  private static Class<?> wrapClassType(Class<?> type) {
    if (type == null) {
      return null;
    } else if (type.isPrimitive()) {
      if (boolean.class == type) {
        return Boolean.class;
      } else if (int.class == type) {
        return Integer.class;
      } else if (long.class == type) {
        return Long.class;
      } else if (short.class == type) {
        return Short.class;
      } else if (byte.class == type) {
        return Byte.class;
      } else if (double.class == type) {
        return Double.class;
      } else if (float.class == type) {
        return Float.class;
      } else if (char.class == type) {
        return Character.class;
      } else if (void.class == type) {
        return Void.class;
      }
    }

    return type;
  }

  /**
   * Get the real object that reflector operates.
   *
   * @param <T> The type of the real object.
   * @return The real object.
   */
  @SuppressWarnings("unchecked")
  public <T> T get() {
    return (T) mObject;
  }

  /**
   * Set a field to given value.
   *
   * @param name  Field name
   * @param value New value
   * @return Reflector
   * @throws ReflectionException If any error occurs
   */
  public Reflect set(String name, Object value) throws ReflectionException {
    try {
      Field field = lookupField(name);
      field.setAccessible(true);
      field.set(mObject, unwrap(value));
      return this;
    } catch (Exception e) {
      throw new ReflectionException(e);
    }
  }

  /**
   * Get the value of given field
   *
   * @param name Field name
   * @param <T>  The type of value
   * @return Value
   * @throws ReflectionException If any error occurs
   */
  public <T> T get(String name) throws ReflectionException {
    return field(name).get();
  }

  /**
   * Get field by name.
   *
   * @param name Field name
   * @return {@link Field}
   * @throws ReflectionException If any error occurs
   */
  public Reflect field(String name) throws ReflectionException {
    try {
      Field field = lookupField(name);
      return on(field.get(mObject));
    } catch (Exception e) {
      throw new ReflectionException(e);
    }
  }

  private Field lookupField(String name) throws ReflectionException {
    Class<?> type = type();

    // 先尝试取得公有字段
    try {
      return type.getField(name);
    }

    //此时尝试非公有字段
    catch (NoSuchFieldException e) {
      do {
        try {
          return makeAccessible(type.getDeclaredField(name));
        } catch (NoSuchFieldException ignore) {
        }

        type = type.getSuperclass();
      }
      while (type != null);

      throw new ReflectionException(e);
    }
  }

  /**
   * Load all fields into a map, the key is field name and the value is its reflector.
   *
   * @return Map to all fields.
   */
  public Map<String, Reflect> fields() {
    Map<String, Reflect> result = new LinkedHashMap<String, Reflect>();
    Class<?> type = type();

    do {
      for (Field field : type.getDeclaredFields()) {
        if (!isClass ^ Modifier.isStatic(field.getModifiers())) {
          String name = field.getName();

          if (!result.containsKey(name))
            result.put(name, field(name));
        }
      }

      type = type.getSuperclass();
    }
    while (type != null);

    return result;
  }

  /**
   * Call a method by name without parameters.
   *
   * @param name Method name
   * @return Reflector to the return value of the method
   * @throws ReflectionException If any error occurs
   */
  public Reflect call(String name) throws ReflectionException {
    return call(name, new Object[0]);
  }

  /**
   * Call a method by name and parameters.
   *
   * @param name Method name
   * @param args Parameters
   * @return Reflector to the return value of the method
   * @throws ReflectionException If any error occurs
   */
  public Reflect call(String name, Object... args) throws ReflectionException {
    Class<?>[] types = convertTypes(args);

    try {
      Method method = exactMethod(name, types);
      return on(method, mObject, args);
    } catch (NoSuchMethodException e) {
      try {
        Method method = lookupSimilarMethod(name, types);
        return on(method, mObject, args);
      } catch (NoSuchMethodException e1) {
        throw new ReflectionException(e1);
      }
    }
  }

  private Method exactMethod(String name, Class<?>[] types) throws NoSuchMethodException {
    Class<?> type = type();

    try {
      return type.getMethod(name, types);
    } catch (NoSuchMethodException e) {
      do {
        try {
          return type.getDeclaredMethod(name, types);
        } catch (NoSuchMethodException ignore) {
        }

        type = type.getSuperclass();
      }
      while (type != null);

      throw new NoSuchMethodException();
    }
  }

  /**
   * Find a method that is similar to the wanted one.
   */
  private Method lookupSimilarMethod(String name, Class<?>[] types) throws NoSuchMethodException {
    Class<?> type = type();

    for (Method method : type.getMethods()) {
      if (isSignatureSimilar(method, name, types)) {
        return method;
      }
    }

    do {
      for (Method method : type.getDeclaredMethods()) {
        if (isSignatureSimilar(method, name, types)) {
          return method;
        }
      }

      type = type.getSuperclass();
    }
    while (type != null);

    throw new NoSuchMethodException("No similar method " + name + " with params " + Arrays.toString(types) + " could be found on type " + type() + ".");
  }

  private boolean isSignatureSimilar(Method possiblyMatchingMethod,
                                     String wantedMethodName,
                                     Class<?>[] wantedParamTypes) {
    return possiblyMatchingMethod.getName().equals(wantedMethodName)
      && match(possiblyMatchingMethod.getParameterTypes(), wantedParamTypes);
  }

  /**
   * Create an instance using its default constructor.
   *
   * @return Reflector to the return value of the method
   * @throws ReflectionException If any error occurs
   */
  public Reflect create() throws ReflectionException {
    return create(new Object[0]);
  }

  /**
   * Create an instance by parameters.
   *
   * @param args Parameters
   * @return Reflector to the return value of the method
   * @throws ReflectionException If any error occurs
   */
  public Reflect create(Object... args) throws ReflectionException {
    Class<?>[] types = convertTypes(args);


    try {
      Constructor<?> constructor = type().getDeclaredConstructor(types);
      return on(constructor, args);
    } catch (NoSuchMethodException e) {
      for (Constructor<?> constructor : type().getDeclaredConstructors()) {
        if (match(constructor.getParameterTypes(), types)) {
          return on(constructor, args);
        }
      }

      throw new ReflectionException(e);
    }
  }

  /**
   * Create a dynamic proxy based on the given type.
   * If we are maintaining a Map and error occurs when calling methods,
   * we will return value from Map as return value.
   * Helpful especially when creating default data handlers.
   *
   * @param proxyType The type to be proxy-ed
   * @return Proxy object
   */
  @SuppressWarnings("unchecked")
  public <P> P as(Class<P> proxyType) {
    final boolean isMap = (mObject instanceof Map);
    final InvocationHandler handler = (proxy, method, args) -> {
      String name = method.getName();
      try {
        return on(mObject).call(name, args).get();
      } catch (ReflectionException e) {
        if (isMap) {
          Map<String, Object> map = (Map<String, Object>) mObject;
          int length = (args == null ? 0 : args.length);

          // Pay special attention to those getters and setters
          if (length == 0 && name.startsWith("get")) {
            return map.get(property(name.substring(3)));
          } else if (length == 0 && name.startsWith("is")) {
            return map.get(property(name.substring(2)));
          } else if (length == 1 && name.startsWith("set")) {
            map.put(property(name.substring(3)), args[0]);
            return null;
          }
        }

        throw e;
      }
    };

    return (P) Proxy.newProxyInstance(proxyType.getClassLoader(),
      new Class[]{proxyType}, handler);
  }

  /**
   * Check whether types matches to avoid {@link ClassCastException} when calling a method.
   * If encountered primitive type, convert to object type first.
   */
  private boolean match(Class<?>[] declaredTypes, Class<?>[] actualTypes) {
    if (declaredTypes.length == actualTypes.length) {
      for (int i = 0; i < actualTypes.length; i++) {
        // nulls are acceptable on any occasions
        if (actualTypes[i] == NullPointer.class) {
          continue;
        }

        if (wrapClassType(declaredTypes[i]).isAssignableFrom(wrapClassType(actualTypes[i]))) {
          continue;
        }
        return false;
      }

      return true;
    } else {
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return mObject.hashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Reflect) {
      return mObject.equals(((Reflect) obj).get());
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return mObject.toString();
  }

  /**
   * Get the class type of the real object that reflector operates.
   *
   * @see Object#getClass()
   */
  public Class<?> type() {
    if (isClass) {
      return (Class<?>) mObject;
    } else {
      return mObject.getClass();
    }
  }
}

/*
 * Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilities for working with key/value data based on the {@link Key} annotation.
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
public class Data {

  // NOTE: create new instances to avoid cache, e.g. new String()

  /** The single instance of the magic null object for a {@link Boolean}. */
  @SuppressWarnings("deprecation")
  public static final Boolean NULL_BOOLEAN = new Boolean(true);

  /** The single instance of the magic null object for a {@link String}. */
  @SuppressWarnings("deprecation")
  public static final String NULL_STRING = new String();

  /** The single instance of the magic null object for a {@link Character}. */
  @SuppressWarnings("deprecation")
  public static final Character NULL_CHARACTER = new Character((char) 0);

  /** The single instance of the magic null object for a {@link Byte}. */
  @SuppressWarnings("deprecation")
  public static final Byte NULL_BYTE = new Byte((byte) 0);

  /** The single instance of the magic null object for a {@link Short}. */
  @SuppressWarnings("deprecation")
  public static final Short NULL_SHORT = new Short((short) 0);

  /** The single instance of the magic null object for a {@link Integer}. */
  @SuppressWarnings("deprecation")
  public static final Integer NULL_INTEGER = new Integer(0);

  /** The single instance of the magic null object for a {@link Float}. */
  @SuppressWarnings("deprecation")
  public static final Float NULL_FLOAT = new Float(0);

  /** The single instance of the magic null object for a {@link Long}. */
  @SuppressWarnings("deprecation")
  public static final Long NULL_LONG = new Long(0);

  /** The single instance of the magic null object for a {@link Double}. */
  @SuppressWarnings("deprecation")
  public static final Double NULL_DOUBLE = new Double(0);

  /** The single instance of the magic null object for a {@link BigInteger}. */
  @SuppressWarnings("deprecation")
  public static final BigInteger NULL_BIG_INTEGER = new BigInteger("0");

  /** The single instance of the magic null object for a {@link BigDecimal}. */
  @SuppressWarnings("deprecation")
  public static final BigDecimal NULL_BIG_DECIMAL = new BigDecimal("0");

  /** The single instance of the magic null object for a {@link DateTime}. */
  public static final DateTime NULL_DATE_TIME = new DateTime(0);

  /** Cache of the magic null object for the given Java class. */
  private static final ConcurrentHashMap<Class<?>, Object> NULL_CACHE =
      new ConcurrentHashMap<Class<?>, Object>();

  static {
    // special cases for some primitives
    NULL_CACHE.put(Boolean.class, NULL_BOOLEAN);
    NULL_CACHE.put(String.class, NULL_STRING);
    NULL_CACHE.put(Character.class, NULL_CHARACTER);
    NULL_CACHE.put(Byte.class, NULL_BYTE);
    NULL_CACHE.put(Short.class, NULL_SHORT);
    NULL_CACHE.put(Integer.class, NULL_INTEGER);
    NULL_CACHE.put(Float.class, NULL_FLOAT);
    NULL_CACHE.put(Long.class, NULL_LONG);
    NULL_CACHE.put(Double.class, NULL_DOUBLE);
    NULL_CACHE.put(BigInteger.class, NULL_BIG_INTEGER);
    NULL_CACHE.put(BigDecimal.class, NULL_BIG_DECIMAL);
    NULL_CACHE.put(DateTime.class, NULL_DATE_TIME);
  }

  /**
   * Returns the single instance of the magic object that represents the "null" value for the given
   * Java class (including array or enum).
   *
   * @param objClass class of the object needed
   * @return magic object instance that represents the "null" value (not Java {@code null})
   * @throws IllegalArgumentException if unable to create a new instance
   */
  public static <T> T nullOf(Class<T> objClass) {
    // ConcurrentMap.computeIfAbsent is explicitly NOT used in the following logic. The
    // ConcurrentHashMap implementation of that method BLOCKS if the mappingFunction triggers
    // modification of the map which createNullInstance can do depending on the state of class
    // loading.
    Object result = NULL_CACHE.get(objClass);
    if (result == null) {
      // If nullOf is called concurrently for the same class createNullInstance may be executed
      // multiple times. However putIfAbsent ensures that no matter what the concurrent access
      // pattern looks like callers always get a singleton instance returned. Since
      // createNullInstance has no side-effects beyond triggering class loading this multiple-call
      // pattern is safe.
      Object newValue = createNullInstance(objClass);
      result = NULL_CACHE.putIfAbsent(objClass, newValue);
      if (result == null) {
        result = newValue;
      }
    }
    @SuppressWarnings("unchecked")
    T tResult = (T) result;
    return tResult;
  }

  private static Object createNullInstance(Class<?> objClass) {
    if (objClass.isArray()) {
      // arrays are special because we need to compute both the dimension and component type
      int dims = 0;
      Class<?> componentType = objClass;
      do {
        componentType = componentType.getComponentType();
        dims++;
      } while (componentType.isArray());
      return Array.newInstance(componentType, new int[dims]);
    }
    if (objClass.isEnum()) {
      // enum requires look for constant with @NullValue
      FieldInfo fieldInfo = ClassInfo.of(objClass).getFieldInfo(null);
      Preconditions.checkNotNull(
          fieldInfo, "enum missing constant with @NullValue annotation: %s", objClass);
      @SuppressWarnings({"unchecked", "rawtypes"})
      Enum e = fieldInfo.<Enum>enumValue();
      return e;
    }
    // other classes are simpler
    return Types.newInstance(objClass);
  }

  /**
   * Returns whether the given object is the magic object that represents the null value of its
   * class.
   *
   * @param object object or {@code null}
   * @return whether it is the magic null value or {@code false} for {@code null} input
   */
  public static boolean isNull(Object object) {
    // don't call nullOf because will throw IllegalArgumentException if cannot create instance
    return object != null && object == NULL_CACHE.get(object.getClass());
  }

  /**
   * Returns the map to use for the given data that is treated as a map from string key to some
   * value.
   *
   * <p>If the input is {@code null}, it returns an empty map. If the input is a map, it simply
   * returns the input. Otherwise, it will create a map view using reflection that is backed by the
   * object, so that any changes to the map will be reflected on the object. The map keys of that
   * map view are based on the {@link Key} annotation, and null is not a possible map value,
   * although the magic null instance is possible (see {@link #nullOf(Class)} and {@link
   * #isNull(Object)}). Iteration order of the data keys is based on the sorted (ascending) key
   * names of the declared fields. Note that since the map view is backed by the object, and that
   * the object may change, many methods in the map view must recompute the field values using
   * reflection, for example {@link Map#size()} must check the number of non-null fields.
   *
   * @param data any key value data, represented by an object or a map, or {@code null}
   * @return key/value map to use
   */
  public static Map<String, Object> mapOf(Object data) {
    if (data == null || isNull(data)) {
      return Collections.emptyMap();
    }
    if (data instanceof Map<?, ?>) {
      @SuppressWarnings("unchecked")
      Map<String, Object> result = (Map<String, Object>) data;
      return result;
    }
    Map<String, Object> result = new DataMap(data, false);
    return result;
  }

  /**
   * Returns a deep clone of the given key/value data, such that the result is a completely
   * independent copy.
   *
   * <p>This should not be used directly in the implementation of {@code Object.clone()}. Instead
   * use {@link #deepCopy(Object, Object)} for that purpose.
   *
   * <p>Final fields cannot be changed and therefore their value won't be copied.
   *
   * @param data key/value data object or map to clone or {@code null} for a {@code null} return
   *     value
   * @return deep clone or {@code null} for {@code null} input
   */
  @SuppressWarnings("unchecked")
  public static <T> T clone(T data) {
    // don't need to clone primitive
    if (data == null || Data.isPrimitive(data.getClass())) {
      return data;
    }
    if (data instanceof GenericData) {
      return (T) ((GenericData) data).clone();
    }
    T copy;
    Class<?> dataClass = data.getClass();
    if (dataClass.isArray()) {
      copy = (T) Array.newInstance(dataClass.getComponentType(), Array.getLength(data));
    } else if (data instanceof ArrayMap<?, ?>) {
      copy = (T) ((ArrayMap<?, ?>) data).clone();
    } else if ("java.util.Arrays$ArrayList".equals(dataClass.getName())) {
      // Arrays$ArrayList does not have a zero-arg constructor, so it has to handled specially.
      // Arrays.asList(x).toArray() may or may not have the same runtime type as x.
      // https://bugs.openjdk.java.net/browse/JDK-6260652
      Object[] arrayCopy = ((List<?>) data).toArray();
      deepCopy(arrayCopy, arrayCopy);
      copy = (T) Arrays.asList(arrayCopy);
      return copy;
    } else {
      copy = (T) Types.newInstance(dataClass);
    }
    deepCopy(data, copy);
    return copy;
  }

  /**
   * Makes a deep copy of the given source object into the destination object that is assumed to be
   * constructed using {@code Object.clone()}.
   *
   * <p>Example usage of this method in {@code Object.clone()}:
   *
   * <pre>
   * &#64;Override
   * public MyObject clone() {
   * try {
   * &#64;SuppressWarnings("unchecked")
   * MyObject result = (MyObject) super.clone();
   * Data.deepCopy(this, result);
   * return result;
   * } catch (CloneNotSupportedException e) {
   * throw new IllegalStateException(e);
   * }
   * }
   * </pre>
   *
   * <p>Final fields cannot be changed and therefore their value won't be copied.
   *
   * @param src source object
   * @param dest destination object of identical type as source object, and any contained arrays
   *     must be the same length
   */
  public static void deepCopy(Object src, Object dest) {
    Class<?> srcClass = src.getClass();
    Preconditions.checkArgument(srcClass == dest.getClass());
    if (srcClass.isArray()) {
      // clone array
      Preconditions.checkArgument(Array.getLength(src) == Array.getLength(dest));
      int index = 0;
      for (Object value : Types.iterableOf(src)) {
        Array.set(dest, index++, clone(value));
      }
    } else if (Collection.class.isAssignableFrom(srcClass)) {
      // clone collection
      @SuppressWarnings("unchecked")
      Collection<Object> srcCollection = (Collection<Object>) src;
      if (ArrayList.class.isAssignableFrom(srcClass)) {
        @SuppressWarnings("unchecked")
        ArrayList<Object> destArrayList = (ArrayList<Object>) dest;
        destArrayList.ensureCapacity(srcCollection.size());
      }
      @SuppressWarnings("unchecked")
      Collection<Object> destCollection = (Collection<Object>) dest;
      for (Object srcValue : srcCollection) {
        destCollection.add(clone(srcValue));
      }
    } else {
      // clone generic data or a non-map Object
      boolean isGenericData = GenericData.class.isAssignableFrom(srcClass);
      if (isGenericData || !Map.class.isAssignableFrom(srcClass)) {
        ClassInfo classInfo =
            isGenericData ? ((GenericData) src).classInfo : ClassInfo.of(srcClass);
        for (String fieldName : classInfo.names) {
          FieldInfo fieldInfo = classInfo.getFieldInfo(fieldName);
          // skip final fields
          if (!fieldInfo.isFinal()) {
            // generic data already has primitive types copied by clone()
            if (!isGenericData || !fieldInfo.isPrimitive()) {
              Object srcValue = fieldInfo.getValue(src);
              if (srcValue != null) {
                fieldInfo.setValue(dest, clone(srcValue));
              }
            }
          }
        }
      } else if (ArrayMap.class.isAssignableFrom(srcClass)) {
        // clone array map
        @SuppressWarnings("unchecked")
        ArrayMap<Object, Object> destMap = (ArrayMap<Object, Object>) dest;
        @SuppressWarnings("unchecked")
        ArrayMap<Object, Object> srcMap = (ArrayMap<Object, Object>) src;
        int size = srcMap.size();
        for (int i = 0; i < size; i++) {
          Object srcValue = srcMap.getValue(i);
          destMap.set(i, clone(srcValue));
        }
      } else {
        // clone map
        @SuppressWarnings("unchecked")
        Map<String, Object> destMap = (Map<String, Object>) dest;
        @SuppressWarnings("unchecked")
        Map<String, Object> srcMap = (Map<String, Object>) src;
        for (Map.Entry<String, Object> srcEntry : srcMap.entrySet()) {
          destMap.put(srcEntry.getKey(), clone(srcEntry.getValue()));
        }
      }
    }
  }

  /**
   * Returns whether the given type is one of the supported primitive classes like number and
   * date/time, or is a wildcard of one.
   *
   * <p>A primitive class is any class for whom {@link Class#isPrimitive()} is true, as well as any
   * classes of type: {@link Character}, {@link String}, {@link Integer}, {@link Long}, {@link
   * Short}, {@link Byte}, {@link Float}, {@link Double}, {@link BigInteger}, {@link BigDecimal},
   * {@link Boolean}, and {@link DateTime}.
   *
   * @param type type or {@code null} for {@code false} result
   * @return whether it is a primitive
   */
  public static boolean isPrimitive(Type type) {
    // TODO(yanivi): support java.net.URI as primitive type?
    if (type instanceof WildcardType) {
      type = Types.getBound((WildcardType) type);
    }
    if (!(type instanceof Class<?>)) {
      return false;
    }
    Class<?> typeClass = (Class<?>) type;
    return typeClass.isPrimitive()
        || typeClass == Character.class
        || typeClass == String.class
        || typeClass == Integer.class
        || typeClass == Long.class
        || typeClass == Short.class
        || typeClass == Byte.class
        || typeClass == Float.class
        || typeClass == Double.class
        || typeClass == BigInteger.class
        || typeClass == BigDecimal.class
        || typeClass == DateTime.class
        || typeClass == Boolean.class;
  }

  /**
   * Returns whether to given value is {@code null} or its class is primitive as defined by {@link
   * Data#isPrimitive(Type)}.
   */
  public static boolean isValueOfPrimitiveType(Object fieldValue) {
    return fieldValue == null || Data.isPrimitive(fieldValue.getClass());
  }

  /**
   * Parses the given string value based on the given primitive type.
   *
   * <p>Types are parsed as follows:
   *
   * <ul>
   *   <li>{@link Void}: null
   *   <li>{@code null} or is assignable from {@link String} (like {@link Object}): no parsing
   *   <li>{@code char} or {@link Character}: {@link String#charAt(int) String.charAt}(0) (requires
   *       length to be exactly 1)
   *   <li>{@code boolean} or {@link Boolean}: {@link Boolean#valueOf(String)}
   *   <li>{@code byte} or {@link Byte}: {@link Byte#valueOf(String)}
   *   <li>{@code short} or {@link Short}: {@link Short#valueOf(String)}
   *   <li>{@code int} or {@link Integer}: {@link Integer#valueOf(String)}
   *   <li>{@code long} or {@link Long}: {@link Long#valueOf(String)}
   *   <li>{@code float} or {@link Float}: {@link Float#valueOf(String)}
   *   <li>{@code double} or {@link Double}: {@link Double#valueOf(String)}
   *   <li>{@link BigInteger}: {@link BigInteger#BigInteger(String) BigInteger(String)}
   *   <li>{@link BigDecimal}: {@link BigDecimal#BigDecimal(String) BigDecimal(String)}
   *   <li>{@link DateTime}: {@link DateTime#parseRfc3339(String)}
   * </ul>
   *
   * <p>Note that this may not be the right behavior for some use cases.
   *
   * @param type primitive type or {@code null} to parse as a string
   * @param stringValue string value to parse or {@code null} for {@code null} result
   * @return parsed object or {@code null} for {@code null} input
   * @throws IllegalArgumentException if the given class is not a primitive class
   */
  public static Object parsePrimitiveValue(Type type, String stringValue) {
    Class<?> primitiveClass = type instanceof Class<?> ? (Class<?>) type : null;
    if (type == null || primitiveClass != null) {
      if (primitiveClass == Void.class) {
        return null;
      }
      if (stringValue == null
          || primitiveClass == null
          || primitiveClass.isAssignableFrom(String.class)) {
        return stringValue;
      }
      if (primitiveClass == Character.class || primitiveClass == char.class) {
        if (stringValue.length() != 1) {
          throw new IllegalArgumentException(
              "expected type Character/char but got " + primitiveClass);
        }
        return stringValue.charAt(0);
      }
      if (primitiveClass == Boolean.class || primitiveClass == boolean.class) {
        return Boolean.valueOf(stringValue);
      }
      if (primitiveClass == Byte.class || primitiveClass == byte.class) {
        return Byte.valueOf(stringValue);
      }
      if (primitiveClass == Short.class || primitiveClass == short.class) {
        return Short.valueOf(stringValue);
      }
      if (primitiveClass == Integer.class || primitiveClass == int.class) {
        return Integer.valueOf(stringValue);
      }
      if (primitiveClass == Long.class || primitiveClass == long.class) {
        return Long.valueOf(stringValue);
      }
      if (primitiveClass == Float.class || primitiveClass == float.class) {
        return Float.valueOf(stringValue);
      }
      if (primitiveClass == Double.class || primitiveClass == double.class) {
        return Double.valueOf(stringValue);
      }
      if (primitiveClass == DateTime.class) {
        return DateTime.parseRfc3339(stringValue);
      }
      if (primitiveClass == BigInteger.class) {
        return new BigInteger(stringValue);
      }
      if (primitiveClass == BigDecimal.class) {
        return new BigDecimal(stringValue);
      }
      if (primitiveClass.isEnum()) {
        if (!ClassInfo.of(primitiveClass).names.contains(stringValue)) {
          throw new IllegalArgumentException(
              String.format("given enum name %s not part of " + "enumeration", stringValue));
        }
        @SuppressWarnings({"unchecked", "rawtypes"})
        Enum result = ClassInfo.of(primitiveClass).getFieldInfo(stringValue).<Enum>enumValue();
        return result;
      }
    }
    throw new IllegalArgumentException("expected primitive class, but got: " + type);
  }

  /**
   * Returns a new collection instance for the given type.
   *
   * <p>Creates a new collection instance specified for the first input collection class that
   * matches as follows:
   *
   * <ul>
   *   <li>{@code null} or an array or assignable from {@link ArrayList} (like {@link List} or
   *       {@link Collection} or {@link Object}): returns an {@link ArrayList}
   *   <li>assignable from {@link HashSet}: returns a {@link HashSet}
   *   <li>assignable from {@link TreeSet}: returns a {@link TreeSet}
   *   <li>else: calls {@link Types#newInstance(Class)}
   * </ul>
   *
   * @param type type or {@code null} for {@link ArrayList}.
   * @return new collection instance
   * @throws ClassCastException if result is does not extend {@link Collection}
   */
  public static Collection<Object> newCollectionInstance(Type type) {
    if (type instanceof WildcardType) {
      type = Types.getBound((WildcardType) type);
    }
    if (type instanceof ParameterizedType) {
      type = ((ParameterizedType) type).getRawType();
    }
    Class<?> collectionClass = type instanceof Class<?> ? (Class<?>) type : null;
    if (type == null
        || type instanceof GenericArrayType
        || collectionClass != null
            && (collectionClass.isArray() || collectionClass.isAssignableFrom(ArrayList.class))) {
      return new ArrayList<Object>();
    }
    if (collectionClass == null) {
      throw new IllegalArgumentException("unable to create new instance of type: " + type);
    }
    if (collectionClass.isAssignableFrom(HashSet.class)) {
      return new HashSet<Object>();
    }
    if (collectionClass.isAssignableFrom(TreeSet.class)) {
      return new TreeSet<Object>();
    }
    @SuppressWarnings("unchecked")
    Collection<Object> result = (Collection<Object>) Types.newInstance(collectionClass);
    return result;
  }

  /**
   * Returns a new instance of a map based on the given field class.
   *
   * <p>Creates a new map instance specified for the first input map class that matches as follows:
   *
   * <ul>
   *   <li>{@code null} or assignable from {@link ArrayMap} (like {@link Map} or {@link Object}):
   *       returns an {@link ArrayMap}
   *   <li>assignable from {@link TreeMap} (like {@link SortedMap}): returns a {@link TreeMap}
   *   <li>else: calls {@link Types#newInstance(Class)}
   * </ul>
   *
   * @param mapClass field class
   * @throws ClassCastException if result is does not extend {@link Map}
   */
  public static Map<String, Object> newMapInstance(Class<?> mapClass) {
    if (mapClass == null || mapClass.isAssignableFrom(ArrayMap.class)) {
      return ArrayMap.create();
    }
    if (mapClass.isAssignableFrom(TreeMap.class)) {
      return new TreeMap<String, Object>();
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) Types.newInstance(mapClass);
    return result;
  }

  /**
   * Aggressively resolves the given type in such a way that the resolved type is not a wildcard
   * type or a type variable, returning {@code Object.class} if the type variable cannot be
   * resolved.
   *
   * @param context context list, ordering from least specific to most specific type context, for
   *     example container class and then its field
   * @param type type or {@code null} for {@code null} result
   * @return resolved type (which may be class, parameterized type, or generic array type, but not
   *     wildcard type or type variable) or {@code null} for {@code null} input
   */
  public static Type resolveWildcardTypeOrTypeVariable(List<Type> context, Type type) {
    // first deal with a wildcard, e.g. ? extends Number
    if (type instanceof WildcardType) {
      type = Types.getBound((WildcardType) type);
    }
    // next deal with a type variable T
    while (type instanceof TypeVariable<?>) {
      // resolve the type variable
      Type resolved = Types.resolveTypeVariable(context, (TypeVariable<?>) type);
      if (resolved != null) {
        type = resolved;
      }
      // if unable to fully resolve the type variable, use its bounds, e.g. T extends Number
      if (type instanceof TypeVariable<?>) {
        type = ((TypeVariable<?>) type).getBounds()[0];
      }
      // loop in case T extends U and U is also a type variable
    }
    return type;
  }
}

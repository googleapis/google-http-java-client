/*
 * Copyright (c) 2010 Google Inc.
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Computes class information to determine data key name/value pairs associated with the class.
 *
 * <p>Implementation is thread-safe.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class ClassInfo {

  /** Class information cache, with case-sensitive field names. */
  private static final ConcurrentMap<Class<?>, ClassInfo> CACHE =
      new ConcurrentHashMap<Class<?>, ClassInfo>();

  /** Class information cache, with case-insensitive fields names. */
  private static final ConcurrentMap<Class<?>, ClassInfo> CACHE_IGNORE_CASE =
      new ConcurrentHashMap<Class<?>, ClassInfo>();

  /** Class. */
  private final Class<?> clazz;

  /** Whether field names are case sensitive. */
  private final boolean ignoreCase;

  /** Map from {@link FieldInfo#getName()} to the field information. */
  private final IdentityHashMap<String, FieldInfo> nameToFieldInfoMap =
      new IdentityHashMap<String, FieldInfo>();

  /**
   * Unmodifiable sorted (with any possible {@code null} member first) list (without duplicates) of
   * {@link FieldInfo#getName()}.
   */
  final List<String> names;

  /**
   * Returns the class information for the given underlying class.
   *
   * @param underlyingClass underlying class or {@code null} for {@code null} result
   * @return class information or {@code null} for {@code null} input
   */
  public static ClassInfo of(Class<?> underlyingClass) {
    return of(underlyingClass, false);
  }

  /**
   * Returns the class information for the given underlying class.
   *
   * @param underlyingClass underlying class or {@code null} for {@code null} result
   * @param ignoreCase whether field names are case sensitive
   * @return class information or {@code null} for {@code null} input
   * @since 1.10
   */
  public static ClassInfo of(Class<?> underlyingClass, boolean ignoreCase) {
    if (underlyingClass == null) {
      return null;
    }
    final ConcurrentMap<Class<?>, ClassInfo> cache = ignoreCase ? CACHE_IGNORE_CASE : CACHE;

    // Logic copied from ConcurrentMap.computeIfAbsent
    ClassInfo v, newValue;
    return ((v = cache.get(underlyingClass)) == null
            && (newValue = new ClassInfo(underlyingClass, ignoreCase)) != null
            && (v = cache.putIfAbsent(underlyingClass, newValue)) == null)
        ? newValue
        : v;
  }

  /**
   * Returns the underlying class.
   *
   * @since 1.4
   */
  public Class<?> getUnderlyingClass() {
    return clazz;
  }

  /**
   * Returns whether field names are case sensitive.
   *
   * @since 1.10
   */
  public final boolean getIgnoreCase() {
    return ignoreCase;
  }

  /**
   * Returns the information for the given {@link FieldInfo#getName()}.
   *
   * @param name {@link FieldInfo#getName()} or {@code null}
   * @return field information or {@code null} for none
   */
  public FieldInfo getFieldInfo(String name) {
    if (name != null) {
      if (ignoreCase) {
        name = name.toLowerCase(Locale.US);
      }
      name = name.intern();
    }
    return nameToFieldInfoMap.get(name);
  }

  /**
   * Returns the field for the given {@link FieldInfo#getName()}.
   *
   * @param name {@link FieldInfo#getName()} or {@code null}
   * @return field or {@code null} for none
   */
  public Field getField(String name) {
    FieldInfo fieldInfo = getFieldInfo(name);
    return fieldInfo == null ? null : fieldInfo.getField();
  }

  /**
   * Returns the underlying class is an enum.
   *
   * @since 1.4
   */
  public boolean isEnum() {
    return clazz.isEnum();
  }

  /**
   * Returns an unmodifiable sorted set (with any possible {@code null} member first) of {@link
   * FieldInfo#getName() names}.
   */
  public Collection<String> getNames() {
    return names;
  }

  private ClassInfo(Class<?> srcClass, boolean ignoreCase) {
    clazz = srcClass;
    this.ignoreCase = ignoreCase;
    Preconditions.checkArgument(
        !ignoreCase || !srcClass.isEnum(), "cannot ignore case on an enum: " + srcClass);
    // name set has a special comparator to keep null first
    TreeSet<String> nameSet =
        new TreeSet<String>(
            new Comparator<String>() {
              public int compare(String s0, String s1) {
                return Objects.equal(s0, s1)
                    ? 0
                    : s0 == null ? -1 : s1 == null ? 1 : s0.compareTo(s1);
              }
            });
    // iterate over declared fields
    for (Field field : srcClass.getDeclaredFields()) {
      FieldInfo fieldInfo = FieldInfo.of(field);
      if (fieldInfo == null) {
        continue;
      }
      String fieldName = fieldInfo.getName();
      if (ignoreCase) {
        fieldName = fieldName.toLowerCase(Locale.US).intern();
      }
      FieldInfo conflictingFieldInfo = nameToFieldInfoMap.get(fieldName);
      Preconditions.checkArgument(
          conflictingFieldInfo == null,
          "two fields have the same %sname <%s>: %s and %s",
          ignoreCase ? "case-insensitive " : "",
          fieldName,
          field,
          conflictingFieldInfo == null ? null : conflictingFieldInfo.getField());
      nameToFieldInfoMap.put(fieldName, fieldInfo);
      nameSet.add(fieldName);
    }
    // inherit from super class and add only fields not already in nameToFieldInfoMap
    Class<?> superClass = srcClass.getSuperclass();
    if (superClass != null) {
      ClassInfo superClassInfo = ClassInfo.of(superClass, ignoreCase);
      nameSet.addAll(superClassInfo.names);
      for (Map.Entry<String, FieldInfo> e : superClassInfo.nameToFieldInfoMap.entrySet()) {
        String name = e.getKey();
        if (!nameToFieldInfoMap.containsKey(name)) {
          nameToFieldInfoMap.put(name, e.getValue());
        }
      }
    }
    names =
        nameSet.isEmpty()
            ? Collections.<String>emptyList()
            : Collections.unmodifiableList(new ArrayList<String>(nameSet));
  }

  /**
   * Returns an unmodifiable collection of the {@code FieldInfo}s for this class, without any
   * guarantee of order.
   *
   * <p>If you need sorted order, instead use {@link #getNames()} with {@link
   * #getFieldInfo(String)}.
   *
   * @since 1.16
   */
  public Collection<FieldInfo> getFieldInfos() {
    return Collections.unmodifiableCollection(nameToFieldInfoMap.values());
  }
}

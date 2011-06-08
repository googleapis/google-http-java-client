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

import com.google.common.base.Preconditions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.WeakHashMap;

/**
 * Parses class information to determine data key name/value pairs associated with the class.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class ClassInfo {

  /** Class information cache. */
  private static final Map<Class<?>, ClassInfo> CACHE = new WeakHashMap<Class<?>, ClassInfo>();

  /** Class. */
  private final Class<?> clazz;

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
    if (underlyingClass == null) {
      return null;
    }
    synchronized (CACHE) {
      ClassInfo classInfo = CACHE.get(underlyingClass);
      if (classInfo == null) {
        classInfo = new ClassInfo(underlyingClass);
        CACHE.put(underlyingClass, classInfo);
      }
      return classInfo;
    }
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
   * Returns the information for the given {@link FieldInfo#getName()}.
   *
   * @param name {@link FieldInfo#getName()} or {@code null}
   * @return field information or {@code null} for none
   */
  public FieldInfo getFieldInfo(String name) {
    return nameToFieldInfoMap.get(name == null ? null : name.intern());
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
   * Returns an unmodifiable sorted set (with any possible {@code null} member first) of
   * {@link FieldInfo#getName() names}.
   */
  public Collection<String> getNames() {
    return names;
  }

  private ClassInfo(Class<?> srcClass) {
    clazz = srcClass;
    // name set has a special comparator to keep null first
    TreeSet<String> nameSet = new TreeSet<String>(new Comparator<String>() {
      public int compare(String s0, String s1) {
        return s0 == s1 ? 0 : s0 == null ? -1 : s1 == null ? 1 : s0.compareTo(s1);
      }
    });
    // inherit from super class
    Class<?> superClass = srcClass.getSuperclass();
    if (superClass != null) {
      ClassInfo superClassInfo = ClassInfo.of(superClass);
      nameToFieldInfoMap.putAll(superClassInfo.nameToFieldInfoMap);
      nameSet.addAll(superClassInfo.names);
    }
    // iterate over declared fields
    for (Field field : srcClass.getDeclaredFields()) {
      FieldInfo fieldInfo = FieldInfo.of(field);
      if (fieldInfo == null) {
        continue;
      }
      String fieldName = fieldInfo.getName();
      FieldInfo conflictingFieldInfo = nameToFieldInfoMap.get(fieldName);
      Preconditions.checkArgument(conflictingFieldInfo == null,
          "two fields have the same name <%s>: %s and %s", fieldName, field,
          conflictingFieldInfo == null ? null : conflictingFieldInfo.getField());
      nameToFieldInfoMap.put(fieldName, fieldInfo);
      nameSet.add(fieldName);
    }
    names = nameSet.isEmpty() ? Collections.<String>emptyList() : Collections.unmodifiableList(
        new ArrayList<String>(nameSet));
  }
}

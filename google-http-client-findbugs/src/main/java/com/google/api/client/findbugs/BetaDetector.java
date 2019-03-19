/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.findbugs;

import com.google.api.client.util.Beta;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

/**
 * Findbugs plugin detector which detects usage of {@link Beta} in your code.
 *
 * @author Eyal Peled
 */
public class BetaDetector extends OpcodeStackDetector {

  /** Beta annotation "signature". */
  private static final String BETA_ANNOTATION = "Lcom/google/api/client/util/Beta;";

  /** A message indicating there is a usage of a method annotated with Beta annotation. */
  private static final String BETA_METHOD_USAGE = "BETA_METHOD_USAGE";

  /** A message indicating there is a usage of a field annotated with Beta annotation. */
  private static final String BETA_FIELD_USAGE = "BETA_FIELD_USAGE";

  /** A message indicating there is a usage of a class annotated with Beta annotation. */
  private static final String BETA_CLASS_USAGE = "BETA_CLASS_USAGE";

  /** The bug reporter is used to report errors. */
  private final BugReporter bugReporter;

  public BetaDetector(BugReporter bugReporter) {
    this.bugReporter = bugReporter;
  }

  @Override
  public void sawOpcode(int seen) {
    switch (seen) {
      case INVOKEINTERFACE:
      case INVOKESTATIC:
      case INVOKESPECIAL:
      case INVOKEVIRTUAL:
        // Method usage
        checkMethod(getNameConstantOperand(), getSigConstantOperand());
        break;

      case GETFIELD:
      case GETFIELD_QUICK:
      case GETFIELD_QUICK_W:
      case PUTFIELD:
      case PUTFIELD_QUICK:
      case PUTFIELD_QUICK_W:
      case GETSTATIC:
      case GETSTATIC_QUICK:
      case GETSTATIC2_QUICK:
      case PUTSTATIC:
      case PUTSTATIC_QUICK:
      case PUTSTATIC2_QUICK:
        // Field usage
        checkField(getNameConstantOperand());
        break;

      case LDC:
      case LDC_W:
      case LDC2_W:
        // Class usage
        if (getConstantRefOperand() instanceof ConstantClass) {
          // report bug in case it's google api @Beta class
          checkClass();
        }
        break;

      default:
        // DO NOTHING
    }
  }

  /** Returns true if the given annotations contain {@link Beta}. */
  private static boolean isBeta(AnnotationEntry[] annotationEntries) {
    for (AnnotationEntry annotation : annotationEntries) {
      if (BETA_ANNOTATION.equals(annotation.getAnnotationType())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns {@link JavaClass} for the current operand only if it's a Google APIs Client library
   * class, it's not {@link Beta} and it doesn't appear in other {@link Beta} section. Otherwise
   * returns {@code null}.
   *
   * <p>Reports a bug in case the class is {@link Beta}.
   */
  private JavaClass checkClass() {
    // TODO(peleyal): check if caching the beta state of every class could improve
    // performance on large projects

    try {
      JavaClass javaClass = Repository.lookupClass(getClassConstantOperand());
      boolean isGoogleClass = javaClass.getClassName().startsWith("com.google.api.client");
      if (!isGoogleClass) {
        return null;
      }

      // suppress errors when declaring fields inside a class (e.g. declaration of Beta
      // field in Beta class)
      if (javaClass.getClassName().equals(getDottedClassName())) {
        return null;
      }

      // suppress errors if the container class or method is beta
      if (isBeta(getThisClass().getAnnotationEntries())
          || (getMethod() != null && isBeta(getMethod().getAnnotationEntries()))) {
        return null;
      }

      if (isBeta(javaClass.getAnnotationEntries())) {
        bugReporter.reportBug(createBugInstance(BETA_CLASS_USAGE).addClass(javaClass));
        return null;
      }

      return javaClass;
    } catch (ClassNotFoundException e) {
      bugReporter.reportMissingClass(e);
      return null;
    }
  }

  /** Returns the superclass of the specified class. */
  private JavaClass getSuperclass(JavaClass javaClass) {
    try {
      return javaClass.getSuperClass();
    } catch (ClassNotFoundException e) {
      bugReporter.reportMissingClass(e);
      return null;
    }
  }

  /**
   * Reports bug in case the method defined by the given name and signature is {@link Beta}.
   *
   * <p>The method is searched in current class and all super classses as well.
   */
  private void checkMethod(String methodName, String signature) {
    JavaClass javaClass = checkClass();
    if (javaClass == null) {
      return;
    }

    for (JavaClass current = javaClass; current != null; current = getSuperclass(current)) {
      for (Method method : current.getMethods()) {
        if (methodName.equals(method.getName()) && signature.equals(method.getSignature())) {
          // method has been found - check if it's beta
          if (isBeta(method.getAnnotationEntries())) {
            bugReporter.reportBug(createBugInstance(BETA_METHOD_USAGE).addCalledMethod(this));
          }
          return;
        }
      }
    }
    if (!javaClass.isAbstract()) {
      bugReporter.logError(
          "Can't locate method " + javaClass.getClassName() + "." + methodName + signature);
    }
  }

  /**
   * Reports bug in case the field defined by the given name is {@link Beta}.
   *
   * <p>The field is searched in current class and all super classses as well.
   */
  private void checkField(String fieldName) {
    JavaClass javaClass = checkClass();
    if (javaClass == null) {
      return;
    }

    for (JavaClass current = javaClass; current != null; current = getSuperclass(current)) {
      for (Field field : current.getFields()) {
        if (fieldName.equals(field.getName())) {
          // field has been found - check if it's beta
          if (isBeta(field.getAnnotationEntries())) {
            bugReporter.reportBug(createBugInstance(BETA_FIELD_USAGE).addReferencedField(this));
          }
          return;
        }
      }
    }
    bugReporter.logError("Can't locate field " + javaClass.getClassName() + "." + fieldName);
  }

  /** Returns a new bug instance with source line and class information. */
  private BugInstance createBugInstance(String type) {
    return new BugInstance(this, type, NORMAL_PRIORITY).addClassAndMethod(this).addSourceLine(this);
  }
}

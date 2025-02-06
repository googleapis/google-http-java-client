package com.google.api.client.extensions.appengine.nativeimage;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

public class HttpAppEngineFeature implements Feature {
  private static final List<String> APP_ENGINE_CLASSES =
      ImmutableList.of(
          "com.google.appengine.api.appidentity.AppIdentityServicePb",
          "com.google.appengine.api.capabilities.CapabilityServicePb",
          "com.google.appengine.api.modules.ModulesServicePb");

  private static final List<String> APP_HOSTING_CLASSES =
      ImmutableList.of(
          "com.google.apphosting.api.UserServicePb",
          "com.google.apphosting.api.logservice.LogServicePb");

  @Override
  public void beforeAnalysis(BeforeAnalysisAccess access) {
    try {
      for (String className : APP_ENGINE_CLASSES) {
        registerClassForReflection(className, access);
      }
      for (String className : APP_HOSTING_CLASSES) {
        registerClassForReflection(className, access);
      }
    } catch (Throwable e) {
      throw new RuntimeException("Error configuring TestFeature", e);
    }
  }

  private void registerClassForReflection(
      String classFullQualifiedName, BeforeAnalysisAccess access) {
    Class<?> clazz = access.findClassByName(classFullQualifiedName);
    RuntimeReflection.register(clazz);
    RuntimeReflection.register(clazz.getDeclaredConstructors());
    RuntimeReflection.register(clazz.getDeclaredMethods());
    RuntimeReflection.register(clazz.getDeclaredFields());
    for (Class<?> subClass : clazz.getInterfaces()) {
      registerClassForReflection(subClass.getName(), access);
    }
  }
}

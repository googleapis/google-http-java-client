package com.google.api.client;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Repro {

  public static java.lang.Object repro() throws InstantiationException, IllegalAccessException {
    System.out.println("clazz.getName() = " + java.lang.Object.class.getName());
    java.lang.Object instance = java.lang.Object.class.newInstance();
    System.out.println("instance.getClass().getName() = " + instance.getClass().getName());
    return instance;
  }
}
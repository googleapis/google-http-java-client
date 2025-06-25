package com.google.api.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ReproTest {

  @Test
  public void testRepro() throws InstantiationException, IllegalAccessException {
    java.lang.Object obj = Repro.repro();
    assertEquals("java.lang.Object", obj.getClass().getName());
  }

}

package org.lsst.camera.etraveler.backend.API;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class TestRunner {
  public static void main(String[] args) {
    Result result = JUnitCore.runClasses(TestAPI.class);
  }
}

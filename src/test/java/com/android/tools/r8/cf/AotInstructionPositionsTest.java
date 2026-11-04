// Copyright (c) 2026, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.cf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.android.tools.r8.TestBase;
import com.android.tools.r8.TestRuntime;
import com.android.tools.r8.utils.InternalOptions.LineNumberOptimization;
import org.junit.Test;

public class AotInstructionPositionsTest extends TestBase {
  @Test
  public void defaultReleasePolicy() throws Exception {
    compileAndRun(false);
  }

  @Test
  public void downstreamAotPositions() throws Exception {
    compileAndRun(true);
  }

  private void compileAndRun(boolean retain) throws Exception {
    testForR8(Backend.CF)
        .addProgramClasses(TestClass.class)
        .addKeepRules("-keep,allowoptimization class " + TestClass.class.getTypeName() + " { *; }")
        .addOptionsModification(
            options -> {
              assertFalse(options.debug);
              options.retainClassFileInstructionPositions = retain;
              options.lineNumberOptimization = LineNumberOptimization.OFF;
              options.inlinerOptions().enableInlining = false;
            })
        .compile()
        .inspect(
            inspector ->
                assertEquals(
                    retain,
                    inspector
                        .clazz(TestClass.class)
                        .uniqueMethodWithOriginalName("work")
                        .hasLineNumberTable()))
        .run(TestRuntime.getSystemRuntime(), TestClass.class)
        .assertSuccessWithOutputLines("7");
  }

  public static class TestClass {
    public static int work(int value) {
      return value + 7;
    }

    public static void main(String[] args) {
      System.out.println(work(args.length));
    }
  }
}

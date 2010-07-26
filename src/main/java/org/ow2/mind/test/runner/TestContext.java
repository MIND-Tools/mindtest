/**
 * Copyright (C) 2010 STMicroelectronics
 *
 * This file is part of "Mind Compiler" is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: mind@ow2.org
 *
 * Authors: Matthieu Leclercq
 * Contributors: 
 */

package org.ow2.mind.test.runner;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.objectweb.fractal.adl.Node;
import org.ow2.mind.test.TestException;
import org.ow2.mind.test.ast.Config;
import org.ow2.mind.test.ast.Test;
import org.ow2.mind.test.ast.TestSet;

public class TestContext {

  public static final String TEST_SET_NAME            = "TESTSET_NAME";
  public static final String TEST_NAME                = "TEST_NAME";
  public static final String TEST_ADL                 = "TEST_ADL";
  public static final String CONFIG_NAME              = "CONFIG_NAME";
  public static final String CONFIG_TARGET_DESCRIPTOR = "CONFIG_TARGET_DESCRIPTOR";

  public static final String EXEC_NAME                = "EXEC_NAME";

  final String               testSetName;
  final String               testName;
  final String               testADL;

  String                     configName;
  String                     configTargetDescriptor;
  final Map<String, String>  context                  = new HashMap<String, String>();

  public TestContext(TestSet testSet, Test test) {
    testSetName = testSet.getName();
    testName = test.getName();
    testADL = test.getAdl();
  }

  public void startConfig(Config config) {
    configName = config.getName();
    configTargetDescriptor = config.getTargetDescriptor();
    context.clear();
  }

  public void setContextValue(String name, String value) {
    if (name.equals(TEST_SET_NAME) || name.equals(TEST_NAME)
        || name.equals(TEST_ADL) || name.equals(CONFIG_NAME)
        || name.equals(CONFIG_TARGET_DESCRIPTOR)) {
      throw new IllegalArgumentException(name + " is a reserved name");
    }
    context.put(name, value);
  }

  public String getContextValue(String name) {
    if (name.equals(TEST_SET_NAME)) return testSetName;
    if (name.equals(TEST_NAME)) return testName;
    if (name.equals(TEST_ADL)) return testADL;
    if (name.equals(CONFIG_NAME)) return configName;
    if (name.equals(CONFIG_TARGET_DESCRIPTOR)) return configTargetDescriptor;

    return context.get(name);
  }

  public String getTestSetName() {
    return testSetName;
  }

  public String getTestName() {
    return testName;
  }

  public String getTestADL() {
    return testADL;
  }

  public String getConfigName() {
    return configName;
  }

  public String getConfigTargetDescriptor() {
    return configTargetDescriptor;
  }

  protected String interpolate(final String s) throws TestException {
    if (s == null) return null;
    final int i = s.indexOf("${");
    if (i == -1) return s;

    final int j = s.indexOf("}", i);
    if (j == -1) {
      throw new TestException("Invalid string \"" + s + "\"");
    }
    final String varName = s.substring(i + 2, j);

    String value = System.getProperty(varName);
    if (value == null) value = System.getenv(varName);
    if (value == null) value = getContextValue(varName);

    if (value == null) return s;

    return interpolate(s.substring(0, i) + value + s.substring(j + 1));
  }

}

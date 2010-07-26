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

package org.ow2.mind.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.objectweb.fractal.adl.xml.XMLNodeFactoryImpl;
import org.ow2.mind.test.ast.Command;
import org.ow2.mind.test.ast.Config;
import org.ow2.mind.test.ast.Mindc;
import org.ow2.mind.test.ast.Test;
import org.ow2.mind.test.ast.TestSet;
import org.testng.annotations.BeforeMethod;

public class LoaderTest {

  TestSetLoader loader;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    TestSetParser tsp = new TestSetParser();
    XMLCommandLoader xcl = new XMLCommandLoader();
    ConfigRefLoader crl = new ConfigRefLoader();
    ParameterSpreadingLoader psl = new ParameterSpreadingLoader();

    psl.clientLoaderItf = crl;
    crl.clientLoaderItf = xcl;
    xcl.clientLoaderItf = tsp;

    tsp.nodeFactoryItf = new XMLNodeFactoryImpl();

    loader = psl;
  }

  @org.testng.annotations.Test(groups = {"functional", "checkin"})
  public void test1() throws Exception {
    TestSet testSet = loader.load(getTest("unitTests/test1.xml"));

    assertEquals(testSet.getName(), "testSuite1");

    Test[] tests = testSet.getTests();
    assertEquals(tests.length, 3);

    Test test1 = tests[0];
    assertEquals(test1.getName(), "test1");
    assertEquals(test1.getAdl(), "foo.bar1");
    Config[] test1Configs = test1.getConfigs();
    assertEquals(test1Configs.length, 1);
    assertEquals(test1Configs[0].getName(), "conf2");
    Command[] test1Commands = test1Configs[0].getCommands();
    assertEquals(test1Commands.length, 2);
    assertTrue(test1Commands[0] instanceof Mindc);
    assertEquals(((Mindc) test1Commands[0]).getAdl(), "foo.bar1");
    assertEquals(((Mindc) test1Commands[0]).getTargetDescriptor(), "foo");
    assertEquals(test1Commands[1].getCommand(), "cmd2");

    Test test2 = tests[1];
    assertEquals(test2.getName(), "test2");
    assertEquals(test2.getAdl(), "foo.bar2");
    Config[] test2Configs = test2.getConfigs();
    assertEquals(test2Configs.length, 2);
    assertEquals(test2Configs[0].getName(), "conf1");
    assertEquals(test2Configs[1].getName(), "conf2");
    Command[] test21Commands = test2Configs[0].getCommands();
    assertEquals(test21Commands.length, 1);
    assertEquals(test21Commands[0].getCommand(), "cmd1");
    Command[] test22Commands = test2Configs[1].getCommands();
    assertEquals(test22Commands.length, 2);
    assertTrue(test22Commands[0] instanceof Mindc);
    assertEquals(((Mindc) test22Commands[0]).getAdl(), "foo.bar2");
    assertEquals(((Mindc) test22Commands[0]).getTargetDescriptor(), "foo");
    assertEquals(test22Commands[1].getCommand(), "cmd2");

    Test test3 = tests[2];
    assertEquals(test3.getName(), "test3");
    assertEquals(test3.getAdl(), "foo.bar3");
    Config[] test3Configs = test3.getConfigs();
    assertEquals(test3Configs.length, 2);
    assertEquals(test3Configs[0].getName(), "conf1");
    assertEquals(test3Configs[1].getName(), "conf2");
    Command[] test31Commands = test3Configs[0].getCommands();
    assertEquals(test31Commands.length, 1);
    assertEquals(test31Commands[0].getCommand(), "cmd1");
    Command[] test32Commands = test3Configs[1].getCommands();
    assertEquals(test32Commands.length, 2);
    assertTrue(test32Commands[0] instanceof Mindc);
    assertEquals(((Mindc) test32Commands[0]).getAdl(), "foo.bar3");
    assertEquals(((Mindc) test32Commands[0]).getTargetDescriptor(), "foo");
    assertEquals(test32Commands[1].getCommand(), "cmd2");
  }

  private File getTest(String testName) throws Exception {
    URL url = getClass().getClassLoader().getResource(testName);
    return new File(url.toURI());
  }
}

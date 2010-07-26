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

import java.io.File;
import java.util.List;

import org.ow2.mind.test.result.AbstractResult.State;
import org.ow2.mind.test.result.ConfigResult;
import org.ow2.mind.test.result.TestResult;
import org.ow2.mind.test.result.TestSetResult;
import org.testng.annotations.Test;

public class IntegrationTest {

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    Launcher launcher = new Launcher();
    File outputDir = new File("target/it/build");
    outputDir.mkdirs();
    File reportDir = new File("target/it/reports");
    reportDir.mkdirs();
    launcher.setTestOptions(new TestOptions(
        "target/mindc/mindc-0.2.2/bin/mindc",
        "target/test-classes/integration/src", outputDir,
        reportDir, null, null, null, new String[] {"optimize"}));

    TestSetResult result = launcher.runTestSet(new File(
        "target/test-classes/integration/testset.xml"));

    launcher.printResult(result, System.out);
    launcher.dumpReport(result);
    
    assertEquals(result.getState(), State.FAILURE);
    TestResult testResult = result.getSubResults().get(0);
    List<ConfigResult> configResults = testResult.getSubResults();
    assertEquals(configResults.get(0).getState(), State.SUCCESS);
    assertEquals(configResults.get(1).getState(), State.SKIPPED);
    assertEquals(configResults.get(2).getState(), State.FAILURE);
  }

}

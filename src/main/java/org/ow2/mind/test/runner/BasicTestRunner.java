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

import java.util.ArrayList;
import java.util.List;

import org.ow2.mind.test.TestException;
import org.ow2.mind.test.TestOptions;
import org.ow2.mind.test.ast.Command;
import org.ow2.mind.test.ast.Config;
import org.ow2.mind.test.ast.Test;
import org.ow2.mind.test.ast.TestSet;
import org.ow2.mind.test.result.CommandResult;
import org.ow2.mind.test.result.ConfigResult;
import org.ow2.mind.test.result.TestResult;
import org.ow2.mind.test.result.TestSetResult;
import org.ow2.mind.test.result.AbstractResult.State;

public class BasicTestRunner implements TestRunner {

  // ---------------------------------------------------------------------------
  // Implementation of the TestRunner interface
  // ---------------------------------------------------------------------------

  public TestSetResult run(TestSet testSet, TestOptions options)
      throws TestException {
    CommandRunner commandRunner = new CommandRunner(options);
    List<TestResult> testResults = new ArrayList<TestResult>();
    for (Test test : testSet.getTests()) {
      if (!options.isRunnableTest(test.getName())) {
        testResults.add(new TestResult(test));
        continue;
      }
      
      TestContext testContext = new TestContext(testSet, test);

      List<ConfigResult> configResults = new ArrayList<ConfigResult>();
      for (Config config : test.getConfigs()) {
        if (!options.isRunnableConf(config.getName())) {
          configResults.add(new ConfigResult(config));
          continue;
        }

        testContext.startConfig(config);
        List<CommandResult> commandResults = new ArrayList<CommandResult>();
        for (Command command : config.getCommands()) {
          CommandResult commandResult = commandRunner.run(command, testContext);
          commandResults.add(commandResult);
          if (commandResult.getState() != State.SUCCESS) break;
        }
        configResults.add(new ConfigResult(config, commandResults));
      }
      testResults.add(new TestResult(test, configResults));
    }
    return new TestSetResult(testSet, testResults);
  }
}

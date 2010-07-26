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

package org.ow2.mind.test.dumper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import javax.swing.filechooser.FileSystemView;

import org.ow2.mind.test.TestOptions;
import org.ow2.mind.test.result.AbstractResult.State;
import org.ow2.mind.test.result.CommandResult;
import org.ow2.mind.test.result.ConfigResult;
import org.ow2.mind.test.result.TestResult;
import org.ow2.mind.test.result.TestSetResult;

public class JUnitXMLDumper extends AbstractXMLDumper {

  public JUnitXMLDumper(TestOptions options) {
    super(options);
  }

  @Override
  protected File getReportFile(TestSetResult testSetResult) {
    return new File(options.getReportDir(), testSetResult.getTestElem()
        .getName() + "-TestSuite.xml");
  }

  protected void visitTestSet(TestSetResult testSetResult) {
    int nbTests = 0;
    for (TestResult testResult : testSetResult.getSubResults()) {
      nbTests += testResult.getNbConfig();
    }

    startElement("testsuite", attribute("errors", "0"),
        attribute("failures", testSetResult.getNbFailure()),
        attribute("name", testSetResult.getTestElem().getName()),
        attribute("tests", nbTests),
        attribute("time", testSetResult.getExecutionTime() / 1000.0));

    for (TestResult testResult : testSetResult.getSubResults()) {
      visiteTest(testSetResult, testResult);
    }

    endElement();
  }

  protected void visiteTest(TestSetResult testSetResult, TestResult testResult) {
    for (ConfigResult configResult : testResult.getSubResults()) {
      visiteConfig(testSetResult, testResult, configResult);
    }
  }

  protected void visiteConfig(TestSetResult testSetResult,
      TestResult testResult, ConfigResult configResult) {
    startElement("testcase",
        attribute("classname", testResult.getTestElem().getName()),
        attribute("name", configResult.getTestElem().getName()),
        attribute("time", configResult.getExecutionTime() / 1000.0));

    if (configResult.getState() == State.SKIPPED) {
      element("skipped");
    } else if (configResult.getState() == State.FAILURE) {
      startElement("failure");
      for (CommandResult commandResult : configResult.getSubResults()) {
        sb.append("Output of command (result=")
            .append(commandResult.getReturnCode()).append(")\n");
        sb.append("  ").append(commandResult.getTestElem().getCommand())
            .append("\n");
        if (commandResult.getTestElem().getDir() != null) {
          sb.append("  Executed in directory: '")
              .append(commandResult.getTestElem().getDir()).append("'\n");
        }
        sb.append(escape(commandResult.getOutput())).append("\n\n");
      }
      endElement();
    }
    endElement();
  }
}

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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ow2.mind.test.TestOptions;
import org.ow2.mind.test.result.AbstractResult.State;
import org.ow2.mind.test.result.CommandResult;
import org.ow2.mind.test.result.ConfigResult;
import org.ow2.mind.test.result.TestResult;
import org.ow2.mind.test.result.TestSetResult;

public class MindTestTextDumper {
  protected StringBuilder sb;
  protected int           indent;
  final TestOptions       options;

  private List<String>    elems;

  public MindTestTextDumper(TestOptions options) {
    this.options = options;
  }

  public void dumpTestSet(TestSetResult testSetResult)
      throws FileNotFoundException {
    sb = new StringBuilder();
    elems = new ArrayList<String>();
    indent = 0;
    visitTestSet(testSetResult);

    File outputFile = new File(options.getReportDir(), testSetResult
        .getTestElem().getName() + "-mindtest.txt");
    PrintStream ps = new PrintStream(outputFile);
    ps.print(sb.toString());
    ps.close();
  }

  protected void visitTestSet(TestSetResult testSetResult) {
    sb.append("================================================================================\n");
    sb.append("  Test Set \"").append(testSetResult.getTestElem().getName())
        .append("\"\n");
    sb.append("  Tests run: ").append(testSetResult.getNbTests())
        .append(", Failures: ").append(testSetResult.getNbFailure())
        .append(", Skipped: ").append(testSetResult.getNbSkipped());
    sb.append("  Time elapsed: ").append(testSetResult.formatExecutionTime());
    sb.append("\n================================================================================\n\n");
    if (testSetResult.getState() == State.FAILURE) {
      for (TestResult testResult : testSetResult.getSubResults()) {
        if (testResult.getState() == State.FAILURE) {
          visitSet(testResult);
        }
      }
    }
  }

  protected void visitSet(TestResult testResult) {
    for (ConfigResult configResult : testResult.getSubResults()) {
      if (configResult.getState() == State.FAILURE) {
        visitConfig(testResult, configResult);
      }
    }
  }

  protected void visitConfig(TestResult testResult, ConfigResult configResult) {
    // the faulty command is necessarily the last one
    List<CommandResult> commandResults = configResult.getSubResults();
    CommandResult commandResult = commandResults.get(commandResults.size() - 1);

    sb.append("Test: \"").append(testResult.getTestElem().getName())
        .append("\" Config: \"").append(configResult.getTestElem().getName())
        .append("\" FAILLED\n");
    sb.append("  Command: ").append(commandResult.getExecutedCommand()).append("\n");
    Iterator<String> commandArgs = commandResult.getCommandArgs().iterator();
    if (commandArgs.hasNext()) {
      sb.append("     args: ").append(commandArgs.next()).append("\n");
      while (commandArgs.hasNext()) {
        sb.append("           ").append(commandArgs.next()).append("\n");
      }
    }
    sb.append("  Output: ");
    for (String outputLine : commandResult.getOutput().split("\n")) {
      sb.append(outputLine).append("\n          ");
    }
    sb.append("\n================================================================================\n\n");
  }

}

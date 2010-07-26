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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.mind.test.TestOptions;
import org.ow2.mind.test.ast.Mindc;
import org.ow2.mind.test.result.AbstractResult.State;
import org.ow2.mind.test.result.CommandResult;
import org.ow2.mind.test.result.ConfigResult;
import org.ow2.mind.test.result.TestResult;
import org.ow2.mind.test.result.TestSetResult;

public class MindTestXMLDumper extends AbstractXMLDumper {

  public MindTestXMLDumper(TestOptions options) {
    super(options);
  }

  @Override
  protected File getReportFile(TestSetResult testSetResult) {
    return new File(options.getReportDir(), testSetResult.getTestElem()
        .getName() + "-mindtest.xml");
  }

  protected void visitTestSet(TestSetResult testSetResult) {
    startElement("testset",
        attribute("name", testSetResult.getTestElem().getName()),
        attribute("tests", testSetResult.getNbTests()),
        attribute("skipped", testSetResult.getNbSkipped()),
        attribute("failures", testSetResult.getNbFailure()),
        attribute("time", testSetResult.formatExecutionTime()),
        attribute("time-ms", testSetResult.getExecutionTime()));

    startElement("options");
    if (options.getMindcCmd() != null)
      element("option", attribute("name", "mindc-command"),
          attribute("value", options.getMindcCmd()));
    if (options.getSrcPath() != null)
      element("option", attribute("name", "src-path"),
          attribute("value", options.getSrcPath()));
    if (options.getOutputDir() != null)
      element("option", attribute("name", "out-path"),
          attribute("value", options.getOutputDir().getPath()));
    if (options.getIncludeTests() != null)
      element("option", attribute("name", "include-test"),
          attribute("value", options.getIncludeTests().toString()));
    if (options.getExcludeTests() != null)
      element("option", attribute("name", "exclude-test"),
          attribute("value", options.getExcludeTests().toString()));
    if (options.getIncludeConfs() != null)
      element("option", attribute("name", "include-conf"),
          attribute("value", options.getIncludeConfs().toString()));
    if (options.getExcludeConfs() != null)
      element("option", attribute("name", "exclude-conf"),
          attribute("value", options.getExcludeConfs().toString()));
    endElement();

    startElement("environment");
    for (Map.Entry<String, String> env : System.getenv().entrySet()) {
      element("variable", attribute("name", env.getKey()),
          attribute("value", env.getValue()));
    }
    endElement();

    startElement("properties");
    for (Map.Entry<Object, Object> prop : System.getProperties().entrySet()) {
      element("property", attribute("name", prop.getKey().toString()),
          attribute("value", prop.getValue().toString()));
    }
    endElement();

    for (TestResult testResult : testSetResult.getSubResults()) {
      visitTest(testResult);
    }

    endElement();
  }

  protected void visitTest(TestResult testResult) {
    startElement(((testResult.getState() == State.FAILURE) ? "failled-" : "")
        + "test", attribute("name", testResult.getTestElem().getName()),
        attribute("configs", testResult.getNbConfig()),
        attribute("skipped", testResult.getNbSkipped()),
        attribute("failures", testResult.getNbFailure()),
        attribute("time", testResult.formatExecutionTime()),
        attribute("time-ms", testResult.getExecutionTime()));

    if (testResult.getState() == State.SKIPPED) {
      element("skipped");
    } else {
      for (ConfigResult configResult : testResult.getSubResults()) {
        visitConfig(configResult);
      }
    }

    endElement();
  }

  protected void visitConfig(ConfigResult configResult) {
    startElement(((configResult.getState() == State.FAILURE) ? "failled-" : "")
        + "config", attribute("name", configResult.getTestElem().getName()),
        attribute("time", configResult.formatExecutionTime()),
        attribute("time-ms", configResult.getExecutionTime()));

    if (configResult.getState() == State.SKIPPED) {
      element("skipped");
    } else {
      for (CommandResult commandResult : configResult.getSubResults()) {
        visitCommand(commandResult);
      }
    }
    endElement();
  }

  protected void visitCommand(CommandResult commandResult) {
    if (commandResult.getTestElem() instanceof Mindc) {
      visitMindcCommand(commandResult);
    } else {
      List<String> attributes = new ArrayList<String>(9);
      attributes.add(attribute("command", commandResult.getExecutedCommand()));
      if (commandResult.getTestElem().getDir() != null)
        attributes.add(attribute("dir", commandResult.getTestElem().getDir()));
      attributes.add(attribute("result", commandResult.getReturnCode()));
      attributes.add(attribute("time", commandResult.formatExecutionTime()));
      attributes.add(attribute("time-ms", commandResult.getExecutionTime()));

      startElement(((commandResult.getState() == State.FAILURE)
          ? "failled-"
          : "") + "command", attributes);
      for (String arg : commandResult.getCommandArgs()) {
        element("arg", attribute("arg", arg));
      }
      startElement("output");
      sb.append(commandResult.getOutput());
      endElement();

      endElement();
    }
  }

  protected void visitMindcCommand(CommandResult commandResult) {
    Mindc mindc = (Mindc) commandResult.getTestElem();

    List<String> attributes = new ArrayList<String>(9);
    attributes.add(attribute("mindc", commandResult.getExecutedCommand()));
    attributes.add(attribute("adl", mindc.getAdl()));
    attributes.add(attribute("execName", mindc.getExecName()));
    attributes.add(attribute("result", commandResult.getReturnCode()));
    attributes.add(attribute("target-descriptor", mindc.getTargetDescriptor()));
    if (mindc.getCFlags() != null)
      attributes.add(attribute("cFlags", mindc.getCFlags()));
    if (mindc.getLdFlags() != null)
      attributes.add(attribute("ldFlags", mindc.getLdFlags()));
    attributes.add(attribute("time", commandResult.formatExecutionTime()));
    attributes.add(attribute("time-ms", commandResult.getExecutionTime()));

    startElement(
        ((commandResult.getState() == State.FAILURE) ? "failled-" : "")
            + "mindc", attributes);

    startElement("output");
    sb.append(escape(commandResult.getOutput()));
    endElement();

    endElement();
  }
}

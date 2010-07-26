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

import static java.lang.Character.isWhitespace;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ow2.mind.test.TestException;
import org.ow2.mind.test.TestOptions;
import org.ow2.mind.test.ast.Command;
import org.ow2.mind.test.ast.Mindc;
import org.ow2.mind.test.result.CommandResult;
import org.ow2.mind.test.result.AbstractResult.State;

public class CommandRunner {

  final TestOptions options;

  public CommandRunner(TestOptions options) {
    this.options = options;
  }

  public CommandResult run(Command command, TestContext testContext)
      throws TestException {
    if (command instanceof Mindc) {
      return run((Mindc) command, testContext);
    }

    String dir = testContext.interpolate(command.getDir());
    List<String> cmdLine = splitOptionString(testContext.interpolate(command
        .getCommand()));

    return execCommand(command, cmdLine, dir);
  }

  protected CommandResult execCommand(Command command, List<String> cmdLine,
      String dir) throws TestException {
    int result = 1;

    ProcessBuilder pb = new ProcessBuilder(cmdLine);
    if (dir != null) {
      File workdir = new File(dir);
      if (!workdir.isDirectory()) {
        throw new TestException("At " + command.astGetSource()
            + " invalid directory \"" + dir + "\".");
      }
      pb.directory(workdir);
    }
    pb.redirectErrorStream(true);

    long beginTime = System.currentTimeMillis();

    ProcessRunner runner;
    try {
      final Process process = pb.start();
      runner = new ProcessRunner(process);
      runner.start();

      result = process.waitFor();
      runner.join();
    } catch (IOException e) {
      throw new TestException("Fail to execute command at "
          + command.astGetSource(), e);
    } catch (InterruptedException e) {
      throw new TestException("Fail to execute command at "
          + command.astGetSource(), e);
    }

    long endTime = System.currentTimeMillis();

    String output = runner.output.toString();

    return new CommandResult(command, cmdLine, result, output, endTime
        - beginTime);
  }

  public CommandResult run(Mindc command, TestContext testContext)
      throws TestException {
    List<String> cmd = new ArrayList<String>();
    String message = "Compile ADL \"" + command.getAdl() + "\"";
    if (options.getMindcCmd() == null) {
      cmd.add("mindc");
      message += " using mindc";
    } else {
      cmd.add(options.getMindcCmd());
      message += " using " + options.getMindcCmd();
    }

    message += "\n  with execName   : " + command.getExecName();

    if (options.getSrcPath() != null) {
      cmd.add("--src-path=" + options.getSrcPath());
      message += "\n  with src-path   : " + options.getSrcPath();
    }

    File outputDir = new File(options.getOutputDir(),
        testContext.getTestSetName() + File.separator
            + testContext.getTestName() + File.separator
            + testContext.getConfigName());
    outputDir.mkdirs();
    cmd.add("--out-path=" + outputDir);
    message += "\n  with out-path   : " + outputDir;

    if (command.getTargetDescriptor() != null) {
      cmd.add("--target-descriptor=" + command.getTargetDescriptor());
      message += "\n  with target-desc: " + command.getTargetDescriptor();
    }

    if (command.getCFlags() != null) {
      cmd.add("--c-flags=" + command.getCFlags());
      message += "\n  with c-flags    : " + command.getCFlags();
    }

    if (command.getLdFlags() != null) {
      cmd.add("--ld-flags=" + command.getLdFlags());
      message += "\n  with ld-flags   : " + command.getLdFlags();
    }

    //cmd.add("--force");

    cmd.add(command.getAdl() + ":" + command.getExecName());

    System.out.println(message);
    command.setCommand(message);
    CommandResult result = execCommand(command, cmd, null);
    if (result.getState() == State.SUCCESS) {
      File execFile = new File(outputDir, command.getExecName());
      testContext.setContextValue(TestContext.EXEC_NAME,
          execFile.getAbsolutePath());
    }
    return result;
  }

  private static final class ProcessRunner extends Thread {
    final Process process;
    StringBuilder output = new StringBuilder();

    ProcessRunner(Process process) {
      this.process = process;
    }

    public void run() {
      BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(
          process.getInputStream()));

      try {
        String line;
        while ((line = bufferedreader.readLine()) != null) {
          System.out.println(line);
          output.append(line).append("\n");
        }
      } catch (Exception e) {
      }
    }

  }

  public static List<String> splitOptionString(final String s) {
    if (s == null) {
      return Collections.emptyList();
    }

    final List<String> result = new ArrayList<String>();

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      final char c = s.charAt(i);
      if (isWhitespace(c)) {
        if (sb.length() != 0) {
          result.add(sb.toString());
          sb = new StringBuilder();
        }
      } else if (c == '\\') {
        if (i + 1 < s.length() && isWhitespace(s.charAt(i + 1))) {
          sb.append(s.charAt(i + 1));
          i++;
        } else {
          sb.append('\\');
        }
      } else {
        sb.append(c);
      }
    }
    if (sb.length() != 0) result.add(sb.toString());

    return result;
  }

}

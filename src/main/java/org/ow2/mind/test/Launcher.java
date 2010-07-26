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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.xml.XMLNodeFactoryImpl;
import org.ow2.mind.test.ast.TestSet;
import org.ow2.mind.test.dumper.JUnitXMLDumper;
import org.ow2.mind.test.dumper.MindTestTextDumper;
import org.ow2.mind.test.dumper.MindTestXMLDumper;
import org.ow2.mind.test.result.AbstractResult.State;
import org.ow2.mind.test.result.ConfigResult;
import org.ow2.mind.test.result.TestResult;
import org.ow2.mind.test.result.TestSetResult;
import org.ow2.mind.test.runner.BasicTestRunner;
import org.ow2.mind.test.runner.TestRunner;

public class Launcher {

  public static final String  DEFAULT_TEST_SET = "testset.xml";

  private static final String COMMAND_NAME     = "mindtest";

  protected final CmdArgument mindcCmdOpt      = new CmdArgument(
                                                   null,
                                                   "mindc-command",
                                                   "the command of the mindc compiler",
                                                   "<path>", "mindc", false);

  final CmdPathOption         srcPathOpt       = new CmdPathOption(
                                                   "S",
                                                   "src-path",
                                                   "the search path of ADL,IDL and implementation files (list of path separated by '"
                                                       + File.pathSeparator
                                                       + "')", "<path list>");

  final CmdArgument           outDirOpt        = new CmdArgument(
                                                   "o",
                                                   "out-path",
                                                   "Specify the output directory of mindc compiler",
                                                   "<output path>", ".", false);

  final CmdArgument           reportDirOpt     = new CmdArgument(
                                                   "r",
                                                   "report-path",
                                                   "Specify the output directory of generated reports",
                                                   "<output path>", null, false);

  final CmdArgument           includeTestOpt   = new CmdArgument(
                                                   null,
                                                   "include-test",
                                                   "Specify the tests to be executed (comma separated list)",
                                                   "<test name>", null, true);
  final CmdArgument           excludeTestOpt   = new CmdArgument(
                                                   null,
                                                   "exclude-test",
                                                   "Specify the tests to be excluded (comma separated list)",
                                                   "<test name>", null, true);

  final CmdArgument           includeConfOpt   = new CmdArgument(
                                                   null,
                                                   "include-config",
                                                   "Specify the configuration to be executed (comma separated list)",
                                                   "<test name>", null, true);
  final CmdArgument           excludeConfOpt   = new CmdArgument(
                                                   null,
                                                   "exclude-config",
                                                   "Specify the configuration to be excluded (comma separated list)",
                                                   "<test name>", null, true);

  final CmdFlag               helpOpt          = new CmdFlag("h", "help",
                                                   "Print this help and exit");

  final CmdFlag               versionOpt       = new CmdFlag("v", "version",
                                                   "Print version number and exit");

  final Options               options          = new Options();
  {
    options.addOptions(helpOpt, versionOpt, mindcCmdOpt, srcPathOpt, outDirOpt,
        reportDirOpt, includeTestOpt, excludeTestOpt, includeConfOpt,
        excludeConfOpt);
  }

  TestOptions                 testOptions;
  TestSetLoader               loader           = null;

  protected List<String> init(String... args)
      throws InvalidCommandLineException {
    // parse arguments to a CommandLine.
    final CommandLine cmdLine = CommandLine.parseArgs(options, false, args);

    // If help is asked, print it and exit.
    if (helpOpt.isPresent(cmdLine)) {
      printHelp(System.out);
      System.exit(0);
    }

    // If version is asked, print it and exit.
    if (versionOpt.isPresent(cmdLine)) {
      printVersion(System.out);
      System.exit(0);
    }

    final File outputDir = new File(outDirOpt.getValue(cmdLine));
    if (!outputDir.isDirectory()) {
      System.err.println("Invalid output directory \"" + outputDir.getPath()
          + "\". No such directory");
      System.exit(1);
    }

    String reportDirName = reportDirOpt.getValue(cmdLine);
    final File reportDir;
    if (reportDirName != null) {
      reportDir = new File(reportDirName);
      if (!reportDir.isDirectory()) {
        System.err.println("Invalid report directory \"" + reportDir.getPath()
            + "\". No such directory");
        System.exit(1);
      }
    } else {
      reportDir = null;
    }

    final String[] tests = splitOption(includeTestOpt.getValue(cmdLine));
    final String[] excludeTests = splitOption(excludeTestOpt.getValue(cmdLine));
    final String[] confs = splitOption(includeConfOpt.getValue(cmdLine));
    final String[] excludeConfs = splitOption(excludeConfOpt.getValue(cmdLine));

    setTestOptions(new TestOptions(mindcCmdOpt.getValue(cmdLine),
        srcPathOpt.getValue(cmdLine), outputDir, reportDir, tests,
        excludeTests, confs, excludeConfs));

    return cmdLine.getArguments();
  }

  public static void main(String[] args) {

    Launcher launcher = new Launcher();
    List<String> testSets;
    try {
      testSets = launcher.init(args);
    } catch (InvalidCommandLineException e) {
      System.err.println(e.getMessage());
      launcher.printHelp(System.err);
      System.exit(e.exitValue);
      return;
    }
    if (testSets.isEmpty()) {
      testSets.add(DEFAULT_TEST_SET);
    }

    boolean hasFailure = false;
    for (String testSet : testSets) {
      File testSetFile = new File(testSet);
      if (!testSetFile.exists()) {
        System.err.println("Unknown file \"" + testSetFile + "\".");
        System.exit(1);
      }
      try {
        TestSetResult result = launcher.runTestSet(testSetFile);
        launcher.printResult(result, System.out);
        launcher.dumpReport(result);
        if (result.getState() == State.FAILURE) hasFailure = true;
      } catch (TestException e) {
        System.err.println("Invalid testset file \"" + testSetFile + "\": "
            + e.getMessage());
        System.exit(1);
      } catch (FileNotFoundException e) {
        System.err.println("Cqn't write report of \"" + testSetFile + "\": "
            + e.getMessage());
        System.exit(1);
      }
    }
    if (hasFailure)
      System.exit(1);
    else
      System.exit(0);
  }

  public void setTestOptions(TestOptions testOptions) {
    this.testOptions = testOptions;
  }

  public TestSetResult runTestSet(File testSet) throws TestException {
    TestSet t = initLoader().load(testSet);

    TestRunner runner = getRunner(t);

    return runner.run(t, testOptions);
  }

  public void printResult(TestSetResult result, PrintStream ps) {
    ps.println("===============================================");
    ps.println("  Test Set \"" + result.getTestElem().getName() + "\"");

    if (result.getState() == State.FAILURE) {
      ps.println("  Following tests fail:");
      for (TestResult testResult : result.getSubResults()) {
        if (testResult.getState() != State.FAILURE) continue;
        ps.print("  * Test: \"" + testResult.getTestElem().getName()
            + "\" FAILS");
        int nbFailure = testResult.getNbFailure();
        int nbConfig = testResult.getNbConfig();
        if (nbConfig == nbFailure) {
          ps.println(" (All config)");
        } else {
          ps.println(" (" + nbFailure + " of " + nbConfig + " config"
              + ((nbConfig > 0) ? "s" : "") + " fail)");
          for (ConfigResult configResult : testResult.getSubResults()) {
            if (configResult.getState() != State.FAILURE) continue;
            ps.print("    - Config: \"" + configResult.getTestElem().getName()
                + "\" FAILS");
          }
        }
      }
      ps.println();
    }
    ps.println("  Tests run: " + result.getNbTests() + ", Failures: "
        + result.getNbFailure() + ", Skipped: " + result.getNbSkipped());
    ps.println("  Time elapsed: " + result.formatExecutionTime());
    ps.println("===============================================");
  }

  public void dumpReport(TestSetResult result) throws FileNotFoundException {
    if (testOptions.getReportDir() != null) {
      new MindTestTextDumper(testOptions).dumpTestSet(result);
      new MindTestXMLDumper(testOptions).dumpTestSet(result);
      new JUnitXMLDumper(testOptions).dumpTestSet(result);
    }
  }

  protected synchronized TestSetLoader initLoader() {
    if (loader == null) {
      TestSetParser tsp = new TestSetParser();
      XMLCommandLoader xcl = new XMLCommandLoader();
      ConfigRefLoader crl = new ConfigRefLoader();
      ParameterSpreadingLoader psl = new ParameterSpreadingLoader();

      loader = psl;
      psl.clientLoaderItf = crl;
      crl.clientLoaderItf = xcl;
      xcl.clientLoaderItf = tsp;

      tsp.nodeFactoryItf = new XMLNodeFactoryImpl();
    }
    return loader;
  }

  protected TestRunner getRunner(TestSet testSet) throws TestException {
    if (testSet.getRunner() == null) return new BasicTestRunner();

    try {
      return getClass().getClassLoader().loadClass(testSet.getRunner())
          .asSubclass(TestRunner.class).newInstance();
    } catch (ClassNotFoundException e) {
      throw new TestException("Runner \"" + testSet.getRunner()
          + "\" not found", e);
    } catch (InstantiationException e) {
      throw new TestException("Invalid runner class \"" + testSet.getRunner()
          + "\"", e);
    } catch (IllegalAccessException e) {
      throw new TestException("Invalid runner class \"" + testSet.getRunner()
          + "\"", e);
    } catch (ClassCastException e) {
      throw new TestException("Invalid runner class \"" + testSet.getRunner()
          + "\"", e);
    }
  }

  protected static String[] splitOption(String test) {
    final String[] tests;
    if (test != null) {
      tests = test.split(",");
      for (int i = 0; i < tests.length; i++) {
        tests[i] = tests[i].trim();
      }
    } else {
      tests = null;
    }
    return tests;
  }

  protected void printHelp(final PrintStream ps) {
    ps.println("Usage: " + COMMAND_NAME + " [OPTIONS] (<test set)+");
    ps.println("  where <test set> is a test set XML description file.");
    ps.println();
    ps.println("Available options are :");
    int maxCol = 0;

    for (final CmdOption opt : options.getOptions()) {
      final int col = 2 + opt.getPrototype().length();
      if (col > maxCol) maxCol = col;
    }
    for (final CmdOption opt : options.getOptions()) {
      final StringBuffer sb = new StringBuffer("  ");
      sb.append(opt.getPrototype());
      while (sb.length() < maxCol)
        sb.append(' ');
      sb.append("  ").append(opt.getDescription());
      ps.println(sb);
    }
  }

  protected void printVersion(final PrintStream ps) {
    String pkgVersion = this.getClass().getPackage().getImplementationVersion();
    if (pkgVersion == null) pkgVersion = "unknown";
    ps.println(COMMAND_NAME + " version " + pkgVersion);
  }

  protected static List<String> parsePathList(String paths) {
    final List<String> l = new ArrayList<String>();
    int index = paths.indexOf(File.pathSeparatorChar);
    while (index != -1) {
      l.add(paths.substring(0, index));
      paths = paths.substring(index + 1);
      index = paths.indexOf(File.pathSeparatorChar);
    }
    l.add(paths);
    return l;
  }

  // ---------------------------------------------------------------------------
  // Internal classes
  // ---------------------------------------------------------------------------

  /**
   * Exception thrown when an error on the command line has been detected.
   */
  public static class InvalidCommandLineException extends Exception {

    protected final int exitValue;

    /**
     * @param message detail message.
     * @param exitValue exit value.
     */
    public InvalidCommandLineException(final String message, final int exitValue) {
      super(message);
      this.exitValue = exitValue;
    }

    /**
     * @return the exit value.
     */
    public int getExitValue() {
      return exitValue;
    }
  }

  /** Set of available command-line options. */
  public static class Options {
    protected final Set<CmdOption>             optionSet          = new LinkedHashSet<CmdOption>();
    protected final Map<String, CmdOption>     optionsByShortName = new HashMap<String, CmdOption>();
    protected final Map<String, CmdOption>     optionsByLongName  = new HashMap<String, CmdOption>();
    protected final Map<String, CmdProperties> optionsByPrefix    = new HashMap<String, CmdProperties>();

    /**
     * Add an option
     * 
     * @param option an option to add.
     */
    public void addOption(final CmdOption option) {
      if (option instanceof CmdProperties) {
        final CmdOption prevOpt = optionsByPrefix.put(option.shortName,
            (CmdProperties) option);
        if (prevOpt != null || optionsByShortName.containsKey(option.shortName)) {
          throw new IllegalArgumentException("short name '" + option.shortName
              + "' already used");
        }
      } else {
        if (option.shortName != null) {
          final CmdOption prevOpt = optionsByShortName.put(option.shortName,
              option);
          if (prevOpt != null || optionsByPrefix.containsKey(option.shortName)) {
            throw new IllegalArgumentException("short name '"
                + option.shortName + "' already used");
          }
        }
        if (option.longName != null) {
          final CmdOption prevOpt = optionsByLongName.put(option.longName,
              option);
          if (prevOpt != null) {
            throw new IllegalArgumentException("long name '" + option.longName
                + "' already used");
          }
        }
      }

      optionSet.add(option);
    }

    /**
     * Add a set of options
     * 
     * @param options the options to add.
     */
    public void addOptions(final CmdOption... options) {
      for (final CmdOption option : options) {
        addOption(option);
      }
    }

    /** @return the available options. */
    public Collection<CmdOption> getOptions() {
      return optionSet;
    }

    CmdOption getByShortName(final String shortName) {
      return optionsByShortName.get(shortName);
    }

    CmdOption getByLongName(final String longName) {
      return optionsByLongName.get(longName);
    }

    CmdOption getByName(final String name) {
      final String prefix = name.substring(0, 1);

      CmdOption option = optionsByPrefix.get(prefix);
      if (option != null) return option;

      option = optionsByShortName.get(name);
      if (option != null) return option;

      return optionsByLongName.get(name);
    }
  }

  /**
   * A command line is the result of parsing a list of string arguments with a
   * set of options.
   */
  public static class CommandLine {
    protected final Options                options;
    protected final Map<CmdOption, Object> optionValues = new LinkedHashMap<CmdOption, Object>();
    protected final List<String>           arguments    = new ArrayList<String>();

    /**
     * Parse the given arguments to a CommandLine.
     * 
     * @param options the available options.
     * @param allowUnknownOption if true, unrecognized options will be added to
     *          list of arguments.
     * @param args the list of argument to parse.
     * @return a CommandLine object.
     * @throws InvalidCommandLineException if the list of argument is invalid.
     */
    public static CommandLine parseArgs(final Options options,
        final boolean allowUnknownOption, final String... args)
        throws InvalidCommandLineException {
      final CommandLine cmdLine = new CommandLine(options);

      for (final String arg : args) {
        if (arg.startsWith("-")) {

          final String argName;
          final String argValue;

          boolean longName;
          final int startIndex;
          if (arg.startsWith("--")) {
            startIndex = 2;
            longName = true;
          } else {
            startIndex = 1;
            longName = false;
          }

          final int index = arg.indexOf('=');
          if (index == -1) {
            argName = arg.substring(startIndex);
            argValue = null;
          } else {
            if (index < startIndex + 1) {
              throw new InvalidCommandLineException("Invalid option '" + arg
                  + "'", 1);
            }
            argName = arg.substring(startIndex, index);
            argValue = arg.substring(index + 1);
          }

          final CmdOption opt;
          if (longName)
            opt = cmdLine.options.getByLongName(argName);
          else
            opt = cmdLine.options.getByName(argName);

          if (opt == null) {
            if (allowUnknownOption) {
              cmdLine.arguments.add(arg);
            } else {
              throw new InvalidCommandLineException("Unknown option '"
                  + argName + "'", 1);
            }
          } else {
            if (opt instanceof CmdFlag) {
              if (argValue != null) {
                throw new InvalidCommandLineException("Invalid option '"
                    + argName + "' do not accept value", 1);
              }

              ((CmdFlag) opt).setPresent(cmdLine);

            } else if (opt instanceof CmdProperties) {
              if (argValue == null) {
                throw new InvalidCommandLineException("Invalid option '"
                    + argName + "' expects a value", 1);
              }
              ((CmdProperties) opt).setValue(cmdLine, argName.substring(1),
                  argValue);
            } else { // opt instanceof CmdArgument
              if (argValue == null) {
                throw new InvalidCommandLineException("Invalid option '"
                    + argName + "' expects a value", 1);
              }
              ((CmdArgument) opt).setValue(cmdLine, argValue);
            }
          }
        } else {
          cmdLine.arguments.add(arg);
        }
      }
      return cmdLine;
    }

    protected CommandLine(final Options options) {
      this.options = options;
    }

    protected Object setOptionValue(final CmdOption option, final Object value) {
      return optionValues.put(option, value);
    }

    /** @return the list of arguments. */
    public List<String> getArguments() {
      return arguments;
    }

    /**
     * @param option an option.
     * @return <code>true</code> is the given option is present on this command
     *         line.
     */
    public boolean isOptionPresent(final CmdOption option) {
      return optionValues.containsKey(option);
    }

    Object getOptionValue(final CmdOption option) {
      return optionValues.get(option);
    }
  }

  /**
   * Base class of command line options.
   */
  public abstract static class CmdOption {
    protected final String shortName;
    protected final String longName;
    protected final String description;

    /**
     * @param shortName the short name of the option. Must have one and only one
     *          character. May be <code>null</code>.
     * @param longName the long name of the option. Must have more than one
     *          character. May be <code>null</code>.
     * @param description the description of the option (used to generate help
     *          message).
     */
    public CmdOption(final String shortName, final String longName,
        final String description) {
      if (shortName == null && longName == null)
        throw new IllegalArgumentException("Invalid option names");
      if (shortName != null && shortName.length() > 1)
        throw new IllegalArgumentException("Invalid shortName");
      if (longName != null && longName.length() <= 1)
        throw new IllegalArgumentException("Invalid longName");

      this.shortName = shortName;
      this.longName = longName;
      this.description = description;
    }

    /** @return the prototype of the options (used to generate help message). */
    public String getPrototype() {
      String desc;
      if (shortName != null) {
        desc = "-" + shortName;
        if (longName != null) {
          desc += ", --" + longName;
        }
      } else {
        desc = "--" + longName;
      }
      return desc;
    }

    /** @return the short name of the option. */
    public String getShortName() {
      return shortName;
    }

    /** @return the long name of the option. */
    public String getLongName() {
      return longName;
    }

    /** @return the description of the option. */
    public String getDescription() {
      return description;
    }

    /**
     * @param commandLine a command-line.
     * @return <code>true</code> if this option is present on the given
     *         command-line
     */
    public boolean isPresent(final CommandLine commandLine) {
      return commandLine.isOptionPresent(this);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) return true;

      if (!(obj instanceof CmdOption)) return false;

      final CmdOption opt = (CmdOption) obj;
      if (shortName == null) {
        return opt.shortName == null && opt.longName.equals(longName);
      } else {
        return shortName.equals(opt.shortName);
      }
    }

    @Override
    public int hashCode() {
      if (shortName == null)
        return longName.hashCode();
      else
        return shortName.hashCode();
    }
  }

  /**
   * An option that may be present or not on a command line.
   */
  public static class CmdFlag extends CmdOption {

    /** @see CmdOption#CmdOption(String, String, String) */
    public CmdFlag(final String shortName, final String longName,
        final String description) {
      super(shortName, longName, description);
    }

    void setPresent(final CommandLine commandLine) {
      commandLine.setOptionValue(this, "");
    }
  }

  /**
   * A command line option that have a value.
   */
  public static class CmdArgument extends CmdOption {
    protected final String  argDesc;
    protected final String  defaultValue;
    protected final boolean allowMultiple;

    /**
     * @param shortName the short name of the option. Must have one and only one
     *          character. May be <code>null</code>.
     * @param longName the long name of the option. Must have more than one
     *          character. May be <code>null</code>.
     * @param description the description of the option (used to generate help
     *          message).
     * @param argDesc the description of the argument value (used to generate
     *          help message).
     * @param defaultValue the default value of this option. May be
     *          <code>null</code>.
     * @param allowMultiple if <code>true</code>, this option can be specified
     *          several time on a command-line. In that case, the last
     *          occurrence is used.
     */
    public CmdArgument(final String shortName, final String longName,
        final String description, final String argDesc,
        final String defaultValue, final boolean allowMultiple) {
      super(shortName, longName, (defaultValue == null)
          ? description
          : description + " (default is '" + defaultValue + "')");
      this.argDesc = argDesc;
      this.defaultValue = defaultValue;
      this.allowMultiple = allowMultiple;
    }

    /**
     * Constructor for CmdArgument that has no default value and that does not
     * allow multiple occurrences.
     * 
     * @param shortName the short name of the option. Must have one and only one
     *          character. May be <code>null</code>.
     * @param longName the long name of the option. Must have more than one
     *          character. May be <code>null</code>.
     * @param description the description of the option (used to generate help
     *          message).
     * @param argDesc the description of the argument value (used to generate
     *          help message).
     */
    public CmdArgument(final String shortName, final String longName,
        final String description, final String argDesc) {
      this(shortName, longName, description, argDesc, null, false);
    }

    void setValue(final CommandLine commandLine, final String value)
        throws InvalidCommandLineException {
      if (value == null) return;
      final Object prevValue = commandLine.setOptionValue(this, value);
      if (!allowMultiple && prevValue != null) {
        throw new InvalidCommandLineException("'" + longName
            + "' can't be specified several times.", 1);
      }
    }

    /**
     * Return the value of this option in the given command-line.
     * 
     * @param commandLine a command line.
     * @return the value of this option in the given command-line, or the
     *         {@link #getDefaultValue() default value}, or <code>null</code> if
     *         the given command line does not contains this option and this
     *         option has no default value.
     */
    public String getValue(final CommandLine commandLine) {
      final String optionValue = (String) commandLine.getOptionValue(this);
      return optionValue == null ? defaultValue : optionValue;
    }

    /** @return the default value. */
    public String getDefaultValue() {
      return defaultValue;
    }

    @Override
    public String getPrototype() {
      String desc;
      if (shortName != null) {
        desc = "-" + shortName + "=" + argDesc;
        if (longName != null) {
          desc += ", --" + longName;
        }
      } else {
        desc = "--" + longName + "=" + argDesc;
      }
      return desc;
    }

    @Override
    public String getDescription() {
      if (allowMultiple)
        return super.getDescription()
            + ". This option may be specified several times.";
      else
        return super.getDescription();
    }
  }

  /**
   * An option that associate name to value.
   */
  public static class CmdProperties extends CmdOption {
    protected final String argNameDesc;
    protected final String argValueDesc;

    /**
     * @param shortName the short name of the option. Must have one and only one
     *          character. May be <code>null</code>.
     * @param description the description of the option (used to generate help
     *          message).
     * @param argNameDesc the description of the argument name (used to generate
     *          help message).
     * @param argValueDesc the description of the argument name (used to
     *          generate help message).
     */
    public CmdProperties(final String shortName, final String description,
        final String argNameDesc, final String argValueDesc) {
      super(shortName, null, description
          + ". This option may be specified several times.");
      this.argNameDesc = argNameDesc;
      this.argValueDesc = argValueDesc;
    }

    @Override
    public String getPrototype() {
      return "-" + shortName + argNameDesc + "=" + argValueDesc;
    }

    @SuppressWarnings("unchecked")
    void setValue(final CommandLine commandLine, final String name,
        final String value) throws InvalidCommandLineException {
      if (name == null || value == null) return;
      Map<String, String> values = (Map<String, String>) commandLine
          .getOptionValue(this);
      if (values == null) {
        values = new HashMap<String, String>();
        commandLine.setOptionValue(this, values);
      }
      values.put(name, value);
    }

    /**
     * Returns the value of this option in the given command-line.
     * 
     * @param commandLine a command-line.
     * @return A map associating name to value, or <code>null</code> if this
     *         option is not specified on the given command line.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getValue(final CommandLine commandLine) {
      return (Map<String, String>) commandLine.getOptionValue(this);
    }
  }

  /**
   * An option that have a value and that may be specified several time on a
   * command-line. The resulting option value is the concatenation of the values
   * of each occurrence of this option.
   */
  public static class CmdAppendOption extends CmdArgument {

    protected final String separator;

    /**
     * Constructor for CmdAppendOption that has no default value and that use
     * <code>" "</code> as separator.
     * 
     * @param shortName the short name of the option. Must have one and only one
     *          character. May be <code>null</code>.
     * @param longName the long name of the option. Must have more than one
     *          character. May be <code>null</code>.
     * @param description the description of the option (used to generate help
     *          message).
     * @param argDesc the description of the argument value (used to generate
     *          help message).
     */
    public CmdAppendOption(final String shortName, final String longName,
        final String description, final String argDesc) {
      this(shortName, longName, description, argDesc, null, " ");
    }

    /**
     * @param shortName the short name of the option. Must have one and only one
     *          character. May be <code>null</code>.
     * @param longName the long name of the option. Must have more than one
     *          character. May be <code>null</code>.
     * @param description the description of the option (used to generate help
     *          message).
     * @param argDesc the description of the argument value (used to generate
     *          help message).
     * @param defaultValue the default value of this option. May be
     *          <code>null</code>.
     * @param separator the string used to separate individual value.
     */
    public CmdAppendOption(final String shortName, final String longName,
        final String description, final String argDesc,
        final String defaultValue, final String separator) {
      super(shortName, longName, description, argDesc, defaultValue, true);
      this.separator = separator;
    }

    @Override
    void setValue(final CommandLine commandLine, final String value)
        throws InvalidCommandLineException {
      if (value == null) return;
      final String prevValue = (String) commandLine.getOptionValue(this);

      if (prevValue == null) {
        commandLine.setOptionValue(this, value);
      } else {
        commandLine.setOptionValue(this, prevValue + separator + value);
      }
    }
  }

  /**
   * An option that have a value and that may be specified several time on a
   * command-line. The resulting option value is the concatenation of the values
   * of each occurrence of this option separated by {@link File#pathSeparator}.
   */
  public static class CmdPathOption extends CmdAppendOption {

    /**
     * @param shortName the short name of the option. Must have one and only one
     *          character. May be <code>null</code>.
     * @param longName the long name of the option. Must have more than one
     *          character. May be <code>null</code>.
     * @param description the description of the option (used to generate help
     *          message).
     * @param argDesc the description of the argument value (used to generate
     *          help message).
     */
    public CmdPathOption(final String shortName, final String longName,
        final String description, final String argDesc) {
      super(shortName, longName, description, argDesc, null, File.pathSeparator);
    }

    /**
     * @param commandLine a command-line
     * @return the value of this option on the given command-line as a list of
     *         String, or <code>null</code>.
     */
    public List<String> getPathValue(final CommandLine commandLine) {
      final String value = getValue(commandLine);
      if (value == null)
        return null;
      else
        return parsePathList(value);
    }
  }
}

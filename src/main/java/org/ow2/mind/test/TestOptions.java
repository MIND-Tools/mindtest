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
import java.util.HashSet;
import java.util.Set;

public class TestOptions {

  final String      mindcCmd;
  final String      srcPath;
  final File        outputDir;
  final File        reportDir;

  final Set<String> includeTests;
  final Set<String> excludeTests;

  final Set<String> includeConfs;
  final Set<String> excludeConfs;

  /**
   * @param outputDir
   * @param includeTests
   * @param excludeTests
   * @param includeConfs
   * @param excludeConfs
   */
  public TestOptions(String mindcCmd, final String srcPath,
      final File outputDir, final File reportDir, String[] includeTests,
      String[] excludeTests, String[] includeConfs, String[] excludeConfs) {

    this.mindcCmd = mindcCmd;
    this.srcPath = srcPath;
    this.outputDir = outputDir;
    this.reportDir = reportDir;

    this.includeTests = toSet(includeTests);
    this.excludeTests = toSet(excludeTests);
    this.includeConfs = toSet(includeConfs);
    this.excludeConfs = toSet(excludeConfs);
  }

  private Set<String> toSet(String[] array) {
    if (array == null) return null;
    Set<String> s = new HashSet<String>(array.length);
    for (int i = 0; i < array.length; i++) {
      s.add(array[i]);
    }
    return s;
  }

  public boolean isRunnableTest(String testName) {
    if (includeTests != null && !includeTests.contains(testName)) return false;

    if (excludeTests != null && excludeTests.contains(testName)) return false;

    return true;
  }

  public boolean isRunnableConf(String confName) {
    if (includeConfs != null && !includeConfs.contains(confName)) return false;

    if (excludeConfs != null && excludeConfs.contains(confName)) return false;

    return true;
  }

  public File getOutputDir() {
    return outputDir;
  }

  public String getMindcCmd() {
    return mindcCmd;
  }

  public String getSrcPath() {
    return srcPath;
  }

  public File getReportDir() {
    return reportDir;
  }
  
  public Set<String> getIncludeTests() {
    return includeTests;
  }

  public Set<String> getExcludeTests() {
    return excludeTests;
  }

  public Set<String> getIncludeConfs() {
    return includeConfs;
  }

  public Set<String> getExcludeConfs() {
    return excludeConfs;
  }
  
  
}

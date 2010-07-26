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

import org.ow2.mind.test.ast.Command;
import org.ow2.mind.test.ast.Config;
import org.ow2.mind.test.ast.Mindc;
import org.ow2.mind.test.ast.Test;
import org.ow2.mind.test.ast.TestSet;

public class ParameterSpreadingLoader implements TestSetLoader {

  // ---------------------------------------------------------------------------
  // Client interface
  // ---------------------------------------------------------------------------

  /** The {@link TestSetLoader} client loader. */
  public TestSetLoader clientLoaderItf;

  // ---------------------------------------------------------------------------
  // Implementation of the TestSetLoader interface
  // ---------------------------------------------------------------------------

  public TestSet load(File testfile) throws TestException {
    TestSet testSet = clientLoaderItf.load(testfile);
    for (Test test : testSet.getTests()) {
      String adl = test.getAdl();
      for (Config config : test.getConfigs()) {
        String targetDescriptor = config.getTargetDescriptor();
        for (Command cmd : config.getCommands()) {
          if (cmd instanceof Mindc) {
            Mindc mindc = ((Mindc) cmd);
            if (mindc.getAdl() == null) {
              if (adl == null) {
                throw new TestException("At " + test.astGetSource()
                    + " missing ADL name for mindc command at \""
                    + cmd.astGetSource() + "\".");
              }
              mindc.setAdl(adl);
            }

            if (mindc.getTargetDescriptor() == null) {
              mindc.setTargetDescriptor(targetDescriptor);
            }

            if (mindc.getExecName() == null) {
              mindc.setExecName(testSet.getName() + "_" + test.getName() + "_"
                  + config.getName() + "_" + mindc.getAdl().replace('.', '_'));
            }
          }
        }
      }
    }
    return testSet;
  }
}

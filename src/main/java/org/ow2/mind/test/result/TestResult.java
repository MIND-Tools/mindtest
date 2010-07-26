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

package org.ow2.mind.test.result;

import java.util.Collections;
import java.util.List;

import org.ow2.mind.test.ast.Config;
import org.ow2.mind.test.ast.Test;
import org.ow2.mind.test.result.AbstractResult.State;

public class TestResult extends AbstractCompoundResult<Test, ConfigResult> {

  public TestResult(Test test, List<ConfigResult> configResults) {
    super(test, configResults);
  }

  public TestResult(Test test) {
    super(test);
  }
  
  public int getNbConfig() {
    return testElem.getConfigs().length;
  }
  
  @Override
  protected int getNbSubElemt() {
    return getNbConfig();
  }
}

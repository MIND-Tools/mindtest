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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.NodeUtil;
import org.ow2.mind.test.ast.Config;
import org.ow2.mind.test.ast.Test;
import org.ow2.mind.test.ast.TestSet;

public class ConfigRefLoader implements TestSetLoader{

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

    Config[] configs = testSet.getConfigs();
    Map<String, Config> configMap = new LinkedHashMap<String, Config>(configs.length);
    for (Config config : configs) {
      configMap.put(config.getName(), config);
    }

    for (Test test : testSet.getTests()) {
      processTest(test, configMap);
    }

    return testSet;
  }

  protected void processTest(Test test, Map<String, Config> configMap)
      throws TestException {
    String configRefs = test.getConfigRefs();
    if (configRefs == null) return;

    Config[] localConfigs = test.getConfigs();
    Set<String> localConfigNames = new HashSet<String>(localConfigs.length);
    for (Config localConfig : localConfigs) {
      localConfigNames.add(localConfig.getName());
    }

    if (configRefs.equalsIgnoreCase("all")) {
      // references all config. Add all global configs (except if a local 
      // config exists with the same name).
      for (Config config : configMap.values()) {
        if (localConfigNames.contains(config.getName())) continue;
        test.addConfig(NodeUtil.cloneTree(config));
      }
    } else {
      for (String configRef : configRefs.split(",")) {
        if (localConfigNames.contains(configRef)) {
          throw new TestException("At " + test.astGetSource()
              + " cannot reference config \"" + configRef
              + "\". A config with the same name is defined locally");
        }

        Config config = configMap.get(configRef.trim());
        if (config == null) {
          throw new TestException("At " + test.astGetSource()
              + " unknown config \"" + configRef + "\".");
        }
        
        test.addConfig(NodeUtil.cloneTree(config));
      }
    }
  }
}

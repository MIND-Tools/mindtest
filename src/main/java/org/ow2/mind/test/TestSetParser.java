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
import java.io.FileInputStream;
import java.io.IOException;

import org.objectweb.fractal.adl.Parser;
import org.objectweb.fractal.adl.ParserException;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.objectweb.fractal.adl.xml.XMLParser;
import org.ow2.mind.test.ast.TestSet;
import org.xml.sax.SAXParseException;

public class TestSetParser implements TestSetLoader {

  // ---------------------------------------------------------------------------
  // Client interface
  // ---------------------------------------------------------------------------

  /** The {@link XMLNodeFactory} client interface of this component. */
  public XMLNodeFactory nodeFactoryItf;

  // ---------------------------------------------------------------------------
  // Implementation of the TestSetLoader interface
  // ---------------------------------------------------------------------------

  public TestSet load(File testfile) throws TestException {
    Parser parser = new XMLParser(true, nodeFactoryItf);
    try {
      return (TestSet) parser.parse(new FileInputStream(testfile),
          testfile.getPath());
    } catch (final IOException e) {
      throw new TestException("IO error while reading file " + testfile, e);
    } catch (final ParserException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof SAXParseException) {
        final SAXParseException spe = (SAXParseException) cause;
        throw new TestException("At " + testfile + ":" + spe.getLineNumber()
            + " Parse error: " + spe.getMessage());
      }
      throw new TestException("At " + testfile + "Parse error: "
          + cause.getMessage(), cause);
    }
  }
}

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
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.ow2.mind.test.TestOptions;
import org.ow2.mind.test.result.TestSetResult;

public abstract class AbstractXMLDumper {

  protected StringBuilder sb;
  protected int           indent;
  final TestOptions       options;

  private List<String>    elems;

  public AbstractXMLDumper(TestOptions options) {
    this.options = options;
  }

  public void dumpTestSet(TestSetResult testSetResult)
      throws FileNotFoundException {
    sb = new StringBuilder();
    elems = new ArrayList<String>();
    indent = 0;
    visitTestSet(testSetResult);

    File outputFile = getReportFile(testSetResult);
    PrintStream ps = new PrintStream(outputFile);
    ps.print(sb.toString());
    ps.close();
  }

  protected abstract File getReportFile(TestSetResult testSetResult);

  protected abstract void visitTestSet(TestSetResult testSetResult);

  protected void element(String elementName, String... attributes) {
    indent();
    sb.append("<").append(elementName);
    for (String attribute : attributes) {
      sb.append(" ").append(attribute);
    }
    sb.append("/>\n");
  }

  protected String escape(String s) {
    return StringEscapeUtils.escapeXml(s);
  }
  
  protected String attribute(String name, int value) {
    return attribute(name, Integer.toString(value));
  }

  protected String attribute(String name, long value) {
    return attribute(name, Long.toString(value));
  }

  protected String attribute(String name, double value) {
    return attribute(name, Double.toString(value));
  }

  protected String attribute(String name, String value) {
    return name + "=\"" + escape(value) + "\"";
  }

  protected void startElement(String elementName, List<String> attributes) {
    startElement(elementName, attributes.toArray(new String[attributes.size()]));
  }

  protected void startElement(String elementName, String... attributes) {
    indent();
    sb.append("<").append(elementName);
    int attrLength = 0;
    for (String attribute : attributes) {
      attrLength += attribute.length() + 1;
    }
    if (attrLength + indent > 80) {
      sb.append(" ").append(attributes[0]).append("\n");
      indent += 4;
      for (int i = 1; i < attributes.length; i++) {
        indent();
        sb.append(attributes[i]);
        if (i < attributes.length - 1) sb.append("\n");
      }
      indent -= 4;
    } else {
      for (String attribute : attributes) {
        sb.append(" ").append(attribute);
      }
    }
    sb.append(">\n");
    indent += 2;
    elems.add(elementName);
  }

  protected void endElement() {
    if (elems.size() < 1) {
      throw new IllegalStateException("Element stack is empty");
    }
    indent -= 2;
    indent();
    String elementName = elems.remove(elems.size() - 1);
    sb.append("</").append(elementName).append(">\n");
  }

  protected void indent() {
    for (int i = 0; i < indent; i++) {
      sb.append(" ");
    }
  }

}
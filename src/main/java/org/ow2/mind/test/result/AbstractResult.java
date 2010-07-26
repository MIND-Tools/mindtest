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

import java.util.List;

import org.objectweb.fractal.adl.Node;
import org.omg.PortableInterceptor.SUCCESSFUL;

public abstract class AbstractResult<T> {

  public enum State {SUCCESS, FAILURE, SKIPPED};
  
  protected final T       testElem;
  protected final State state;
  protected final long    executionTime;

  /**
   * @param success
   * @param executionTime
   */
  public AbstractResult(T testElem, State state, long executionTime) {
    this.testElem = testElem;
    this.state = state;
    this.executionTime = executionTime;
  }

  public T getTestElem() {
    return testElem;
  }

  /**
   * @return the state
   */
  public State getState() {
    return state;
  }

  /**
   * @return the executionTime
   */
  public long getExecutionTime() {
    return executionTime;
  }

  public String formatExecutionTime() {
    long ms = executionTime % 1000;
    long s = (executionTime / 1000) % 60;
    long m = (executionTime / 60000) % 60;
    long h = (executionTime / 3600000);
    
    String time = "";
    if (h >0) time += h +" h ";
    if (m > 0) time += m + " min ";
    time += s + "." + ms + " sec";
    
    return time;
  }

  protected static State isSuccessfull(List<? extends AbstractResult> results) {
    for (AbstractResult result : results) {
      if (result.state == State.FAILURE) return State.FAILURE;
    }
    return State.SUCCESS;
  }

  protected static long cummulativeExecTime(
      List<? extends AbstractResult> results) {
    long execTime = 0;
    for (AbstractResult result : results) {
      execTime += result.getExecutionTime();
    }
    return execTime;
  }
}

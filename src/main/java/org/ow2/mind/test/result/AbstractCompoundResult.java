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

public abstract class AbstractCompoundResult<T, SubResult extends AbstractResult<?>> extends AbstractResult<T>{

  final int nbFailure;
  final int nbSkipped;
  final int nbSuccess;
  final List<SubResult> subResults;

  public AbstractCompoundResult(T testElem,
      List<SubResult> subResults) {
    super(testElem, isSuccessfull(subResults), cummulativeExecTime(subResults));
    this.subResults = subResults;
    
    int nbFailure = 0;
    int nbSuccess = 0;
    int nbSkipped = 0;
    
    for (SubResult subResult : subResults) {
      switch (subResult.getState()) {
        case FAILURE : nbFailure++; break;
        case SUCCESS : nbSuccess++; break;
        case SKIPPED : nbSkipped++; break;
      }
    }
    
    this.nbFailure = nbFailure;
    this.nbSuccess = nbSuccess;
    this.nbSkipped = nbSkipped;
  }

  /**
   * To be used only if corresponding testElem has been skipped.
   * @param testElem
   */
  public AbstractCompoundResult(T testElem) {
    super(testElem, State.SKIPPED, 0);
    
    nbFailure = 0;
    nbSuccess = 0;
    nbSkipped = getNbSubElemt();
    subResults = Collections.emptyList();
  }
  
  /**
   * @return the nbFailure
   */
  public int getNbFailure() {
    return nbFailure;
  }

  /**
   * @return the nbSkipped
   */
  public int getNbSkipped() {
    return nbSkipped;
  }

  /**
   * @return the nbSuccess
   */
  public int getNbSuccess() {
    return nbSuccess;
  }
  
  /**
   * @return the subResults
   */
  public List<SubResult> getSubResults() {
    return subResults;
  }

  protected abstract int getNbSubElemt();
}

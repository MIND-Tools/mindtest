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

import org.ow2.mind.test.ast.Command;

public class CommandResult extends AbstractResult<Command> {

  protected final String executedCommand;
  protected final List<String> commandArgs;
  protected final int returnCode;
  protected final String output;
  
  public CommandResult(Command command, List<String> cmd, int returnCode, String output, long execTime) {
    super(command, (returnCode == 0)? State.SUCCESS : State.FAILURE, execTime);
    this.executedCommand = cmd.get(0);
    this.commandArgs = cmd.subList(1, cmd.size());
    this.returnCode = returnCode;
    this.output = output;
  }

  /**
   * @return the executedCommand
   */
  public String getExecutedCommand() {
    return executedCommand;
  }
  
  /**
   * @return the commandArgs
   */
  public List<String> getCommandArgs() {
    return commandArgs;
  }
  
  /**
   * @return the returnCode
   */
  public int getReturnCode() {
    return returnCode;
  }

  /**
   * @return the output
   */
  public String getOutput() {
    return output;
  }
}

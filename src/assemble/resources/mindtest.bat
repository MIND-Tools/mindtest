@REM  This file is part of "Mind Compiler" is free software: you can redistribute 
@REM  it and/or modify it under the terms of the GNU Lesser General Public License 
@REM  as published by the Free Software Foundation, either version 3 of the 
@REM  License, or (at your option) any later version.
@REM 
@REM  This program is distributed in the hope that it will be useful, but WITHOUT 
@REM  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
@REM  FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
@REM  details.
@REM 
@REM  You should have received a copy of the GNU Lesser General Public License
@REM  along with this program.  If not, see <http://www.gnu.org/licenses/>.
@REM 
@REM  Contact: mind@ow2.org
@REM 
@REM  Authors: Edine Coly (edine.coly@mail.sogeti.com)
@REM  Contributors: 
@REM -----------------------------------------------------------------------------
@REM Mind-Test batch script ${project.version}
@REM
@REM Required ENV vars:
@REM ------------------
@REM   JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM -----------------
@REM   MIND_TEST_HOME - location of mindtest's installed home dir
@REM   MIND_TEST_OPTS - parameters passed to the Java VM running mindtest

@echo off

@REM ==== CHECK JAVA_HOME ===
if not "%JAVA_HOME%" == "" goto OkJHome
echo.
echo ERROR: JAVA_HOME not found in your environment.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:OkJHome
@REM ==== CHECK JAVA_HOME_EXE ===
if exist "%JAVA_HOME%\bin\java.exe" goto OkJHomeExe

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:OkJHomeExe
@REM ==== CHECK MIND_TEST_HOME ===
@REM use the batch path to determine MIND_TEST_HOME if not defined.
pushd %~dp0..\
set MIND_TEST_ROOT=%cd%
popd

if "%MIND_TEST_HOME%" == "" set MIND_TEST_HOME=%MIND_TEST_ROOT%

@REM MIND_TEST_HOME defined and different from batch path, use it but warn the user
if /i "%MIND_TEST_HOME%" == "%MIND_TEST_ROOT%" goto endInit
echo.
echo WARNING: Using environment variable MIND_TEST_HOME which is different from mindtest.bat location
echo MIND_TEST_HOME         = %MIND_TEST_HOME% 
echo mindtest.bat location = %MIND_TEST_ROOT%
echo.

:endInit

setlocal
set MIND_TEST_CMD_LINE_ARGS=%*
set MIND_TEST_LIB=%MIND_TEST_HOME%/lib
set LAUNCHER=org.ow2.mind.test.Launcher
set MIND_TEST_JAVA_EXE="%JAVA_HOME%\bin\java.exe"
if not "%MIND_TEST_CLASSPATH%" == "" set MIND_TEST_CLASSPATH=%MIND_TEST_CLASSPATH%;

for /r "%MIND_TEST_LIB%\" %%i in (*.jar) do (
	set VarTmp=%%~fnxi;& call :concat
	)

goto :runMind
:concat
set MIND_TEST_CLASSPATH=%MIND_TEST_CLASSPATH%%VarTmp%
goto :eof

:runMind
%MIND_TEST_JAVA_EXE% -classpath %MIND_TEST_CLASSPATH% %MIND_TEST_OPTS% %LAUNCHER% %MIND_TEST_CMD_LINE_ARGS%


:error
@echo off
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal
(set ERROR_CODE=1)

@echo off
setlocal
cd /d "%~dp0"

mode con: cols=120 lines=35 >nul 2>&1

if not exist "out" mkdir "out"

echo Compiling SkyLink-Flight Crew Scheduling System...
javac -cp "lib\*" -d "out" Main.java DashboardWindow.java model\*.java dao\*.java service\*.java util\*.java
if errorlevel 1 (
    echo Compilation failed.
    exit /b 1
)

echo Starting application...
java -cp "out;lib\*" Main

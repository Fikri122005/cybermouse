@echo off
echo Compiling CyberMouse Game...
if not exist "bin" mkdir bin

javac -cp "bin;lib/*" -d bin src/main/*.java src/game/*.java src/entity/*.java src/object/*.java src/tile/*.java src/util/*.java src/data/*.java

if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b %errorlevel%
)
echo Compilation successful. Starting game...
java -cp "bin;lib/*" main.Main
pause
pause

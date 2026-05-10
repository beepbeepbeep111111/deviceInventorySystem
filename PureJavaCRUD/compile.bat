@echo off
echo Compiling...
if not exist bin mkdir bin
for %%f in (lib\*.jar) do set JAR=%%f
if "%JAR%"=="" ( echo ERROR: No .jar in lib/ folder! & pause & exit /b 1 )
javac -cp "%JAR%" -d bin src\*.java
if %errorlevel%==0 ( echo SUCCESS! Run: run.bat ) else ( echo FAILED. )
pause

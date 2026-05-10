@echo off
for %%f in (lib\*.jar) do set JAR=%%f
java -cp "bin;%JAR%" Main
pause

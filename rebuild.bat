@echo off
chcp 65001 >nul
cd /d "D:\MineAlphaProxy1"
call gradlew.bat clean build
if %ERRORLEVEL% NEQ 0 (
    echo BUILD FAILED
    pause
    exit /b %ERRORLEVEL%
)
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar build\libs\MineAlphaProxy-1.0.1-ALPHA.jar
pause
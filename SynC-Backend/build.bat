@echo off
setlocal

echo.
echo   SynC builder
echo   ------------

where go >nul 2>nul
if errorlevel 1 (
    echo   Go was not found on this machine.
    echo   Download and install it from https://go.dev/dl/ ^(the .msi installer^), then run this again.
    echo.
    pause
    exit /b 1
)

cd /d "%~dp0"

if not exist go.mod (
    echo   Setting up the module...
    go mod init sync-app-go
)

echo   Fetching the SQLite driver ^(needs internet, one time only^)...
go get modernc.org/sqlite
if errorlevel 1 (
    echo   Could not fetch dependencies. Check your internet connection and try again.
    pause
    exit /b 1
)

echo   Compiling SynC.exe...
go build -o SynC.exe .
if errorlevel 1 (
    echo   Build failed - see the errors above.
    pause
    exit /b 1
)

echo.
echo   Done. SynC.exe is ready in this folder.
echo   From now on, just double-click SynC.exe - no Go, no Node, nothing else needed.
echo.
pause

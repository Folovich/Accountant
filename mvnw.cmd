@echo off
setlocal
set "MAVEN_VERSION=3.9.9"
set "MAVEN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_CMD=%MAVEN_DIR%\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
  echo Downloading Apache Maven %MAVEN_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference = 'Stop'; $destination = Join-Path $env:USERPROFILE '.m2\wrapper\dists\apache-maven-3.9.9'; $archive = Join-Path $env:TEMP 'apache-maven-3.9.9-bin.zip'; New-Item -ItemType Directory -Force -Path $destination | Out-Null; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip' -OutFile $archive; Expand-Archive -LiteralPath $archive -DestinationPath $destination -Force; Remove-Item -LiteralPath $archive"
  if errorlevel 1 exit /b 1
)

pushd "%~dp0"
call "%MAVEN_CMD%" %*
set "RESULT=%ERRORLEVEL%"
popd
exit /b %RESULT%

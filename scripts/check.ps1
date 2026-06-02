$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$checkBuildDir = Join-Path $root "build\check-classes"

New-Item -ItemType Directory -Force -Path $checkBuildDir | Out-Null

$mainSources = Get-ChildItem -Path (Join-Path $root "src\main\java") -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
$testSources = Get-ChildItem -Path (Join-Path $root "src\test\java") -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
$sources = @($mainSources) + @($testSources)

javac -encoding UTF-8 -d $checkBuildDir $sources
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

java -cp $checkBuildDir com.combocounter.RepositorySmokeTest
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

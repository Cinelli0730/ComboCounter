$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$buildDir = Join-Path $root "build\classes"

New-Item -ItemType Directory -Force -Path $buildDir | Out-Null

$sources = Get-ChildItem -Path (Join-Path $root "src\main\java") -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
if (-not $sources) {
    throw "Java source files were not found."
}

javac -encoding UTF-8 -d $buildDir $sources
java -cp $buildDir com.combocounter.App

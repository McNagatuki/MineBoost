$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (!(Test-Path 'gradle/wrapper/gradle-wrapper.jar') -or !(Test-Path 'gradlew.bat')) {
    $temp = Join-Path ([System.IO.Path]::GetTempPath()) ('tagboost-' + [guid]::NewGuid().ToString())
    New-Item -ItemType Directory -Path $temp | Out-Null
    try {
        $zip = Join-Path $temp 'mdk.zip'
        $mdk = Join-Path $temp 'mdk'
        Invoke-WebRequest -UseBasicParsing -Uri 'https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.4.0/forge-1.20.1-47.4.0-mdk.zip' -OutFile $zip
        Expand-Archive -Path $zip -DestinationPath $mdk
        Copy-Item (Join-Path $mdk 'gradlew') . -Force
        Copy-Item (Join-Path $mdk 'gradlew.bat') . -Force
        New-Item -ItemType Directory -Path 'gradle/wrapper' -Force | Out-Null
        Copy-Item (Join-Path $mdk 'gradle/wrapper/*') 'gradle/wrapper' -Force
    } finally {
        Remove-Item $temp -Recurse -Force
    }
}
& .\gradlew.bat build @args
exit $LASTEXITCODE

param(
    [ValidateSet("check", "sdk", "build", "install")]
    [string]$Action = "check"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot

function Require-Command([string]$Name) {
    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if ($null -eq $command) { throw "'$Name' is not on PATH. Install Android SDK Command-Line Tools / Gradle and try again." }
    return $command.Source
}

function Get-SdkManager {
    $command = Get-Command sdkmanager -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }
    $candidate = Join-Path $env:ANDROID_HOME "cmdline-tools\latest\bin\sdkmanager.bat"
    if (Test-Path -LiteralPath $candidate) { return $candidate }
    throw "sdkmanager is missing. Install the Android SDK Command-Line Tools under ANDROID_HOME."
}

function Check-AndroidSdkManager {
    if ([string]::IsNullOrWhiteSpace($env:JAVA_HOME)) { throw "JAVA_HOME must point to JDK 17 or newer." }
    $java = Join-Path $env:JAVA_HOME "bin\java.exe"
    if (!(Test-Path -LiteralPath $java)) { throw "No java.exe found under JAVA_HOME: $env:JAVA_HOME" }
    $versionText = (& $java -version 2>&1 | Out-String)
    if ($versionText -notmatch 'version "(\d+)') { throw "Could not read the JDK version from JAVA_HOME." }
    if ([int]$Matches[1] -lt 17) { throw "JDK 17 or newer is required; JAVA_HOME reports $($Matches[1])." }
    if ([string]::IsNullOrWhiteSpace($env:ANDROID_HOME)) { throw "Set ANDROID_HOME to your Android SDK directory." }
    if (!(Test-Path -LiteralPath $env:ANDROID_HOME)) { throw "ANDROID_HOME does not exist: $env:ANDROID_HOME" }
    $sdkmanager = Get-SdkManager
    Write-Host "JDK: $env:JAVA_HOME"
    Write-Host "Android SDK: $env:ANDROID_HOME"
    Write-Host "sdkmanager: $sdkmanager"
}

function Check-AndroidToolchain {
    Check-AndroidSdkManager
    $adb = Get-Command adb -ErrorAction SilentlyContinue
    if ($null -eq $adb) {
        $adbPath = Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"
        if (!(Test-Path -LiteralPath $adbPath)) { throw "adb is missing. Run .\scripts\android-cli.ps1 sdk first." }
        $adb = [pscustomobject]@{ Source = $adbPath }
    }
    $gradle = Require-Command "gradle"
    Write-Host "adb: $($adb.Source)"
    Write-Host "Gradle: $gradle"
}

switch ($Action) {
    "check" { Check-AndroidToolchain }
    "sdk" {
        Check-AndroidSdkManager
        $sdkmanager = Get-SdkManager
        & $sdkmanager --sdk_root=$env:ANDROID_HOME "platform-tools" "platforms;android-36" "build-tools;36.0.0"
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
        & $sdkmanager --sdk_root=$env:ANDROID_HOME --licenses
        exit $LASTEXITCODE
    }
    "build" {
        Check-AndroidToolchain
        Push-Location $ProjectRoot
        try { & gradle :androidApp:assembleDebug; exit $LASTEXITCODE }
        finally { Pop-Location }
    }
    "install" {
        Check-AndroidToolchain
        Push-Location $ProjectRoot
        try { & gradle :androidApp:installDebug; exit $LASTEXITCODE }
        finally { Pop-Location }
    }
}

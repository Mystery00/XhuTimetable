param(
    [Parameter(Mandatory = $true)][string]$DevEcoHome,
    [Parameter(Mandatory = $true)][string]$CacheDirectory,
    [ValidateSet('arm64', 'x64')][string]$Architecture = 'arm64',
    [string]$JavaHome,
    [switch]$SkipKotlin,
    [switch]$Tests
)
$ErrorActionPreference = 'Stop'
$Architecture = $Architecture.ToLowerInvariant()
$DevEcoHome = (Resolve-Path $DevEcoHome).Path
$CacheDirectory = [IO.Path]::GetFullPath($CacheDirectory)

function Get-JavaMajorVersion([string]$candidate) {
    if ([string]::IsNullOrWhiteSpace($candidate)) { return $null }
    $javaExe = Join-Path $candidate 'bin\java.exe'
    if (-not (Test-Path $javaExe)) { return $null }
    $line = (& $javaExe -version 2>&1 | Select-Object -First 1).ToString()
    if ($line -match '"(\d+)(?:\.\d+)?') {
        return [int]$Matches[1]
    }
    return $null
}

# Gradle 8.9 仅支持 JDK 17~23，DevEco Studio 26 自带 JBR 为 JDK 25，需自动优先选用兼容的 JDK 21/17~23。
$resolvedJavaHome = $null
$candidates = [System.Collections.Generic.List[string]]::new()
if (-not [string]::IsNullOrWhiteSpace($JavaHome)) { $candidates.Add($JavaHome) }
if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) { $candidates.Add($env:JAVA_HOME) }
$candidates.Add((Join-Path $DevEcoHome 'jbr'))
foreach ($searchRoot in @(
    (Join-Path (Split-Path $CacheDirectory -Parent) '.jdks'),
    (Join-Path $env:USERPROFILE '.jdks'),
    'C:\Program Files\Eclipse Adoptium',
    'C:\Program Files\Java'
)) {
    if (Test-Path $searchRoot) {
        Get-ChildItem -Path $searchRoot -Directory -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending |
            ForEach-Object { $candidates.Add($_.FullName) }
    }
}
$whereJava = Get-Command java -ErrorAction SilentlyContinue
if ($whereJava) {
    $candidates.Add((Split-Path (Split-Path $whereJava.Source -Parent) -Parent))
}
foreach ($candidate in $candidates) {
    $major = Get-JavaMajorVersion $candidate
    if ($null -ne $major -and $major -ge 17 -and $major -le 23) {
        $resolvedJavaHome = (Resolve-Path $candidate).Path
        break
    }
}
if (-not $resolvedJavaHome) {
    throw "未找到兼容 Gradle 8.9 的 JDK（17~23），请通过 -JavaHome 或 JAVA_HOME 指定 JDK 21 路径。"
}

$env:JAVA_HOME = $resolvedJavaHome
$env:NODE_HOME = Join-Path $DevEcoHome 'tools/node'
$env:DEVECO_SDK_HOME = Join-Path $DevEcoHome 'sdk'
$env:KONAN_DATA_DIR = Join-Path $CacheDirectory '.konan'
$env:PATH = "$env:NODE_HOME;$env:JAVA_HOME\bin;$env:PATH"

$product = if ($Architecture -eq 'x64') { 'emulator' } else { 'default' }
$nativeTarget = if ($Architecture -eq 'x64') { 'OhosX64' } else { 'OhosArm64' }

if (-not $SkipKotlin) {
    & "$PSScriptRoot/kotlin/gradlew.bat" "-PharmonyArch=$Architecture" :loggerChecks:test ":composeApp:publishDebug$nativeTarget" --console=plain
    if ($LASTEXITCODE -ne 0) { throw "Kotlin 编译失败：$LASTEXITCODE" }
} else {
    & "$PSScriptRoot/kotlin/gradlew.bat" "-PharmonyArch=$Architecture" :composeApp:syncHarmonyResources --console=plain
    if ($LASTEXITCODE -ne 0) { throw "鸿蒙资源同步失败：$LASTEXITCODE" }
}

& "$DevEcoHome/tools/ohpm/bin/ohpm.bat" install --prefix $PSScriptRoot --cache (Join-Path $CacheDirectory 'ohpm')
if ($LASTEXITCODE -ne 0) { throw "鸿蒙依赖安装失败：$LASTEXITCODE" }

Push-Location $PSScriptRoot
try {
    $target = if ($Tests) { 'ohosTest' } else { $product }
    & "$env:NODE_HOME/node.exe" "$DevEcoHome/tools/hvigor/bin/hvigorw.js" --mode module -p "product=$product" -p "module=entry@$target" -p buildMode=debug assembleHap --no-daemon
    if ($LASTEXITCODE -ne 0) { throw "鸿蒙应用打包失败：$LASTEXITCODE" }
} finally {
    Pop-Location
}

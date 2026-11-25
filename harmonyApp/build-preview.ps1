param(
    [Parameter(Mandatory = $true)][string]$DevEcoHome,
    [Parameter(Mandatory = $true)][string]$CacheDirectory,
    [ValidateSet('arm64', 'x64')][string]$Architecture = 'arm64',
    [switch]$SkipKotlin,
    [switch]$Tests
)
$ErrorActionPreference = 'Stop'
$Architecture = $Architecture.ToLowerInvariant()
$DevEcoHome = (Resolve-Path $DevEcoHome).Path
$CacheDirectory = [IO.Path]::GetFullPath($CacheDirectory)
$env:JAVA_HOME = Join-Path $DevEcoHome 'jbr'
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

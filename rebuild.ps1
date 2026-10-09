chcp 65001 | Out-Null
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding  = [System.Text.Encoding]::UTF8
Set-Location "D:\MineAlphaProxy1"
.\gradlew.bat clean build
if ($LASTEXITCODE -eq 0) {
    $args = @(
        "-Dfile.encoding=UTF-8",
        "-Dstdout.encoding=UTF-8",
        "-Dstderr.encoding=UTF-8",
        "-jar",
        "build\libs\MineAlphaProxy-1.0.1-ALPHA.jar"
    )
    & java $args
}
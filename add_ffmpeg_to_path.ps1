# Script para adicionar FFmpeg ao PATH do Windows
# Execute com: powershell -ExecutionPolicy Bypass -File add_ffmpeg_to_path.ps1

# Caminho do FFmpeg
$ffmpegPath = "C:\development\ffmpeg-8.0.1\bin"

# Verifica se o diretório existe
if (!(Test-Path $ffmpegPath)) {
    Write-Host "❌ Erro: Diretório não encontrado: $ffmpegPath" -ForegroundColor Red
    exit 1
}

# Obtém o PATH atual
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")

# Verifica se já está no PATH
if ($currentPath -like "*$ffmpegPath*") {
    Write-Host "✅ FFmpeg já está no PATH" -ForegroundColor Green
    exit 0
}

# Adiciona ao PATH
$newPath = $currentPath + ";" + $ffmpegPath
[Environment]::SetEnvironmentVariable("PATH", $newPath, "Machine")

Write-Host "✅ FFmpeg adicionado ao PATH com sucesso!" -ForegroundColor Green
Write-Host "   Caminho: $ffmpegPath" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚠️  Por favor, reinicie o terminal ou IDE para que as mudanças tenham efeito." -ForegroundColor Yellow
Write-Host ""
Write-Host "Para verificar: ffprobe -version" -ForegroundColor Gray


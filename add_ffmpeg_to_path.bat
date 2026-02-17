@echo off
REM Script para adicionar FFmpeg ao PATH do Windows
REM Execute como Administrador

echo.
echo ========================================
echo  Configurar FFmpeg no PATH do Windows
echo ========================================
echo.

REM Verifica se é administrador
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo Erro: Este script precisa rodar como ADMINISTRADOR
    echo.
    echo Como executar:
    echo 1. Clique com botao direito neste arquivo
    echo 2. Selecione "Executar como administrador"
    echo.
    pause
    exit /b 1
)

REM Define o caminho do FFmpeg
set FFMPEG_PATH=C:\development\ffmpeg-8.0.1\bin

REM Verifica se o diretorio existe
if not exist "%FFMPEG_PATH%" (
    echo Erro: Diretorio nao encontrado: %FFMPEG_PATH%
    echo.
    pause
    exit /b 1
)

echo [OK] Diretorio encontrado: %FFMPEG_PATH%
echo.

REM Adiciona ao PATH (usa setx)
setx PATH "%PATH%;%FFMPEG_PATH%" /M

if %errorLevel% equ 0 (
    echo.
    echo ========================================
    echo [SUCESSO] FFmpeg adicionado ao PATH!
    echo ========================================
    echo.
    echo Caminho adicionado: %FFMPEG_PATH%
    echo.
    echo IMPORTANTE: Feche e reabra o terminal/IDE
    echo para que as mudancas tenham efeito!
    echo.
    echo Para verificar, abra novo terminal e digite:
    echo   ffprobe -version
    echo.
) else (
    echo [ERRO] Falha ao adicionar ao PATH
    echo Tente manualmente via:
    echo - Iniciar ^> Variaveis de ambiente ^> Path
    echo.
)

pause


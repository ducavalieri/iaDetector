# 🔧 Guia: Configurar FFmpeg no PATH do Windows

## ✅ Você Descompactou FFmpeg Em:
```
C:\development\ffmpeg-8.0.1\
```

## 🎯 Objetivo
Fazer o ffprobe ser encontrado em qualquer lugar do terminal, sem precisar digitar o caminho completo.

## 📋 Opção 1: Adicionar ao PATH do Windows (RECOMENDADO)

### Passo 1: Abrir Variáveis de Ambiente

**Método A - GUI (Mais Fácil):**
1. Pressione `Win + X` → Clique em "Sistema"
2. Clique em "Configurações avançadas do sistema" (ou pesquise "Variáveis de ambiente")
3. Clique no botão "Variáveis de Ambiente..."
4. Encontre "Path" na seção "Variáveis do sistema" (parte de baixo)
5. Clique em "Editar"

**Método B - PowerShell (Automático):**
```powershell
# Copie e execute no PowerShell (Como Administrador):
$ffmpegPath = "C:\development\ffmpeg-8.0.1\bin"
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
if ($currentPath -notlike "*$ffmpegPath*") {
    [Environment]::SetEnvironmentVariable("PATH", $currentPath + ";" + $ffmpegPath, "Machine")
    Write-Host "✅ FFmpeg adicionado ao PATH"
} else {
    Write-Host "✅ FFmpeg já está no PATH"
}
```

### Passo 2: Adicionar o Caminho

Na janela de edição do Path:
1. Clique em "Novo"
2. Copie e cole este caminho:
   ```
   C:\development\ffmpeg-8.0.1\bin
   ```
3. Clique "OK" em todas as janelas
4. **Reinicie o terminal ou IDE**

### Passo 3: Verificar

Abra um **novo** PowerShell/CMD e digite:
```powershell
ffprobe -version
```

Se aparecer a versão do ffprobe, está funcionando! ✅

---

## 📋 Opção 2: Configurar via application.yaml (BACKUP)

Se preferir, você pode configurar o caminho completo na aplicação:

**Editar**: `src/main/resources/application.yaml`

```yaml
ffprobe:
  path: C:/development/ffmpeg-8.0.1/bin/ffprobe.exe
  timeout-seconds: 30
```

**Notas:**
- Use barras `/` (não `\`) em YAML
- Adicione `.exe` no final do caminho (Windows)
- Esta é uma alternativa se não conseguir adicionar ao PATH

---

## 🧪 Teste Após Configurar

### 1. Verificar no Terminal
```powershell
# Nova janela PowerShell (IMPORTANTE: nova janela!)
ffprobe -version
```

Resultado esperado:
```
ffprobe version 8.0.1 Copyright (c) 2007-2025 the FFmpeg developers
...
```

### 2. Verificar na Aplicação

Após iniciar a app:
```powershell
# Em outro terminal
curl http://localhost:8080/api/health/ffprobe
```

Resposta esperada:
```json
{
  "status": "UP",
  "service": "ffprobe",
  "message": "ffprobe is available and operational"
}
```

### 3. Logs da App

Quando iniciar, você deve ver:
```
2026-02-17T17:21:34.203  INFO  FfprobeRunner : 
    Validating ffprobe availability at: ffprobe

2026-02-17T17:21:34.350  INFO  FfprobeRunner : 
    ✓ ffprobe is available and operational at: ffprobe
```

---

## ❓ Troubleshooting

### Problema: "ffprobe still not found"

**Solução 1: Reiniciar Terminal**
```
A mudança de PATH só vale em NOVO terminal
Terminal antigo ainda não vê a mudança
```

**Solução 2: Reiniciar IDE**
Se usar IntelliJ/VS Code:
- Feche completamente
- Reabra
- Rode novamente

**Solução 3: Usar Caminho Absoluto**
```yaml
ffprobe:
  path: C:/development/ffmpeg-8.0.1/bin/ffprobe.exe
```

**Solução 4: Verificar Permissões**
```powershell
# Verifique se o arquivo é executável
dir "C:\development\ffmpeg-8.0.1\bin\ffprobe.exe"
```

---

## 📍 Onde Encontrar Arquivos FFmpeg

```
C:\development\ffmpeg-8.0.1\
├── bin\
│   ├── ffmpeg.exe      ← Conversor de vídeo
│   ├── ffprobe.exe     ← Leitor de metadados (O QUE USAMOS)
│   └── ...
├── doc\
├── LICENSE
└── README.txt
```

---

## ✨ Resultado Final

Após configurar, você terá:

| Aspecto | Status |
|---------|--------|
| FFmpeg instalado | ✅ `C:\development\ffmpeg-8.0.1` |
| FFprobe no PATH | ✅ Acessível como `ffprobe` |
| App detecta ffprobe | ✅ Logs mostram "✓ available" |
| Endpoints funcionam | ✅ `/api/video/info` ativo |
| Health check | ✅ Status "UP" |

---

## 🚀 Próximo Passo

Após configurar:
1. Inicie a aplicação
2. Faça upload de um vídeo pequeno para testar
3. Use `/api/health/ffprobe` para verificar status

Pronto! FFmpeg está configurado! 🎉


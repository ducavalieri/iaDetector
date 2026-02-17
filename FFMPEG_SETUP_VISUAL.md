# 📋 Passo-a-Passo: Configurar FFmpeg no PATH

## 🎯 Resumo Rápido

Você tem FFmpeg em: `C:\development\ffmpeg-8.0.1`
Você precisa: Adicionar `C:\development\ffmpeg-8.0.1\bin` ao PATH do Windows

## ⚡ Opção Mais Rápida (Recomendado)

### Passo 1: Execute o Script BAT

1. Vá para: `C:\Users\eduar\projects\iaDetector\`
2. Clique com **botão direito** em `add_ffmpeg_to_path.bat`
3. Selecione **"Executar como administrador"**
4. Clique **"Sim"** quando pedir permissão
5. Aguarde a mensagem de sucesso
6. Pressione Enter

✅ **PRONTO!** FFmpeg foi adicionado ao PATH!

---

## 🖱️ Opção Manual (Se o Script Não Funcionar)

### Passo 1: Abrir Variáveis de Ambiente

**Windows 10/11:**

1. Pressione `Windows + X` (menu iniciar no canto)
2. Clique em **"Sistema"**
3. Clique em **"Configurações avançadas do sistema"**
   - (Ou pesquise "Variáveis de ambiente" na barra de iniciar)
4. Clique em **"Variáveis de Ambiente..."**

**Resultado esperado:**
```
┌─────────────────────────────────────┐
│ Variáveis de Ambiente               │
│                                     │
│ Variáveis do usuário para ...       │
│ ┌─────────────────────────────────┐ │
│ │ [Lista de variáveis]            │ │
│ └─────────────────────────────────┘ │
│                                     │
│ Variáveis do sistema                │
│ ┌─────────────────────────────────┐ │
│ │ Path                            │ │ ← Procure isto
│ │ TEMP                            │ │
│ │ PATHEXT                         │ │
│ │ ...                             │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [Novo] [Editar] [Deletar]          │
└─────────────────────────────────────┘
```

### Passo 2: Editar Variável "Path"

1. Na seção **"Variáveis do sistema"** (parte inferior)
2. Procure por **"Path"** (não "PATHEXT")
3. Clique em **"Path"** para selecioná-lo
4. Clique em **"Editar"**

**Resultado esperado:**
```
┌─────────────────────────────────────┐
│ Editar Variável de Ambiente         │
│                                     │
│ Nome da variável: Path              │
│                                     │
│ Valor da variável:                  │
│ ┌─────────────────────────────────┐ │
│ │ C:\Windows\System32             │ │
│ │ C:\Windows                      │ │
│ │ C:\Program Files\...            │ │
│ │ [mais caminhos aqui]            │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [Novo] [Editar] [Deletar]          │
│ [Mover para Cima] [Mover para Baixo]│
└─────────────────────────────────────┘
```

### Passo 3: Adicionar Novo Caminho

Na janela que abriu:

1. Clique em **"Novo"** (botão verde com +)
2. Digite exatamente:
   ```
   C:\development\ffmpeg-8.0.1\bin
   ```
3. Clique **"OK"**

**Resultado esperado:**
```
┌─────────────────────────────────────┐
│ Novo caminho adicionado!            │
│                                     │
│ C:\development\ffmpeg-8.0.1\bin     │
│ ↑ este novo caminho aparece aqui   │
└─────────────────────────────────────┘
```

### Passo 4: Confirmar Tudo

1. Clique **"OK"** na janela "Editar Variável"
2. Clique **"OK"** na janela "Variáveis de Ambiente"
3. Feche todas as janelas

✅ **PRONTO!** FFmpeg foi adicionado ao PATH!

---

## 🧪 Verificar se Funcionou

### Passo 1: Abrir Terminal NOVO

**IMPORTANTE:** Feche e abra um terminal novo (ou IDE)!

```
As mudanças só aparecem em NOVO terminal
Terminal antigo ainda não sabe sobre o novo PATH
```

Abra **PowerShell** ou **CMD**:
- Pressione `Windows + R`
- Digite `powershell` ou `cmd`
- Pressione Enter

### Passo 2: Verificar Instalação

Digite:
```powershell
ffprobe -version
```

**Se funcionar, você verá:**
```
ffprobe version 8.0.1 Copyright (c) 2007-2025 the FFmpeg developers
built with gcc 13.3.0 (GCC)
...
```

**Se não funcionar:**
```
'ffprobe' is not recognized as an internal or external command
```

Se vir isso, volte para **Passo 1** e tente novamente com um novo terminal.

---

## 🚀 Testar na Aplicação

### Passo 1: Inicie a App

```powershell
cd C:\Users\eduar\projects\iaDetector
./mvnw.cmd spring-boot:run
```

### Passo 2: Verifique nos Logs

Procure por uma linha assim:
```
✓ ffprobe is available and operational at: ffprobe
```

Se vir isto, FFprobe está funcionando! ✅

### Passo 3: Teste o Health Check

Em outro terminal:
```powershell
curl http://localhost:8080/api/health/ffprobe
```

**Resposta esperada:**
```json
{
  "status": "UP",
  "service": "ffprobe",
  "message": "ffprobe is available and operational"
}
```

---

## ❌ Se Não Funcionar

### Problema 1: "ffprobe is not recognized"

**Solução:**
- Você reiniciou o terminal? (precisa de novo terminal)
- Fechou a IDE e reabre? (IntelliJ/VS Code capturam PATH ao iniciar)

### Problema 2: "ffprobe is not available" (na app)

**Solução 1:**
Verifique se está no PATH:
```powershell
echo $env:PATH
```

Se não ver `C:\development\ffmpeg-8.0.1\bin`, configure manualmente em `application.yaml`:

```yaml
ffprobe:
  path: C:/development/ffmpeg-8.0.1/bin/ffprobe.exe
```

**Solução 2:**
Verifique se o arquivo existe:
```powershell
dir "C:\development\ffmpeg-8.0.1\bin\ffprobe.exe"
```

Se não encontrar, ffmpeg pode não estar extraído corretamente.

### Problema 3: Permissões

Se o script BAT disser "Acesso negado":
1. Clique com botão direito
2. Selecione **"Executar como administrador"**
3. Clique **"Sim"** no UAC

---

## 📸 Imagens de Referência (Descrição)

### Variáveis de Ambiente - Localização do Path

```
Variáveis do sistema (parte de baixo):
┌──────────────────────────────┐
│ ALLUSERSPROFILE              │
│ APPDATA                      │
│ CLASSPATH                    │
│ COMSPEC                      │
│ HOMEDRIVE                    │
│ HOMEPATH                     │
│ JAVA_HOME                    │
│ Path  ← ESTE AQUI!           │
│ PATHEXT                      │
│ PROCESSOR_ARCHITECTURE       │
│ ...                          │
└──────────────────────────────┘
```

### Após Adicionar

```
Valor da variável Path:
┌─────────────────────────────────┐
│ C:\Windows\System32             │
│ C:\Windows                      │
│ ...                             │
│ C:\development\ffmpeg-8.0.1\bin │ ← Novo!
│                                 │
└─────────────────────────────────┘
```

---

## ✅ Checklist Final

- [ ] FFmpeg extraído em `C:\development\ffmpeg-8.0.1`
- [ ] Path adicionado: `C:\development\ffmpeg-8.0.1\bin`
- [ ] Terminal novo aberto (IMPORTANTE!)
- [ ] `ffprobe -version` funciona
- [ ] App mostra "✓ ffprobe is available"
- [ ] `/api/health/ffprobe` retorna status "UP"

---

## 🎉 Sucesso!

Você agora tem ffmpeg/ffprobe funcionando na sua máquina e integrado com a aplicação! 

Próximos passos:
1. Faça upload de um vídeo pequeno para testar
2. Use `/api/video/info` para extrair metadados
3. Aproveite a análise de vídeo na sua aplicação!

**Dúvidas?** Consulte `HELP.md` ou `FFMPEG_SETUP_GUIDE.md`


# Visual: Como o Spring Verifica FFprobe no Startup

## 📊 Diagrama Completo do Fluxo

```
┌─────────────────────────────────────────────────────────────────┐
│                   SPRING BOOT INICIA                            │
│                   IaDetectorApplication.main()                  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│         Spring procura classes com @Component                   │
│                                                                 │
│  ✓ @Component public class FfprobeRunner { ... }              │
│  ✓ @Component public class FfprobeProperties { ... }          │
│  ✓ @RestController public class HealthController { ... }      │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│     Spring vê que FfprobeRunner tem @Autowired                 │
│                                                                 │
│     @Autowired                                                  │
│     public FfprobeRunner(FfprobeProperties props) { ... }      │
│                         ↑                                       │
│     Spring injeta props aqui                                   │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│     CONSTRUTOR DO FfprobeRunner É CHAMADO                       │
│                                                                 │
│     public FfprobeRunner(FfprobeProperties props) {            │
│         this(props.getPath(),                                  │
│              Duration.ofSeconds(props.getTimeoutSeconds()));   │
│         validateFfprobeAvailability(); ← CHAMADA CHAVE!        │
│     }                                                           │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│  validateFfprobeAvailability() EXECUTA                          │
│                                                                 │
│  Lê: ffprobe.path = "ffprobe" (de application.yaml)           │
│      ffprobe.timeout = 30 segundos                            │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│  CRIA ProcessBuilder para executar:                            │
│                                                                 │
│  → Windows:   "ffprobe.exe -version"                          │
│  → Linux/Mac: "ffprobe -version"                              │
│                                                                 │
│  ProcessBuilder pb = new ProcessBuilder("ffprobe", "-version")│
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌────────────────────────────────────────┬────────────────────────┐
│ TENTA INICIAR O PROCESSO               │                        │
│ process = pb.start()                   │                        │
└────────────────┬───────────────────────┴────────────────────────┘
                 │
         ┌───────┴────────┐
         │                │
    ✓ SUCESSO         ✗ FALHA
    (conseguiu        (não conseguiu
     iniciar          iniciar)
     ffprobe)         
         │                │
         ▼                ▼
    ┌─────────────┐  ┌──────────────────┐
    │ Procura     │  │ IOException       │
    │ arquivo     │  │ lançada:          │
    │ "ffprobe"   │  │ "Cannot run       │
    │ no PATH     │  │  program ffprobe" │
    │             │  │                   │
    │ Encontrou!  │  │ NÃO ENCONTROU!    │
    └──────┬──────┘  └────────┬──────────┘
           │                  │
           ▼                  ▼
    ┌─────────────┐  ┌──────────────────────────┐
    │ Executa:    │  │ isAvailableFlag = false  │
    │ ffprobe     │  │                          │
    │ -version    │  │ Log WARN:                │
    │             │  │ "⚠ ffprobe not found    │
    │ Aguarda     │  │  at path: 'ffprobe'"    │
    │ resposta    │  │                          │
    │ (5 seg)     │  │ App continua iniciando   │
    └──────┬──────┘  └────────┬─────────────────┘
           │                  │
           ▼                  │
    ┌─────────────┐           │
    │ FFprobe     │           │
    │ responde    │           │
    │ com exit    │           │
    │ code 0      │           │
    └──────┬──────┘           │
           │                  │
           ▼                  │
    ┌──────────────────┐      │
    │ isAvailable      │      │
    │ Flag = true      │      │
    │                  │      │
    │ Log INFO:        │      │
    │ "✓ ffprobe is   │      │
    │ available"      │      │
    └────────┬─────────┘      │
             │                │
             └────────┬───────┘
                      │
                      ▼
        ┌─────────────────────────────┐
        │  SPRING CONTINUA INICIANDO  │
        │                             │
        │  (App NÃO bloqueia se       │
        │   ffprobe faltar!)          │
        │                             │
        │  Cria outros beans:         │
        │  - HealthController         │
        │  - VideoInfoService         │
        │  - GlobalExceptionHandler   │
        │  - etc...                   │
        └────────────────┬────────────┘
                         │
                         ▼
        ┌─────────────────────────────────┐
        │  APLICAÇÃO INICIADA COM SUCESSO │
        │                                 │
        │  Servidor escutando na porta    │
        │  8080                           │
        │                                 │
        │  Endpoints disponíveis:         │
        │  - /api/video/info              │
        │  - /api/health/ffprobe          │
        │  - etc...                       │
        └─────────────────────────────────┘
```

---

## 🔍 Zoom: O que Acontece Dentro de validateFfprobeAvailability()

```
validateFfprobeAvailability() {
    
    TRY {
        
        1️⃣  Log início:
            "Validating ffprobe availability at: ffprobe"
        
        2️⃣  ProcessBuilder pb = new ProcessBuilder("ffprobe", "-version")
                    ↓
                    Prepara comando para executar
        
        3️⃣  pb.redirectErrorStream(true)
                    ↓
                    Se ffprobe exibir erro, redireciona para output
        
        4️⃣  Process process = pb.start()
                    ↓
                    ✓ Se conseguir: cria um objeto Process
                    ✗ Se não conseguir: lança IOException
                    
                    ┌─────────────────────────────────┐
                    │ PODE LANÇAR IOException:        │
                    │ "Cannot run program 'ffprobe'"  │
                    │ (ffprobe não está em PATH)      │
                    └──────────────┬──────────────────┘
                                   │
                                   └─→ CATCH IOException
                                       ↓
                                       isAvailableFlag = false
        
        5️⃣  boolean finished = process.waitFor(5, TimeUnit.SECONDS)
                    ↓
                    Aguarda até 5 segundos para ffprobe terminar
                    
                    ✓ true  = ffprobe respondeu dentro de 5 seg
                    ✗ false = ffprobe demorou MAIS de 5 seg
        
        6️⃣  if (!finished) {
                process.destroyForcibly()
                isAvailableFlag = false
                return  ← Sai da função
            }
                    
        7️⃣  int exitCode = process.exitValue()
                    ↓
                    Obtém o código de saída:
                    0   = sucesso
                    1,2,3... = erro
        
        8️⃣  if (exitCode == 0) {
                isAvailableFlag = true
                Log: "✓ ffprobe is available"
            } else {
                isAvailableFlag = false
                Log: "✗ ffprobe exited with code X"
            }
    
    } CATCH (IOException e) {
        
        isAvailableFlag = false
        Log WARN: "⚠ ffprobe not found at path 'ffprobe'"
        Log INFO: "Dica: Instale ffmpeg..."
        
        ← NÃO LANÇA EXCEÇÃO (permite app iniciar)
    
    } CATCH (InterruptedException e) {
        
        isAvailableFlag = false
        Log WARN: "ffprobe validation interrupted"
        Thread.currentThread().interrupt()
    
    }
}
```

---

## 🎯 O Que Muda Quando User Tenta Usar um Endpoint

```
┌─────────────────────────────────────────────────────┐
│ User faz requisição:                                │
│ curl -F "file=@video.mp4"                          │
│      http://localhost:8080/api/video/info         │
└──────────────────────┬────────────────────────────────┘
                       │
                       ▼
         ┌─────────────────────────────────┐
         │ VideoInfoController recebe a    │
         │ requisição em postVideoInfo()   │
         └────────────────┬────────────────┘
                          │
                          ▼
         ┌─────────────────────────────────┐
         │ Chama:                          │
         │ service.analyzeFile(file)       │
         └────────────────┬────────────────┘
                          │
                          ▼
         ┌─────────────────────────────────┐
         │ VideoInfoService chama:         │
         │ runner.run(tempFilePath)        │
         └────────────────┬────────────────┘
                          │
                 ┌────────┴────────┐
                 │                 │
        ✓ ffprobe        ✗ ffprobe
        DISPONÍVEL       NÃO DISPONÍVEL
        (isAvailable     (isAvailable
         = true)         = false)
                 │                 │
                 ▼                 ▼
         ┌────────────────┐  ┌──────────────────┐
         │ Executa        │  │ if (!isAvailable)│
         │ ffprobe        │  │   throw new      │
         │ normalmente    │  │   IOException()  │
         │                │  └────────┬─────────┘
         │ Retorna JSON   │           │
         │ com metadados  │           ▼
         │                │  ┌──────────────────────┐
         │ Return 200 OK  │  │ GlobalExceptionHandler│
         │ + VideoInfoDTO │  │ captura IOException  │
         └────────────────┘  │                       │
                             │ Retorna:             │
                             │ HTTP 500 ou 422      │
                             │ ErrorDTO com         │
                             │ mensagem clara       │
                             └──────────────────────┘
```

---

## 📝 Log Esperado no Startup

### CASO 1: FFprobe Instalado e Disponível ✅

```
2026-02-17T17:21:34.203  INFO  c.i.a.s.FfprobeRunner : 
    Validating ffprobe availability at: ffprobe

2026-02-17T17:21:34.350  INFO  c.i.a.s.FfprobeRunner : 
    ✓ ffprobe is available and operational at: ffprobe
```

### CASO 2: FFprobe NÃO Instalado ❌

```
2026-02-17T17:21:34.203  INFO  c.i.a.s.FfprobeRunner : 
    Validating ffprobe availability at: ffprobe

2026-02-17T17:21:34.253  WARN  c.i.a.s.FfprobeRunner : 
    ⚠ ffprobe not found or not executable at path: 'ffprobe'. 
    Video analysis endpoints will not work until ffprobe is installed. 
    To fix: Install ffmpeg (includes ffprobe) from https://ffmpeg.org/download.html, 
    or set FFPROBE_PATH environment variable to ffprobe executable path.
```

### CASO 3: FFprobe Timeout ⏱️

```
2026-02-17T17:21:34.203  INFO  c.i.a.s.FfprobeRunner : 
    Validating ffprobe availability at: ffprobe

2026-02-17T17:21:39.305  WARN  c.i.a.s.FfprobeRunner : 
    ffprobe -version timed out during startup validation
```

---

## 🔑 Pontos-Chave para Lembrar

1. **Não Bloqueia Startup**: Mesmo se ffprobe não existir, a app continua iniciando
2. **Verifica com `ffprobe -version`**: Comando simples que apenas testa se executable
3. **Timeout de 5 Segundos**: Evita travamento se ffprobe responder lentamente  
4. **Flag Booleano**: `isAvailableFlag` armazena resultado para usar depois
5. **Logs Informativos**: Você vê mensagens claras no startup
6. **Falha Gracefully**: Se tentar usar endpoint sem ffprobe, retorna erro amigável
7. **Health Check**: Endpoint `/api/health/ffprobe` mostra status em tempo real

---

## 🚀 Resumo Visual

```
┌─────────────────────────────────────────┐
│  Spring Boot Inicia                     │
│  ↓                                      │
│  Cria FfprobeRunner                     │
│  ↓                                      │
│  validateFfprobeAvailability()          │
│  ↓                                      │
│  Tenta rodar: ffprobe -version          │
│  ↓                                      │
│  ┌──────────┬──────────┐               │
│  │          │          │               │
│  ✓ OK    ✗ ERRO   ⏱ TIMEOUT            │
│  │          │          │               │
│  isAvail = true  isAvail = false        │
│  │          │          │               │
│  └──────────┴──────────┘               │
│  ↓                                      │
│  App continua iniciando                │
│  ↓                                      │
│  App pronta para receber requisições    │
│  ↓                                      │
│  Se user chamar /api/video/info:       │
│    → Se isAvail=true: processa vídeo   │
│    → Se isAvail=false: retorna erro    │
└─────────────────────────────────────────┘
```

Pronto! Agora você entende completamente como o Spring verifica se ffprobe está instalado! 🎓


# Guia Prático: Passo a Passo da Verificação do FFprobe

## 📚 Cenário Real: Você Executa a Aplicação

### Passo 1: Inicia a Aplicação

```powershell
cd C:\Users\eduar\projects\iaDetector
./mvnw.cmd spring-boot:run
```

### Passo 2: Spring Boot Inicia (você vê isto no console)

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___  '_  '_  '_ \/ _`  \ \ \ \
 \\/  ___) _)      (_   ) ) ) )
  '  ____ .___ __ _\__,  / / / /
 =========_==============___/=/_/_/_/

 :: Spring Boot ::                (v4.0.1)

2026-02-17T17:21:34.000  INFO  com.iaDetector.IaDetectorApplication : 
    Starting IaDetectorApplication using Java 17.0.13
```

### Passo 3: Spring Procura pelos @Component (você não vê isto)

Spring internamente faz:

```java
// Spring procura classes marcadas com @Component
List<Class<?>> components = findAllComponentsInPackage("com.iaDetector");

// Spring encontra:
// ✓ FfprobeRunner (tem @Component)
// ✓ FfprobeProperties (tem @Component)
// ✓ VideoInfoService (tem @Service que estende @Component)
// ✓ HealthController (tem @RestController que estende @Component)
// etc...
```

### Passo 4: Spring Vê que FfprobeRunner Tem @Autowired no Construtor

```java
// Spring encontra:
@Component
public class FfprobeRunner {
    
    @Autowired  // ← Spring vê isto
    public FfprobeRunner(FfprobeProperties props) {
        // ...
    }
}

// Spring então:
// 1. Cria instância de FfprobeProperties
// 2. Injeita em FfprobeRunner
// 3. Chama o construtor
```

### Passo 5: Construtor do FfprobeRunner Executa

```java
@Autowired
public FfprobeRunner(FfprobeProperties props) {
    // 1️⃣ Primeiro chama o outro construtor
    this(props.getPath(), Duration.ofSeconds(props.getTimeoutSeconds()));
    //    ↑ Lê application.yaml:
    //       ffprobe:
    //         path: ffprobe
    //         timeout-seconds: 30
    
    // 2️⃣ Depois chama a validação
    validateFfprobeAvailability();
}
```

### Passo 6: Você Vê o Log no Console

```
2026-02-17T17:21:34.203  INFO  com.iaDetector.api.service.FfprobeRunner   : 
    Validating ffprobe availability at: ffprobe
```

### Passo 7: validateFfprobeAvailability() Executa

```java
private void validateFfprobeAvailability() {
    try {
        // 1️⃣ Cria ProcessBuilder
        ProcessBuilder pb = new ProcessBuilder("ffprobe", "-version");
        pb.redirectErrorStream(true);
        
        // 2️⃣ Tenta iniciar o processo
        //    Se ffprobe não existir, lança IOException aqui
        Process process = pb.start();
        
        // 3️⃣ Aguarda resposta (máximo 5 segundos)
        boolean finished = process.waitFor(5, TimeUnit.SECONDS);
        
        // 4️⃣ Verifica o resultado
        if (!finished) {
            // Demorou demais (timeout)
            process.destroyForcibly();
            log.warn("ffprobe -version timed out...");
            isAvailableFlag = false;
            return;
        }
        
        int exitCode = process.exitValue();
        if (exitCode == 0) {
            // ✓ Sucesso!
            log.info("✓ ffprobe is available and operational at: ffprobe");
            isAvailableFlag = true;
        } else {
            // ✗ Erro
            log.warn("✗ ffprobe exited with code {}", exitCode);
            isAvailableFlag = false;
        }
        
    } catch (IOException e) {
        // Não conseguiu iniciar o processo
        // Isto significa: ffprobe não está em PATH
        log.warn("⚠ ffprobe not found or not executable at path: 'ffprobe'...");
        log.warn("Video analysis endpoints will not work until ffprobe is installed...");
        isAvailableFlag = false;
        // NÃO lança exceção - app continua iniciando
    }
}
```

### Passo 8: Resultado

#### CENÁRIO A: FFprobe Instalado ✅

```
2026-02-17T17:21:34.203  INFO  FfprobeRunner   : 
    Validating ffprobe availability at: ffprobe

2026-02-17T17:21:34.350  INFO  FfprobeRunner   : 
    ✓ ffprobe is available and operational at: ffprobe

2026-02-17T17:21:34.500  INFO  IaDetectorApplication : 
    Started IaDetectorApplication in 1.234 seconds

Server is running on port 8080 ✅
```

**Resultado**: `isAvailableFlag = true`

#### CENÁRIO B: FFprobe NÃO Instalado ❌

```
2026-02-17T17:21:34.203  INFO  FfprobeRunner   : 
    Validating ffprobe availability at: ffprobe

2026-02-17T17:21:34.253  WARN  FfprobeRunner   : 
    ⚠ ffprobe not found or not executable at path: 'ffprobe'. 
    Video analysis endpoints will not work until ffprobe is installed. 
    To fix: Install ffmpeg (includes ffprobe) from https://ffmpeg.org/download.html, 
    or set FFPROBE_PATH environment variable to ffprobe executable path.

2026-02-17T17:21:34.500  INFO  IaDetectorApplication : 
    Started IaDetectorApplication in 1.234 seconds

Server is running on port 8080 ✅
```

**Resultado**: `isAvailableFlag = false`

---

## 🧪 Teste 1: Health Check Endpoint

### Passo 1: Abra um novo PowerShell

```powershell
# A app ainda está rodando no outro PowerShell
curl http://localhost:8080/api/health/ffprobe
```

### Passo 2: Resposta (Se ffprobe INSTALADO)

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "status": "UP",
  "service": "ffprobe",
  "message": "ffprobe is available and operational"
}
```

### Passo 3: Resposta (Se ffprobe NÃO INSTALADO)

```json
HTTP/1.1 503 Service Unavailable
Content-Type: application/json

{
  "status": "DOWN",
  "service": "ffprobe",
  "message": "ffprobe is not available. Ensure ffmpeg is installed and FFPROBE_PATH is configured correctly."
}
```

---

## 🧪 Teste 2: Tentar Upload Sem FFprobe

### Passo 1: Tente fazer upload de um vídeo

```powershell
curl -F "file=@C:\Videos\sample.mp4" http://localhost:8080/api/video/info
```

### Passo 2: Se FFprobe NÃO está instalado, você recebe:

```json
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "code": "ERROR",
  "message": "ffprobe is not available at path: 'ffprobe'. 
             Please install ffmpeg/ffprobe from https://ffmpeg.org/download.html 
             or set FFPROBE_PATH environment variable.",
  "details": null
}
```

### Passo 3: Logs na app mostram:

```
2026-02-17T17:21:45.100  ERROR  FfprobeRunner   : 
    ffprobe is not available at path: 'ffprobe'. 
    Please install ffmpeg/ffprobe from https://ffmpeg.org/download.html
```

---

## 🔧 Teste 3: Instalar FFprobe e Reiniciar

### Passo 1: Instale FFmpeg no PowerShell

```powershell
choco install ffmpeg
```

### Passo 2: Verifique a instalação

```powershell
ffprobe -version
```

**Resultado esperado:**
```
ffprobe version 6.0 Copyright (c) 2007-2023 the FFmpeg developers
...
```

### Passo 3: Mate a app atual

```powershell
# Na janela onde está rodando: Ctrl+C
```

### Passo 4: Reinicie a app

```powershell
./mvnw.cmd spring-boot:run
```

### Passo 5: Agora você vê

```
2026-02-17T17:22:00.203  INFO  FfprobeRunner   : 
    Validating ffprobe availability at: ffprobe

2026-02-17T17:22:00.350  INFO  FfprobeRunner   : 
    ✓ ffprobe is available and operational at: ffprobe
```

### Passo 6: Health check agora retorna:

```powershell
curl http://localhost:8080/api/health/ffprobe
```

```json
{
  "status": "UP",
  "service": "ffprobe",
  "message": "ffprobe is available and operational"
}
```

### Passo 7: Upload agora funciona!

```powershell
curl -F "file=@C:\Videos\sample.mp4" http://localhost:8080/api/video/info
```

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "format": {
    "filename": "sample.mp4",
    "duration": "123.456",
    "size": "52428800",
    ...
  },
  "streams": [
    {
      "codec_type": "video",
      "codec_name": "h264",
      "width": 1920,
      "height": 1080,
      ...
    },
    {
      "codec_type": "audio",
      "codec_name": "aac",
      "sample_rate": "48000",
      ...
    }
  ]
}
```

---

## 📊 Resumo do Fluxo de Execução

```
┌───────────────────────────────────────────────────────────┐
│ 1. Você executa: ./mvnw.cmd spring-boot:run              │
└──────────────────────┬──────────────────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
        ▼                             ▼
    ┌─────────────┐          ┌──────────────────┐
    │ Spring vê   │          │ ProcessBuilder   │
    │ @Component  │          │ executa:         │
    │ classes     │          │ ffprobe -version │
    │             │          │                  │
    │ Encontra    │          │ ┌──────┬────────┐│
    │ FfprobeRunner           │      │        ││
    └──────┬──────┘          │ ✓OK ✗ERR ⏱TIME││
           │                 │      │        ││
           ▼                 └──────┴────────┘│
    ┌─────────────────────────────────────────┤
    │ Chama construtor com @Autowired      │
    │ → validateFfprobeAvailability()      │
    └─────────────────┬─────────────────────┘
                      │
                      ▼
        ┌─────────────────────────────────┐
        │ isAvailableFlag = true/false    │
        │                                 │
        │ Log exibido no console          │
        └─────────────────┬───────────────┘
                          │
                          ▼
        ┌─────────────────────────────────┐
        │ App Iniciada com Sucesso        │
        │ (mesmo se ffprobe faltar!)      │
        └─────────────────┬───────────────┘
                          │
         ┌────────────────┼────────────────┐
         │                │                │
         ▼                ▼                ▼
    GET /health/       POST /api/      Health
    ffprobe           video/info       Status
    Retorna            Falha se
    status            ffprobe não
                      disponível
```

---

## 🎓 O Que Você Aprendeu

1. **O que Spring verifica**: Se `ffprobe -version` consegue executar
2. **Quando verifica**: No startup, quando cria FfprobeRunner bean
3. **Como verifica**: ProcessBuilder + timeout de 5 segundos
4. **O que armazena**: Flag `isAvailableFlag = true/false`
5. **Como usa depois**: Endpoints checam este flag antes de usar ffprobe
6. **Não bloqueia**: App inicia mesmo que ffprobe falte
7. **Como saber status**: Logs no console + `/api/health/ffprobe` endpoint

Pronto! Você agora entende **completamente** como o Spring verifica FFprobe! 🎉

Todos os documentos de explicação foram criados em `docs/`:
- `FFPROBE_VERIFICATION_EXPLAINED.md` - Explicação técnica completa
- `FFPROBE_IMPROVEMENTS.md` - Melhorias futuras sugeridas
- `FFPROBE_VERIFICATION_VISUAL.md` - Diagramas visuais
- Este arquivo - Guia prático passo-a-passo


/**
 * COMO O SPRING VERIFICA SE FFPROBE ESTÁ INSTALADO NA INICIALIZAÇÃO
 * 
 * ============================================================================
 * FLUXO COMPLETO:
 * ============================================================================
 * 
 * 1. SPRING INICIA A APLICAÇÃO
 *    └─ IaDetectorApplication.main() é chamado
 *       └─ SpringApplication.run() inicia o contexto do Spring
 * 
 * 2. CRIAÇÃO DE BEANS (Dependency Injection)
 *    └─ Spring procura por classes marcadas com @Component, @Service, etc.
 *       └─ Encontra: FfprobeRunner.class (marcado com @Component)
 *          └─ Spring vê que tem um construtor com @Autowired
 *             └─ Spring injeta FfprobeProperties neste construtor
 * 
 * 3. CONSTRUTOR DO FfprobeRunner É EXECUTADO
 *    └─ public FfprobeRunner(FfprobeProperties props) { ... }
 *       └─ Chama: this(props.getPath(), Duration.ofSeconds(...))
 *          └─ Inicializa ffprobeCommand com o valor de application.yaml
 *       └─ Chama: validateFfprobeAvailability()
 *          └─ AQUI ACONTECE A VERIFICAÇÃO!
 * 
 * 4. VALIDAÇÃO DO FFPROBE (validateFfprobeAvailability)
 *    
 *    try {
 *        log.info("Validando ffprobe em: {}", ffprobeCommand);
 *        // Cria um ProcessBuilder para executar: ffprobe -version
 *        ProcessBuilder pb = new ProcessBuilder(ffprobeCommand, "-version");
 *        pb.redirectErrorStream(true);
 *        Process process = pb.start();  // ← TENTA INICIAR O PROCESSO
 *        
 *        // Espera até 5 segundos para o processo responder
 *        boolean finished = process.waitFor(5, TimeUnit.SECONDS);
 *        
 *        if (!finished) {
 *            // Ffprobe demorou demais para responder
 *            process.destroyForcibly();
 *            log.warn("ffprobe -version expirou");
 *            isAvailableFlag = false;
 *            return;
 *        }
 *        
 *        // Verifica o código de saída do processo
 *        int exitCode = process.exitValue();
 *        if (exitCode == 0) {
 *            // ✓ SUCESSO! FFprobe está disponível
 *            log.info("✓ ffprobe disponível e operacional em: {}", ffprobeCommand);
 *            isAvailableFlag = true;
 *        } else {
 *            // ✗ Ffprobe retornou erro
 *            log.warn("✗ ffprobe retornou código: {}", exitCode);
 *            isAvailableFlag = false;
 *        }
 *    } catch (IOException e) {
 *        // ✗ NÃO CONSEGUIU INICIAR O PROCESSO
 *        // Isto significa: ffprobe NÃO ESTÁ NO PATH ou NÃO EXISTE
 *        log.warn("⚠ ffprobe não encontrado ou não executável em: '{}'", ffprobeCommand);
 *        isAvailableFlag = false;
 *        // NÃO LANÇA EXCEÇÃO - permite que a app continue iniciando
 *    }
 * 
 * 5. RESULTADO DA INICIALIZAÇÃO
 *    └─ isAvailableFlag = true  → ffprobe está disponível
 *    └─ isAvailableFlag = false → ffprobe não está disponível
 * 
 * 6. APLICAÇÃO CONTINUA INICIANDO
 *    └─ Spring cria HealthController (que injeta FfprobeRunner)
 *    └─ Spring cria VideoInfoService
 *    └─ etc...
 * 
 * 7. APLICAÇÃO INICIA COM SUCESSO
 *    └─ Servidor começa a ouvir na porta 8080
 *    └─ Endpoints estão disponíveis
 * 
 * ============================================================================
 * O QUE MUDA QUANDO VOCÊ CHAMA UM ENDPOINT
 * ============================================================================
 * 
 * CENÁRIO 1: ffprobe DISPONÍVEL (isAvailableFlag = true)
 *    User: curl -F "file=@video.mp4" http://localhost:8080/api/video/info
 *    └─ VideoInfoService.analyzeFile() é chamado
 *       └─ VideoInfoService chama ffprobeRunner.run()
 *          └─ Como isAvailableFlag = true, executa o ffprobe
 *          └─ Retorna VideoInfoDTO com metadados
 * 
 * CENÁRIO 2: ffprobe NÃO DISPONÍVEL (isAvailableFlag = false)
 *    User: curl -F "file=@video.mp4" http://localhost:8080/api/video/info
 *    └─ VideoInfoService.analyzeFile() é chamado
 *       └─ VideoInfoService chama ffprobeRunner.run()
 *          └─ Como isAvailableFlag = false, LANÇA IOException
 *          └─ GlobalExceptionHandler pega a exception
 *          └─ Retorna 500 ou 422 com mensagem de erro clara
 * 
 * ============================================================================
 * COMO FUNCIONA A VERIFICAÇÃO TÉCNICA
 * ============================================================================
 * 
 * 1. PROCESSBUILDER (ProcessBuilder)
 *    └─ Ferramenta Java para executar processos do sistema operacional
 *    └─ Equivalente a digitar em um terminal/console
 * 
 * 2. COMANDO EXECUTADO
 *    └─ Windows: ffprobe -version
 *    └─ Linux/Mac: ffprobe -version
 *    └─ Isto apenas EXIBE A VERSÃO DO FFPROBE e sai
 * 
 * 3. SE FFPROBE ESTÁ NO PATH
 *    Windows PATH: C:\Program Files\ffmpeg\bin
 *    └─ ProcessBuilder consegue encontrar "ffprobe"
 *    └─ Executa: C:\Program Files\ffmpeg\bin\ffprobe.exe -version
 *    └─ Retorna: exit code 0 (sucesso)
 * 
 * 4. SE FFPROBE NÃO ESTÁ NO PATH
 *    └─ ProcessBuilder NÃO consegue encontrar "ffprobe"
 *    └─ Lança: IOException("Cannot run program "ffprobe": ...")
 *    └─ isAvailableFlag = false
 * 
 * 5. TIMEOUT DE 5 SEGUNDOS
 *    └─ process.waitFor(5, TimeUnit.SECONDS)
 *    └─ Aguarda até 5 segundos para ffprobe responder
 *    └─ Se exceder, força interrupção: process.destroyForcibly()
 * 
 * ============================================================================
 * LOGS QUE VOCÊ VÊ NO STARTUP
 * ============================================================================
 * 
 * CASO 1: FFprobe ENCONTRADO
 *    2026-02-17T17:21:34.203 INFO  ... Validating ffprobe availability at: ffprobe
 *    2026-02-17T17:21:34.350 INFO  ... ✓ ffprobe is available and operational at: ffprobe
 * 
 * CASO 2: FFprobe NÃO ENCONTRADO
 *    2026-02-17T17:21:34.203 INFO  ... Validating ffprobe availability at: ffprobe
 *    2026-02-17T17:21:34.253 WARN  ... ⚠ ffprobe not found or not executable at path: 'ffprobe'...
 *    2026-02-17T17:21:34.253 WARN  ... Video analysis endpoints will not work until ffprobe is installed...
 * 
 * CASO 3: FFprobe TIMEOUT
 *    2026-02-17T17:21:34.203 INFO  ... Validating ffprobe availability at: ffprobe
 *    2026-02-17T17:21:39.305 WARN  ... ffprobe -version timed out during startup validation
 * 
 * ============================================================================
 * DIAGRAMA DO FLUXO
 * ============================================================================
 * 
 *     Spring Boot Inicia
 *            ↓
 *     Procura @Component classes
 *            ↓
 *     Encontra FfprobeRunner
 *            ↓
 *     Chama construtor com @Autowired
 *            ↓
 *     validateFfprobeAvailability() é executado
 *            ↓
 *      ┌─────┴──────┐
 *      ↓             ↓
 *   ProcessBuilder   ProcessBuilder
 *   consegue rodar   NÃO consegue
 *   ffprobe          rodar ffprobe
 *      ↓             ↓
 *   isAvailable    IOException é
 *   Flag = true    capturada
 *      ↓             ↓
 *   Log INFO:      isAvailable
 *   "✓ ffprobe     Flag = false
 *   disponível"       ↓
 *      ↓             Log WARN:
 *   App continua   "⚠ ffprobe
 *   iniciando      não encontrado"
 *      ↓             ↓
 *   ┌──────────────────┐
 *   ↓                  ↓
 *   App inicia com   App inicia com
 *   ffprobe OK       ffprobe DESABILITADO
 * 
 * ============================================================================
 * CÓDIGO CHAVE
 * ============================================================================
 */

// No construtor do FfprobeRunner:
@Autowired
public FfprobeRunner(FfprobeProperties props) {
    // Inicializa variáveis
    this(props.getPath(), Duration.ofSeconds(props.getTimeoutSeconds()));
    
    // ← AQUI A MÁGICA ACONTECE!
    validateFfprobeAvailability();
    // Esta chamada executa ffprobe -version e armazena o resultado em isAvailableFlag
}

// Verificação propriamente dita:
private void validateFfprobeAvailability() {
    try {
        // Cria um "comando do terminal" que executará: ffprobe -version
        ProcessBuilder pb = new ProcessBuilder(ffprobeCommand, "-version");
        pb.redirectErrorStream(true);
        
        // TENTA EXECUTAR O COMANDO
        Process process = pb.start();
        
        // ESPERA O COMANDO TERMINAR (com timeout de 5 segundos)
        boolean finished = process.waitFor(5, TimeUnit.SECONDS);
        
        if (finished) {
            // Obtém o código de saída (0 = sucesso, outros = erro)
            int exitCode = process.exitValue();
            
            if (exitCode == 0) {
                // ✓ SUCESSO!
                log.info("✓ ffprobe is available and operational");
                isAvailableFlag = true;  // ← MARCA COMO DISPONÍVEL
            }
        }
    } catch (IOException e) {
        // ✗ NÃO CONSEGUIU INICIAR O PROCESSO
        // Isto ocorre quando ffprobe NÃO ESTÁ NO PATH
        log.warn("⚠ ffprobe not found: {}", e.getMessage());
        isAvailableFlag = false;  // ← MARCA COMO INDISPONÍVEL
    }
}

// Quando um endpoint tenta usar o ffprobe:
public String run(String pathOrUrl) throws IOException {
    // Verifica se ffprobe está disponível ANTES de usar
    if (!isAvailableFlag) {
        throw new IOException("ffprobe is not available. Install ffmpeg.");
        // ↑ Este erro é capturado por GlobalExceptionHandler
        // que retorna HTTP 500/422 com mensagem amigável
    }
    
    // Se chegou aqui, ffprobe está disponível
    // Então executa normalmente...
}

/**
 * ============================================================================
 * RESUMO EXECUTIVO
 * ============================================================================
 * 
 * O QUE ACONTECE:
 * 1. Spring Boot inicia
 * 2. Encontra classe FfprobeRunner marcada com @Component
 * 3. Chama seu construtor (que tem @Autowired)
 * 4. Construtor chama validateFfprobeAvailability()
 * 5. validateFfprobeAvailability():
 *    - Cria um ProcessBuilder que executaria "ffprobe -version"
 *    - Tenta iniciar este processo
 *    - Se conseguir iniciar e retornar código 0 → DISPONÍVEL ✓
 *    - Se NÃO conseguir (IOException) → NÃO DISPONÍVEL ✗
 *    - Se demorar mais de 5 segundos → NÃO DISPONÍVEL ✗
 * 6. Spring continua iniciando (não bloqueia mesmo se ffprobe faltar)
 * 7. App inicia com flag isAvailableFlag = true ou false
 * 8. Quando um endpoint tenta usar ffprobe:
 *    - Se isAvailableFlag = true → executa ffprobe normalmente
 *    - Se isAvailableFlag = false → retorna erro com mensagem clara
 * 
 * BENEFÍCIO:
 * - App sempre inicia (mesmo sem ffprobe)
 * - Usuário vê aviso nos logs no startup
 * - Se tentar usar endpoint sem ffprobe, recebe erro claro
 * - Health check endpoint mostra status do ffprobe
 */


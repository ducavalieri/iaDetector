# 📚 Índice de Documentação - Verificação de FFprobe no Spring

## 🎯 Rápida Resposta: Como o Spring Verifica FFprobe?

### Em 30 Segundos:

1. **Quando**: No startup da aplicação
2. **Onde**: Construtor do `FfprobeRunner.java` (linha ~27)
3. **Como**: Executa `ffprobe -version` via `ProcessBuilder`
4. **Resultado**: Define flag `isAvailableFlag = true/false`
5. **Impacto**: 
   - Se `true` → endpoints funcionam
   - Se `false` → endpoints retornam erro claro
6. **Não bloqueia**: App inicia mesmo sem ffprobe

---

## 📖 Documentos de Explicação

### 1. **FFPROBE_VERIFICATION_EXPLAINED.md** ⭐ Comece aqui
   - Explicação técnica completa
   - Fluxo detalhado passo-a-passo
   - Diagramas textuais
   - **Tempo de leitura**: 5-10 minutos

   **Seções principais:**
   - Fluxo Completo (7 passos)
   - O que muda quando chama endpoint
   - Como funciona a verificação técnica
   - Logs que você vê no startup
   - Código chave comentado

---

### 2. **FFPROBE_VERIFICATION_VISUAL.md** 📊 Visual learners
   - Diagramas ASCII detalhados
   - Fluxogramas visuais
   - Zoom em cada seção
   - **Tempo de leitura**: 10-15 minutos

   **Diagramas:**
   - Fluxo completo do startup
   - O que acontece dentro de validateFfprobeAvailability()
   - Fluxo quando user chama um endpoint
   - Logs esperados para cada cenário
   - Resumo visual final

---

### 3. **FFPROBE_PRACTICAL_GUIDE.md** 🔧 Hands-on
   - Guia passo-a-passo prático
   - Exemplos reais de execução
   - Cenários com saída esperada
   - **Tempo de leitura**: 10 minutos

   **Seções:**
   - Passo 1-8 da verificação na prática
   - Teste 1: Health check endpoint
   - Teste 2: Upload sem ffprobe
   - Teste 3: Instalar ffprobe e reiniciar
   - Resumo do fluxo de execução
   - O que você aprendeu

---

### 4. **FFPROBE_IMPROVEMENTS.md** 🚀 Futuro
   - Melhorias sugeridas para o código
   - Logging mais detalhado
   - Listener de inicialização do Spring
   - Verificação em tempo de execução
   - Métricas e alertas
   - **Tempo de leitura**: 5-10 minutos

   **Melhorias:**
   - Logging com timestamps
   - ApplicationStartupListener para resumo
   - Verificação dinâmica endpoint
   - Integração com Prometheus
   - Notificações por email

---

## 🔍 Como Encontrar o Código

### Arquivo Principal: `FfprobeRunner.java`

```
Caminho: C:\Users\eduar\projects\iaDetector\
         src\main\java\com\iaDetector\api\service\
         FfprobeRunner.java

Linhas-chave:
- Linha 21: Classe @Component
- Linha 27: Construtor com @Autowired
- Linha 29: Chamada para validateFfprobeAvailability()
- Linha 40: Método validateFfprobeAvailability()
- Linha 48: ProcessBuilder criado
- Linha 50: pb.start() - executa ffprobe
```

### Arquivo de Configuração: `application.yaml`

```
Caminho: C:\Users\eduar\projects\iaDetector\
         src\main\resources\
         application.yaml

Seção ffprobe:
ffprobe:
  path: ffprobe           # Lido pelo FfprobeProperties
  timeout-seconds: 30     # Timeout da verificação
```

### Classe de Configuração: `FfprobeProperties.java`

```
Caminho: C:\Users\eduar\projects\iaDetector\
         src\main\java\com\iaDetector\api\config\
         FfprobeProperties.java

@ConfigurationProperties(prefix = "ffprobe")
- path: String
- timeoutSeconds: int
```

### Health Check: `HealthController.java`

```
Caminho: C:\Users\eduar\projects\iaDetector\
         src\main\java\com\iaDetector\api\controller\
         HealthController.java

Endpoint: GET /api/health/ffprobe
Método: checkFfprobe()
```

---

## 📊 Árvore de Chamadas

```
IaDetectorApplication.main()
    ↓
SpringApplication.run()
    ↓
Spring Boot Context Initialization
    ↓
Component Scanning (@Component)
    ↓
FfprobeRunner detected
    ↓
Constructor Injection (@Autowired)
    ↓
FfprobeRunner(FfprobeProperties props)
    ↓
validateFfprobeAvailability() ← VERIFICAÇÃO ACONTECE AQUI
    ↓
ProcessBuilder.start("ffprobe -version")
    ↓
┌─ processBuilder.waitFor(5, SECONDS)
│   ├─ exit code 0? → isAvailableFlag = true
│   ├─ timeout?    → isAvailableFlag = false
│   └─ other?      → isAvailableFlag = false
│
└─ IOException?   → isAvailableFlag = false

    ↓
Spring Continue Bean Creation
    ↓
Application Ready
```

---

## 🎯 Roadmap de Leitura

### Iniciante (New to Spring)
1. Leia: `FFPROBE_VERIFICATION_VISUAL.md`
2. Depois: `FFPROBE_VERIFICATION_EXPLAINED.md`
3. Por fim: `FFPROBE_PRACTICAL_GUIDE.md`

### Intermediário (Conhece Spring)
1. Leia: `FFPROBE_VERIFICATION_EXPLAINED.md`
2. Depois: `FFPROBE_PRACTICAL_GUIDE.md`
3. Explore: Código em `FfprobeRunner.java`

### Avançado (Spring Expert)
1. Veja: Código em `FfprobeRunner.java`
2. Leia: `FFPROBE_IMPROVEMENTS.md`
3. Implemente as melhorias sugeridas

---

## ❓ Perguntas Frequentes

### P1: Por que ProcessBuilder?
**R**: Permite executar programas do sistema operacional a partir do Java.

### P2: Por que `ffprobe -version`?
**R**: Comando simples que apenas testa se ffprobe é executável. Não processa nenhum arquivo.

### P3: Por que timeout de 5 segundos?
**R**: Evita travamento da aplicação se ffprobe responder muito lentamente.

### P4: Por que não bloqueia startup?
**R**: Permite que a app inicie mesmo sem ffprobe e use outros endpoints.

### P5: Como sei se ffprobe está disponível?
**R**: Verifique logs no startup OU chame GET `/api/health/ffprobe`.

### P6: O que fazer se ffprobe não for encontrado?
**R**: 
1. Instale ffmpeg (inclui ffprobe)
2. Ou defina FFPROBE_PATH apontando para o executável
3. Reinicie a aplicação

### P7: Pode ser desabilitado?
**R**: Sim, remova a chamada para `validateFfprobeAvailability()` no construtor.

### P8: Testa em tempo de execução?
**R**: Sim, através do endpoint `GET /api/health/ffprobe` (criado dinamicamente).

---

## 🔗 Links Importantes

### Arquivos da Aplicação
- FfprobeRunner.java: `src/main/java/.../service/FfprobeRunner.java`
- FfprobeProperties.java: `src/main/java/.../config/FfprobeProperties.java`
- HealthController.java: `src/main/java/.../controller/HealthController.java`
- application.yaml: `src/main/resources/application.yaml`

### Documentação Online
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [ProcessBuilder Java Docs](https://docs.oracle.com/javase/17/docs/api/java.base/java/lang/ProcessBuilder.html)
- [FFmpeg Download](https://ffmpeg.org/download.html)

### Repositório
- Código-fonte: `/src/main/java/com/iaDetector/`
- Testes: `/src/test/java/com/iaDetector/`
- Configuração: `/src/main/resources/`

---

## 📝 Checklist: O que Você Deve Entender

- [ ] O que é ProcessBuilder
- [ ] Por que usa `ffprobe -version`
- [ ] O que é timeout
- [ ] O que é flag boolean
- [ ] Como Spring injeta dependências (@Autowired)
- [ ] O que é @Component e @Service
- [ ] Como o Spring cria beans
- [ ] Por que ProcessBuilder é chamado no construtor
- [ ] Como IOException é capturada
- [ ] Por que app não bloqueia se ffprobe faltar
- [ ] Como verificar status via health endpoint
- [ ] Onde encontrar logs de verificação

---

## 🚀 Próximos Passos

1. **Compreensão**: Leia `FFPROBE_VERIFICATION_EXPLAINED.md`
2. **Visualização**: Estude `FFPROBE_VERIFICATION_VISUAL.md`
3. **Prática**: Siga `FFPROBE_PRACTICAL_GUIDE.md`
4. **Experimente**: Execute a app e observe os logs
5. **Melhore**: Considere implementar sugestões de `FFPROBE_IMPROVEMENTS.md`

---

## 💡 Dica Final

A verificação do ffprobe é um **padrão importante** em aplicações que dependem de executáveis externos:

```
┌─────────────────────────────────────────┐
│ Padrão Geral de Verificação Externa     │
│                                         │
│ 1. Detectar dependência no startup      │
│ 2. Não bloquear startup se faltar       │
│ 3. Armazenar status em flag/variable    │
│ 4. Checar flag antes de usar            │
│ 5. Retornar erro claro se não disponível│
│ 6. Fornecer health check endpoint       │
│ 7. Logs informativos                    │
└─────────────────────────────────────────┘
```

Você pode reutilizar este padrão para outras dependências externas!

---

**Criado em**: 2026-02-17  
**Versão**: 1.0  
**Status**: Completo ✅  

Para dúvidas, consulte a documentação acima ou examine o código em `FfprobeRunner.java`.


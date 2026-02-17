# Melhorias Futuras para Verificação do FFprobe

## 1. Adicionar Logging mais Detalhado

Você pode melhorar o `FfprobeRunner` para coletar mais informações:

```java
private void validateFfprobeAvailability() {
    try {
        log.info("🔍 Iniciando verificação do ffprobe...");
        log.info("   Caminho configurado: {}", ffprobeCommand);
        
        ProcessBuilder pb = new ProcessBuilder(ffprobeCommand, "-version");
        pb.redirectErrorStream(true);
        
        long startTime = System.currentTimeMillis();
        Process process = pb.start();
        
        boolean finished = process.waitFor(5, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;
        
        if (!finished) {
            process.destroyForcibly();
            log.warn("⏱️  ffprobe -version expirou (>{} ms)", duration);
            isAvailableFlag = false;
            return;
        }
        
        String output = readStream(process.getInputStream());
        int exitCode = process.exitValue();
        
        log.debug("   Saída: {}", output.split("\n")[0]); // Primeira linha
        log.debug("   Tempo: {} ms", duration);
        
        if (exitCode == 0) {
            log.info("✅ FFprobe DISPONÍVEL ({}ms)", duration);
            isAvailableFlag = true;
        } else {
            log.warn("❌ FFprobe retornou código: {} ({}ms)", exitCode, duration);
            isAvailableFlag = false;
        }
        
    } catch (IOException e) {
        log.warn("❌ FFprobe NÃO ENCONTRADO em: {}", ffprobeCommand);
        log.warn("   Erro: {}", e.getMessage());
        log.info("   💡 Dica: Instale ffmpeg ou defina FFPROBE_PATH");
        isAvailableFlag = false;
    } catch (InterruptedException e) {
        log.warn("⚠️  Verificação interrompida: {}", e.getMessage());
        Thread.currentThread().interrupt();
        isAvailableFlag = false;
    }
}
```

## 2. Criar Listener de Inicialização do Spring

Você pode criar uma classe que monitora eventos de inicialização:

```java
package com.iaDetector.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.iaDetector.api.service.FfprobeRunner;

@Component
public class ApplicationStartupListener {
    
    private static final Logger log = LoggerFactory.getLogger(ApplicationStartupListener.class);
    private final FfprobeRunner ffprobeRunner;
    
    public ApplicationStartupListener(FfprobeRunner ffprobeRunner) {
        this.ffprobeRunner = ffprobeRunner;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("════════════════════════════════════════════════════════");
        log.info("             APLICAÇÃO INICIADA COM SUCESSO             ");
        log.info("════════════════════════════════════════════════════════");
        
        if (ffprobeRunner.isAvailable()) {
            log.info("✅ Análise de vídeo: ATIVADA");
            log.info("   Endpoints disponíveis:");
            log.info("   - POST /api/video/info");
            log.info("   - POST /api/video/info-from-url");
        } else {
            log.warn("⚠️  Análise de vídeo: DESATIVADA (ffprobe não encontrado)");
            log.warn("   Para ativar:");
            log.warn("   1. Instale ffmpeg de https://ffmpeg.org/download.html");
            log.warn("   2. Reinicie a aplicação");
        }
        
        log.info("✅ Health check: GET /api/health/ffprobe");
        log.info("════════════════════════════════════════════════════════");
    }
}
```

## 3. Adicionar Verificação em Tempo de Execução

Você pode criar um endpoint que re-verifica o ffprobe:

```java
@GetMapping("/ffprobe/verify")
public ResponseEntity<?> verifyFfprobe() {
    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", System.currentTimeMillis());
    
    if (ffprobeRunner.isAvailable()) {
        response.put("status", "UP");
        response.put("version", "Disponível");
        return ResponseEntity.ok(response);
    } else {
        response.put("status", "DOWN");
        response.put("reason", "ffprobe não encontrado");
        response.put("installation_url", "https://ffmpeg.org/download.html");
        return ResponseEntity.status(503).body(response);
    }
}
```

## 4. Adicionar Métricas

Com Spring Actuator você pode monitorar o status:

```java
@Component
public class FfprobeMetrics {
    
    private final MeterRegistry meterRegistry;
    private final FfprobeRunner ffprobeRunner;
    
    public FfprobeMetrics(MeterRegistry meterRegistry, FfprobeRunner ffprobeRunner) {
        this.meterRegistry = meterRegistry;
        this.ffprobeRunner = ffprobeRunner;
        
        // Cria gauge que retorna 1 se disponível, 0 se não
        Gauge.builder("ffprobe.available", 
                      () -> ffprobeRunner.isAvailable() ? 1 : 0)
            .description("FFprobe availability status")
            .register(meterRegistry);
    }
}
```

## 5. Adicionar Notificação por Email (Opcional)

Para produção, você pode notificar administrador se ffprobe ficar indisponível:

```java
@Component
public class FfprobeHealthNotifier {
    
    private final FfprobeRunner ffprobeRunner;
    private final EmailService emailService;
    
    @Scheduled(fixedDelay = 300000) // A cada 5 minutos
    public void checkFfprobeHealth() {
        if (!ffprobeRunner.isAvailable()) {
            emailService.send(
                "admin@example.com",
                "ALERTA: FFprobe indisponível no servidor",
                "Verifique a instalação do ffmpeg"
            );
        }
    }
}
```

## Resumo das Melhorias

| Melhoria | Benefício | Complexidade |
|----------|-----------|--------------|
| Logging detalhado | Melhor diagnóstico | Baixa |
| Listener de inicialização | Informações no startup | Média |
| Verificação em tempo real | Monitore status live | Média |
| Métricas | Integração com Prometheus | Alta |
| Notificações | Alertas automáticos | Alta |

Todas estas melhorias são **opcionais**. A implementação atual já funciona bem! 🚀


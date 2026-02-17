package com.iaDetector.api.controller;

import com.iaDetector.api.dto.ErrorDTO;
import com.iaDetector.api.service.FfprobeRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final FfprobeRunner ffprobeRunner;

    public HealthController(FfprobeRunner ffprobeRunner) {
        this.ffprobeRunner = ffprobeRunner;
    }

    /**
     * Health check endpoint for ffprobe availability.
     * Returns 200 OK if ffprobe is available, 503 Service Unavailable otherwise.
     */
    @GetMapping("/ffprobe")
    public ResponseEntity<?> checkFfprobe() {
        Map<String, Object> response = new HashMap<>();

        if (ffprobeRunner.isAvailable()) {
            response.put("status", "UP");
            response.put("service", "ffprobe");
            response.put("message", "ffprobe is available and operational");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "DOWN");
            response.put("service", "ffprobe");
            response.put("message", "ffprobe is not available. Ensure ffmpeg is installed and FFPROBE_PATH is configured correctly.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}


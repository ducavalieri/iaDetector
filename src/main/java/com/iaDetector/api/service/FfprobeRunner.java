package com.iaDetector.api.service;

import com.iaDetector.api.config.FfprobeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
public class FfprobeRunner {

    private static final Logger log = LoggerFactory.getLogger(FfprobeRunner.class);

    private final String ffprobeCommand;
    private final Duration timeout;
    private boolean isAvailableFlag;

    @Autowired
    public FfprobeRunner(FfprobeProperties props) {
        this(props.getPath(), Duration.ofSeconds(props.getTimeoutSeconds()));
        validateFfprobeAvailability();
    }

    public FfprobeRunner(String ffprobeCommand, Duration timeout) {
        this.ffprobeCommand = ffprobeCommand;
        this.timeout = timeout;
        this.isAvailableFlag = false;
    }

    /**
     * Validates that ffprobe is available by attempting to run: ffprobe -version
     * Does NOT block startup if ffprobe is missing; only logs warnings.
     * Call run() method will fail if ffprobe is not available.
     */
    private void validateFfprobeAvailability() {
        try {
            log.info("Validating ffprobe availability at: {}", ffprobeCommand);
            ProcessBuilder pb = new ProcessBuilder(ffprobeCommand, "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.warn("ffprobe -version timed out during startup validation");
                isAvailableFlag = false;
                return;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("✓ ffprobe is available and operational at: {}", ffprobeCommand);
                isAvailableFlag = true;
            } else {
                log.warn("✗ ffprobe exited with code {} during validation", exitCode);
                isAvailableFlag = false;
            }
        } catch (IOException e) {
            String errorMsg = String.format(
                "⚠ ffprobe not found or not executable at path: '%s'. " +
                "Video analysis endpoints will not work until ffprobe is installed. " +
                "To fix: Install ffmpeg (includes ffprobe) from https://ffmpeg.org/download.html, " +
                "or set FFPROBE_PATH environment variable to ffprobe executable path.",
                ffprobeCommand
            );
            log.warn(errorMsg);
            isAvailableFlag = false;
            // Do NOT throw exception - allow app to start, just warn
        } catch (InterruptedException e) {
            log.warn("ffprobe validation interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
            isAvailableFlag = false;
        }
    }

    public String run(String pathOrUrl) throws IOException, InterruptedException, TimeoutException, ExecutionException {
        if (!isAvailableFlag) {
            String errorMsg = String.format(
                "ffprobe is not available at path: '%s'. " +
                "Please install ffmpeg/ffprobe from https://ffmpeg.org/download.html " +
                "or set FFPROBE_PATH environment variable.",
                ffprobeCommand
            );
            log.error(errorMsg);
            throw new IOException(errorMsg);
        }

        log.debug("Running ffprobe on [{}] with command [{}] and timeout {}ms", pathOrUrl, ffprobeCommand, timeout.toMillis());

        List<String> command = new ArrayList<>();
        command.add(ffprobeCommand);
        command.add("-v");
        command.add("quiet");
        command.add("-print_format");
        command.add("json");
        command.add("-show_format");
        command.add("-show_streams");
        command.add(pathOrUrl);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process;

        try {
            process = pb.start();
        } catch (IOException e) {
            String errorMsg = String.format(
                "Failed to start ffprobe process: %s. " +
                "ffprobe command: '%s'. " +
                "Ensure ffprobe is installed and FFPROBE_PATH is correctly configured.",
                e.getMessage(), ffprobeCommand
            );
            log.error(errorMsg);
            throw new IOException(errorMsg, e);
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> outFuture = executor.submit(() -> readStream(process.getInputStream()));

        try {
            boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.warn("ffprobe timed out for {}", pathOrUrl);
                throw new TimeoutException("ffprobe timed out");
            }
            String output = outFuture.get(1, TimeUnit.SECONDS);
            int exit = process.exitValue();
            if (exit != 0) {
                log.error("ffprobe exited with code {} for {}: {}", exit, pathOrUrl, output);
                throw new IOException("ffprobe exited with code " + exit + ": " + output);
            }
            log.debug("ffprobe finished successfully for {} ({} bytes)", pathOrUrl, output == null ? 0 : output.length());
            return output;
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Check if ffprobe is available without running analysis.
     * Returns true if ffprobe was found during validation, false otherwise.
     */
    public boolean isAvailable() {
        return isAvailableFlag;
    }

    private String readStream(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }
}

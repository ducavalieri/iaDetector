package com.iaDetector.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iaDetector.api.dto.StreamDTO;
import com.iaDetector.api.dto.VideoInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

@Service
public class VideoInfoService implements IVideoInfoService {

    private static final Logger log = LoggerFactory.getLogger(VideoInfoService.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final FfprobeRunner runner;

    public VideoInfoService(FfprobeRunner runner) {
        this.runner = runner;
    }

    @Override
    public VideoInfoDTO analyzeFile(MultipartFile file) throws IOException, InterruptedException, TimeoutException, ExecutionException {
        log.info("analyzeFile: received file [{}] size={} bytes", file == null ? null : file.getOriginalFilename(), file == null ? 0 : file.getSize());

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Path tempFile = Files.createTempFile("upload-", ".bin");
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        try {
            String json = runner.run(tempFile.toAbsolutePath().toString());
            VideoInfoDTO dto = parseFfprobeJson(json);
            log.info("analyzeFile: parsed {} streams", dto.getStreams() == null ? 0 : dto.getStreams().size());
            return dto;

        } catch (Exception e) {
            log.error("Error analyzing file [{}]: {}", file.getOriginalFilename(), e.getMessage(), e);
            throw e;
        }

        finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public VideoInfoDTO analyzeUrl(String url) throws IOException, InterruptedException, TimeoutException, ExecutionException {
        log.info("analyzeUrl: url={}", url);
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL is empty");
        }
        String json = runner.run(url);
        VideoInfoDTO dto = parseFfprobeJson(json);
        log.info("analyzeUrl: parsed {} streams", dto.getStreams() == null ? 0 : dto.getStreams().size());
        return dto;
    }

    private VideoInfoDTO parseFfprobeJson(String json) throws IOException {
        JsonNode root = mapper.readTree(json);
        VideoInfoDTO dto = new VideoInfoDTO();

        if (root.has("format")) {
            JsonNode format = root.get("format");
            dto.setFormat(mapper.convertValue(format, java.util.Map.class));
        }

        List<StreamDTO> streams = new ArrayList<>();
        if (root.has("streams")) {
            Iterator<JsonNode> it = root.get("streams").elements();
            while (it.hasNext()) {
                JsonNode s = it.next();
                StreamDTO sd = mapper.convertValue(s, StreamDTO.class);
                streams.add(sd);
            }
        }
        dto.setStreams(streams);
        return dto;
    }
}

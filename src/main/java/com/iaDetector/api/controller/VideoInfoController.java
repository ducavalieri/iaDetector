package com.iaDetector.api.controller;

import com.iaDetector.api.dto.UrlRequestDTO;
import com.iaDetector.api.dto.VideoInfoDTO;
import com.iaDetector.api.service.IVideoInfoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/video")
public class VideoInfoController {

    private final IVideoInfoService service;

    public VideoInfoController(IVideoInfoService service) {
        this.service = service;
    }

    @PostMapping(path = "/info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VideoInfoDTO> postVideoInfo(@RequestPart("file") MultipartFile file) throws Exception {
        VideoInfoDTO result = service.analyzeFile(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping(path = "/info-from-url", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VideoInfoDTO> postVideoInfoFromUrl(@RequestBody UrlRequestDTO req) throws Exception {
        VideoInfoDTO result = service.analyzeUrl(req.getUrl());
        return ResponseEntity.ok(result);
    }
}

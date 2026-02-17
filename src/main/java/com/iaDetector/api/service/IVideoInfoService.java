package com.iaDetector.api.service;

import com.iaDetector.api.dto.VideoInfoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface IVideoInfoService {
    VideoInfoDTO analyzeFile(MultipartFile file) throws IOException, InterruptedException, TimeoutException, ExecutionException;
    VideoInfoDTO analyzeUrl(String url) throws IOException, InterruptedException, TimeoutException, ExecutionException;
}


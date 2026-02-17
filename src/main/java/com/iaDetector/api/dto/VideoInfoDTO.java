package com.iaDetector.api.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class VideoInfoDTO {
    private Map<String, Object> format;
    private List<StreamDTO> streams;
}

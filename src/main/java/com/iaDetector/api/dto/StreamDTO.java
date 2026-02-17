package com.iaDetector.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StreamDTO {
    private String codec_type;
    private String codec_name;
    private Integer width;
    private Integer height;
    private String r_frame_rate;
    private String sample_rate;
    private Integer channels;
    private String bit_rate;
}

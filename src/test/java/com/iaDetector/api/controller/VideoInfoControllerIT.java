package com.iaDetector.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iaDetector.api.dto.StreamDTO;
import com.iaDetector.api.dto.VideoInfoDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VideoInfoControllerIT {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void dto_serializes_and_deserializes() throws Exception {
        VideoInfoDTO dto = new VideoInfoDTO();
        StreamDTO s = new StreamDTO();
        s.setCodec_name("h264");
        s.setCodec_type("video");
        dto.setStreams(List.of(s));
        dto.setFormat(Map.of("duration", "10.0"));

        String json = mapper.writeValueAsString(dto);
        VideoInfoDTO parsed = mapper.readValue(json, VideoInfoDTO.class);

        assertEquals(1, parsed.getStreams().size());
        assertEquals("h264", parsed.getStreams().get(0).getCodec_name());
        assertEquals("10.0", parsed.getFormat().get("duration"));
    }
}

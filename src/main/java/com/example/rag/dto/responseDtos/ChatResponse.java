package com.example.rag.dto.responseDtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

import com.example.rag.dto.nestedDtos.SourceReference;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private String answer;
    private List<SourceReference> sources;
    private Integer totalSources;
    private Long processingTimeMs;
}
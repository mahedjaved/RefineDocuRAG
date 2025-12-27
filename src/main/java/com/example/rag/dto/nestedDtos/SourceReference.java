package com.example.rag.dto.nestedDtos;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceReference {
    private String documentName;
    private String content;
    private Double relevanceScore;
    private Integer pageNumber;
    private Integer chunkIndex;
}

package com.example.rag.dto.responseDtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentInfo {
    private Long id;
    private String fileName;
    private Long fileSize;
    private Integer pageCount;
    private Integer chunkCount;
    private String status;
    private String uploadedAt;
}

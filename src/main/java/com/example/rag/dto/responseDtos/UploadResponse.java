package com.example.rag.dto.responseDtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResponse {
    private Long documentId;
    private String fileName;
    private String status;
    private Integer pageCount;
    private Integer chunkCount;
    private String message;
}

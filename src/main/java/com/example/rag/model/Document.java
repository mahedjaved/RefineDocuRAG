package com.example.rag.model;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;



@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    private Long fileSize;
    private Integer totalChunks;

    @Enumerated(value = EnumType.STRING)
    private ProcessingStatus status;

    // a single pdf document can have multiple chunks
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentChunk> chunks = new ArrayList<>();

    public enum ProcessingStatus{
        UPLOADING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}
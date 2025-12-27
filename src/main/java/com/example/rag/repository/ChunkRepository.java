package com.example.rag.repository;

import org.springframework.stereotype.Repository;

import com.example.rag.model.rag.DocumentChunk;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ChunkRepository extends JpaRepository<DocumentChunk, Long> {
    /**
     * Two search logic we wish to encode in this spring repo
     * -1- Find by status and order it by time uploaded time
     * -2- Find all documents and order them by uploaded time
     */
    List<DocumentChunk> findByDocumentIdOrderByChunkIndex(Long documentId);
    List<DocumentChunk> findByDocumentId(Long documentId);
}

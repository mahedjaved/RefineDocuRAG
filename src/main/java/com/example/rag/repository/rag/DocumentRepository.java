package com.example.rag.repository.rag;

import org.springframework.stereotype.Repository;

import com.example.rag.model.rag.Document;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    /**
     * Two search logic we wish to encode in this spring repo
     * -1- Find by status and order it by time uploaded time
     * -2- Find all documents and order them by uploaded time
     */
    List<Document> findByStatusOrderByUploadedAtDesc(Document.ProcessingStatus status);
    List<Document> findAllByOrderByUploadedAtDesc();
}

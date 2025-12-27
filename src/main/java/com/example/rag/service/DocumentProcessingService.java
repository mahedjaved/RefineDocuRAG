package com.example.rag.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.rag.model.rag.Document;
import com.example.rag.model.rag.DocumentChunk;
import com.example.rag.repository.ChunkRepository;
import com.example.rag.repository.DocumentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessingService {
    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final ChunkingService chunkingService;
    private final OllamaService ollamaService;

    @Transactional
    public Document processDocument(MultipartFile file) throws Exception {
        log.info("Processing document: {}", file.getOriginalFilename());

        Document document = new Document();
        document.setOriginalFilename(file.getOriginalFilename());
        document.setFilename(file.getOriginalFilename());
        document.setFileSize(file.getSize());
        document.setUploadedAt(LocalDateTime.now());
        document.setStatus(Document.ProcessingStatus.PROCESSING);
        documentRepository.save(document);

        // pdf extraction process
        try {
            String text = extractTextFromPDF(file.getInputStream());
            List<String> chunks = chunkingService.chunkText(text, 500, 50);

            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunk chunk = new DocumentChunk();
                chunk.setDocument(document);
                chunk.setChunkIndex(i);
                chunk.setContent(chunks.get(i));
                chunk.setStartPosition(i * 450);
                chunk.setEndPosition((i + 1) * 450);

                String embedding = ollamaService.generateEmbedding((chunks.get(i)));
                chunk.setEmbedding(embedding);
                
                chunkRepository.save(chunk);
            }

            document.setTotalChunks(chunks.size());
            document.setStatus(Document.ProcessingStatus.COMPLETED);
            log.info("Document processed successfully: {} chunks", chunks.size());

        } catch (Exception e) {
            log.error("Failed to process document", e);
            document.setStatus(Document.ProcessingStatus.FAILED);
            throw e;
        } finally {
            documentRepository.save(document);
        }

        return document;
    }

    private String extractTextFromPDF(InputStream inputStream) throws IOException {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAllByOrderByUploadedAtDesc();
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    public List<DocumentChunk> getDocumentChunks(Long documentId) {
        return chunkRepository.findByDocumentIdOrderByChunkIndex(documentId);
    }
}

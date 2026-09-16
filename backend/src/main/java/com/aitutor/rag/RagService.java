package com.aitutor.rag;

import com.aitutor.ai.AiProvider;
import com.aitutor.ai.AiRequest;
import com.aitutor.ai.AiResponse;
import com.aitutor.ai.AiTask;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import com.aitutor.entity.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final KnowledgeDocumentRepository docs;
    private final KnowledgeChunkRepository chunks;
    private final List<AiProvider> providers;
    private final int maxPdfPages;
    private final int maxExtractedCharacters;

    public RagService(
            KnowledgeDocumentRepository docs,
            KnowledgeChunkRepository chunks,
            List<AiProvider> providers,
            @Value("${app.upload.max-pdf-pages:300}") int maxPdfPages,
            @Value("${app.upload.max-extracted-characters:5000000}") int maxExtractedCharacters
    ) {
        this.docs = docs;
        this.chunks = chunks;
        this.providers = providers;
        this.maxPdfPages = Math.max(1, Math.min(1000, maxPdfPages));
        this.maxExtractedCharacters = Math.max(100_000, Math.min(20_000_000, maxExtractedCharacters));
    }

    public KnowledgeDocument upload(MultipartFile file, User owner) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String name = Objects.requireNonNullElse(file.getOriginalFilename(), "document.pdf");
        if (!name.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }
        if (file.getSize() > 50L * 1024 * 1024) {
            throw new IllegalArgumentException("PDF exceeds the 50 MB upload limit");
        }

        try (PDDocument pdf = Loader.loadPDF(file.getBytes())) {
            if (pdf.getNumberOfPages() == 0) {
                throw new IllegalArgumentException("PDF contains no pages");
            }
            if (pdf.getNumberOfPages() > maxPdfPages) {
                throw new IllegalArgumentException("PDF exceeds the " + maxPdfPages + " page processing limit");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            StringBuilder extracted = new StringBuilder();
            final int chunkSize = 1400;
            final int overlap = 220;

            // Page-aware indexing fixes the old offset-based page estimator. Every
            // stored chunk now carries the exact PDF page it came from, so RAG
            // answers can cite real page numbers rather than approximate positions.
            for (int page = 1; page <= pdf.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(pdf)
                        .replace("\r", "\n")
                        .replaceAll("[ \t]+", " ")
                        .replaceAll("\n{3,}", "\n\n")
                        .trim();

                if (pageText.isBlank()) continue;
                if (extracted.length() > 0) extracted.append("\n\n");
                extracted.append("[Page ").append(page).append("]\n").append(pageText);
                if (extracted.length() > maxExtractedCharacters) throw new IllegalArgumentException("PDF contains too much extracted text for safe indexing");

            }

            String text = extracted.toString().trim();
            if (text.isBlank()) {
                throw new IllegalArgumentException("No readable text found in PDF");
            }

            // Chunks must reference the saved document. We first persist the
            // document, then rebuild the page-aware chunks with that relation.
            KnowledgeDocument document = docs.save(new KnowledgeDocument(
                    name,
                    file.getContentType() == null ? "application/pdf" : file.getContentType(),
                    file.getSize(),
                    text,
                    owner
            ));
            int index = 0;
            for (int page = 1; page <= pdf.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(pdf)
                        .replace("\r", "\n")
                        .replaceAll("[ \t]+", " ")
                        .replaceAll("\n{3,}", "\n\n")
                        .trim();
                if (pageText.isBlank()) continue;
                for (int start = 0; start < pageText.length(); start = Math.max(start + 1, start + chunkSize - overlap)) {
                    int end = Math.min(pageText.length(), start + chunkSize);
                    String part = pageText.substring(start, end).trim();
                    if (!part.isBlank()) chunks.save(new KnowledgeChunk(document, index++, page, part));
                    if (end == pageText.length()) break;
                }
            }
            return document;
        }
    }

    public List<Map<String, Object>> list(User user, boolean admin) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<KnowledgeDocument> source = admin ? docs.findAllByOrderByUploadedAtDesc() : docs.findAllByOwnerIdOrderByUploadedAtDesc(user.getId());

        source.forEach(document -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", document.id);
            item.put("fileName", document.fileName);
            item.put("sizeBytes", document.sizeBytes);
            item.put("uploadedAt", document.uploadedAt);
            item.put("contentType", document.contentType);
            result.add(item);
        });

        return result;
    }

    private KnowledgeDocument accessibleDocument(Long id, User user, boolean admin) {
        KnowledgeDocument document = docs.findById(id).orElseThrow(() -> new NoSuchElementException("Document not found"));
        if (!admin && (document.owner == null || !document.owner.getId().equals(user.getId()))) {
            throw new org.springframework.security.access.AccessDeniedException("You can only access your own knowledge documents");
        }
        return document;
    }

    public void delete(Long id, User user, boolean admin) {
        KnowledgeDocument document = docs.findById(id).orElseThrow(() -> new NoSuchElementException("Document not found"));
        if (!admin && (document.owner == null || !document.owner.getId().equals(user.getId()))) { throw new org.springframework.security.access.AccessDeniedException("You can only modify your own knowledge documents"); }
        if (!docs.existsById(id)) {
            throw new NoSuchElementException("Document not found");
        }

        chunks.findAllByDocumentIdOrderByChunkIndex(id)
                .forEach(chunk -> chunks.deleteById(chunk.id));

        docs.deleteById(id);
    }

    public AiResponse ask(
            Long id,
            User user,
            boolean admin,
            String question,
            String level,
            String language
    ) {
        KnowledgeDocument document = accessibleDocument(id,user,admin);

        List<KnowledgeChunk> all = chunks
                .findAllByDocumentIdOrderByChunkIndex(id);

        List<String> searchTerms = terms(question);

        List<KnowledgeChunk> top = all.stream()
                .sorted(
                        Comparator.comparingInt(
                                (KnowledgeChunk chunk) -> score(chunk.content, searchTerms)
                        ).reversed()
                )
                .limit(8)
                .collect(Collectors.toList());

        String context = top.stream()
                .map(chunk -> "[Page " + chunk.pageNumber + "] " + chunk.content)
                .collect(Collectors.joining("\n\n"));

        AiRequest request = new AiRequest(
                AiTask.TUTOR,
                document.fileName,
                "Answer this question using ONLY the supplied document context. " +
                        "If the answer is not present, say clearly that the document " +
                        "does not contain enough information. Cite page numbers like [Page 2]." +
                        "\n\nQUESTION:\n" + question +
                        "\n\nDOCUMENT CONTEXT:\n" + context,
                level,
                language
        );

        return providers.stream()
                .filter(AiProvider::available)
                .findFirst()
                .map(provider -> {
                    AiResponse generated = provider.generate(request);
                    String text = cleanAiText(generated == null ? "" : generated.answer());
                    if (text.isBlank() || isProviderFailure(text)) {
                        return new AiResponse(fallbackAsk(question, top), "RAG", "local-fallback");
                    }
                    return new AiResponse(text, generated.task(), generated.provider());
                })
                .orElseGet(() -> new AiResponse(fallbackAsk(question, top), "RAG", "local-fallback"));
    }

    public AiResponse generatePack(Long id, User user, boolean admin, String type, String level, String language) {
        KnowledgeDocument document = accessibleDocument(id,user,admin);
        List<KnowledgeChunk> all = chunks.findAllByDocumentIdOrderByChunkIndex(id);
        if (all.isEmpty()) {
            return new AiResponse("No readable content was indexed for this PDF. Please upload a text-based PDF.", "STUDY_PACK", "none");
        }

        // Keep the pack context bounded but materially richer than the old 7-chunk sample.
        // Even sampling gives the generated pack coverage across the whole document while
        // the larger output budget lets summaries/plans/flashcards finish instead of being
        // truncated mid-response.
        int wanted = Math.min(12, all.size());
        List<KnowledgeChunk> sample = new ArrayList<>();
        if (all.size() <= wanted) sample.addAll(all);
        else {
            for (int i = 0; i < wanted; i++) {
                int idx = (int) Math.round(i * (all.size() - 1.0) / (wanted - 1.0));
                sample.add(all.get(idx));
            }
        }
        String context = sample.stream()
                .map(c -> "[Page " + c.pageNumber + "] " + c.content)
                .collect(Collectors.joining("\n\n"));

        String safeType = type == null ? "summary" : type.toLowerCase(Locale.ROOT);
        AiTask task = switch (safeType) {
            case "flashcards" -> AiTask.FLASHCARDS;
            case "quiz" -> AiTask.QUIZ;
            case "studyplan" -> AiTask.STUDY_PLAN;
            default -> AiTask.SUMMARIZE;
        };
        String instruction = switch (safeType) {
            case "flashcards" -> "Create exactly 12 exam-focused flashcards. Format each as Q1: ... then A1: ... through Q12/A12. Ground every card only in the supplied PDF context.";
            case "quiz" -> "Create exactly 10 exam-focused MCQs. For each, give A-D options, the correct answer, and a one-line explanation. Ground every question only in the supplied PDF context.";
            case "studyplan" -> "Create a practical 7-day study plan using only the supplied PDF. For each day give topics, a task, and a short self-check.";
            default -> "Create a concise but detailed exam-ready summary with headings, definitions, formulas/examples when present, and high-yield takeaways. Do not invent facts outside the supplied PDF context.";
        };

        AiRequest request = new AiRequest(
                task,
                document.fileName,
                instruction + "\nIf the supplied context is incomplete, clearly say so instead of inventing missing material."
                        + "\n\nPDF CONTEXT:\n" + context,
                level,
                language
        );

        return providers.stream()
                .filter(AiProvider::available)
                .findFirst()
                .map(provider -> {
                    AiResponse generated = provider.generate(request);
                    String text = cleanAiText(generated == null ? "" : generated.answer());
                    boolean suspiciousSummary = "summary".equals(safeType) && text.length() < 500;
                    if (text.isBlank() || isProviderFailure(text) || suspiciousSummary) {
                        return new AiResponse(fallbackPack(safeType, sample), "STUDY_PACK", "local-fallback");
                    }
                    return new AiResponse(text, generated.task(), generated.provider());
                })
                .orElseGet(() -> new AiResponse(fallbackPack(safeType, sample), "STUDY_PACK", "local-fallback"));
    }

    private boolean isProviderFailure(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        return lower.contains("gemini api key missing")
                || lower.contains("gemini is temporarily")
                || lower.contains("gemini could not complete")
                || lower.contains("could not complete the request")
                || lower.contains("request timed out")
                || lower.contains("no grounded answer");
    }

    private String cleanAiText(String text) {
        if (text == null) return "";
        return text
                .replace("\uFFFD", "")
                .replaceAll("[\u200B-\u200D\uFEFF]", "")
                .replaceAll("[\u0000-\u0008\u000B\u000C\u000E-\u001F]", "")
                .replaceAll("(?m)^[ \t]*[•·▪▫◆◇★☆]{2,}[ \t]*$", "")
                .replaceAll("(?m)^[ \t]*[-_=]{5,}[ \t]*$", "")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }

    private String fallbackAsk(String question, List<KnowledgeChunk> top) {
        StringBuilder b = new StringBuilder("## Document-based answer\n\n");
        if (top.isEmpty()) {
            return b.append("I could not find enough readable content in this PDF to answer the question. Try asking about a specific topic, definition, example, or page.").toString();
        }
        b.append("I could not generate the full AI response, so here is the most relevant material found in your document.\n\n");
        int shown = 0;
        for (KnowledgeChunk c : top) {
            String clean = cleanAiText(c.content);
            if (clean.isBlank()) continue;
            b.append("### Page ").append(c.pageNumber).append("\n");
            b.append(firstSentence(clean)).append("\n\n");
            if (++shown >= 5) break;
        }
        return b.toString().trim();
    }

    private String fallbackPack(String type, List<KnowledgeChunk> sample) {
        StringBuilder b = new StringBuilder();
        if ("summary".equals(type)) {
            b.append("# Exam Summary\n\n");
            b.append("## Key points from the document\n\n");
            int point = 1;
            for (KnowledgeChunk c : sample) {
                String clean = cleanAiText(c.content);
                if (clean.isBlank()) continue;
                b.append(point++).append(". ").append(firstSentence(clean)).append(" [Page ").append(c.pageNumber).append("]\n");
            }
            b.append("\n## Quick revision\n\n");
            b.append("Review the points above, then return to the cited pages for the full definitions, examples, and details.");
        } else if ("flashcards".equals(type)) {
            b.append("# Flashcards\n\n");
            for (int i = 0; i < Math.min(12, sample.size()); i++) {
                KnowledgeChunk c = sample.get(i);
                b.append("Q").append(i + 1).append(": What is the key concept explained on page ").append(c.pageNumber).append("?\n")
                        .append("A").append(i + 1).append(": ").append(firstSentence(c.content)).append("\n\n");
            }
        } else if ("quiz".equals(type)) {
            b.append("# Practice Quiz\n\n");
            for (int i = 0; i < Math.min(10, sample.size()); i++) {
                KnowledgeChunk c = sample.get(i);
                b.append(i + 1).append(". What is the main idea in this section (Page ").append(c.pageNumber).append(")?\n")
                        .append("Answer: ").append(firstSentence(c.content)).append("\n\n");
            }
        } else {
            b.append("# 7-Day Study Plan\n\n");
            for (int i = 0; i < 7; i++) {
                KnowledgeChunk c = sample.get(i % sample.size());
                b.append("Day ").append(i + 1).append(": Review Page ").append(c.pageNumber)
                        .append("; write 5 recall points; solve 3 practice questions; finish with a 5-minute self-check.\n");
            }
        }
        return b.toString().trim();
    }

    private String firstSentence(String text) {
        String clean = text == null ? "" : text.replaceAll("\\s+", " ").trim();
        int dot = clean.indexOf('.');
        String sentence = dot > 20 ? clean.substring(0, dot + 1) : clean;
        return sentence.substring(0, Math.min(260, sentence.length()));
    }

    private List<String> terms(String question) {
        return Arrays.stream(
                        question
                                .toLowerCase(Locale.ROOT)
                                .replaceAll("[^a-z0-9 ]", " ")
                                .split("\\s+")
                )
                .filter(term -> term.length() > 2)
                .distinct()
                .collect(Collectors.toList());
    }

    private int score(String content, List<String> terms) {
        String normalized = content.toLowerCase(Locale.ROOT);
        if (terms.isEmpty() || normalized.isBlank()) return 0;
        int hits = 0;
        int distinct = 0;
        for (String term : terms) {
            int first = normalized.indexOf(term);
            if (first >= 0) {
                distinct++;
                int at = first;
                while ((at = normalized.indexOf(term, at)) >= 0) {
                    hits++;
                    at += Math.max(1, term.length());
                    if (hits > 120) break;
                }
            }
        }
        // V62 retrieval score: reward broad query coverage more than repeated mentions,
        // while retaining a small frequency signal. This deterministic reranker is a
        // stronger bridge toward vector/RAG-2.0 without changing stored document data.
        double coverage = distinct / (double) terms.size();
        double frequency = Math.min(1.0, hits / (double) Math.max(terms.size(), 1));
        return (int) Math.round(coverage * 100 + frequency * 20);
    }

    public List<Map<String, Object>> chunks(Long id, User user, boolean admin) {
        accessibleDocument(id,user,admin);
        List<Map<String, Object>> result = new ArrayList<>();

        chunks.findAllByDocumentIdOrderByChunkIndex(id).forEach(chunk -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", chunk.id);
            item.put("page", chunk.pageNumber);
            item.put("content", chunk.content);
            result.add(item);
        });

        return result;
    }
}

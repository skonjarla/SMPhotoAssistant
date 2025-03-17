package net.konjarla.smagent.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class VectorSearch {
    @NonNull
    private VectorStore photoCaptionVectorStore;

    @Value("${photo.vector.store.top_k}")
    @NonNull
    private final Integer top_k;

    @Value("${photo.vector.store.similarity_threshold}")
    @NonNull
    private final Double SIMILARITY_THRESHOLD;

    @Value("${photo.vector.store.similarity_score_threshold}")
    @NonNull
    private final Double SIMILARITY_SCORE_THRESHOLD;

    public List<Document> similaritySearch(String query) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression exp1 = b.ne("imageKey", "album_data").build();
        List<Document> results = photoCaptionVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .filterExpression(exp1)
                        .topK(top_k)
                        .similarityThreshold(SIMILARITY_THRESHOLD)
                        .build());
        assert results != null;
        return results.stream()
                .filter(
                        doc -> {
                            log.info(String.valueOf(doc.getScore()));
                            return doc.getScore() >= SIMILARITY_SCORE_THRESHOLD;
                        }
                )
                .toList();
    }

    public List<Document> similaritySearchInAlbum(String query, String albumKey) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression exp1 = b.and(b.ne("imageKey", "album_data"), b.eq("albumKey", albumKey)).build();
        List<Document> results = photoCaptionVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .filterExpression(exp1)
                        .topK(top_k)
                        .similarityThreshold(SIMILARITY_THRESHOLD)
                        .build());
        assert results != null;
        return results.stream()
                .filter(
                        doc -> {
                            log.info(String.valueOf(doc.getScore()));
                            return doc.getScore() >= SIMILARITY_SCORE_THRESHOLD;
                        }
                )
                .toList();
    }

    public List<Document> albumSearch(String query) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression exp1 = b.eq("imageKey", "album_data").build();
        List<Document> results = photoCaptionVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .filterExpression(exp1)
                        .topK(top_k)
                        .similarityThreshold(SIMILARITY_THRESHOLD)
                        .build());
        assert results != null;
        return results.stream()
                .filter(
                        doc -> {
                            log.info(String.valueOf(doc.getScore()));
                            return doc.getScore() >= 0.5;
                        }
                )
                .toList();
    }

    public List<Document> findAlbumNameForKey(String albumKey) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression exp1 = b.and(b.eq("imageKey", "album_data"), b.eq("albumKey", albumKey)).build();
        List<Document> results = photoCaptionVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("*")
                        .filterExpression(exp1)
                        //.topK(top_k)
                        .similarityThresholdAll()
                        .build());
        assert results != null;
        return results.stream()
                /*.filter(
                        doc -> {
                            log.info(String.valueOf(doc.getScore()));
                            return doc.getScore() >= 0.5;
                        }
                )*/
                .toList();
    }
}

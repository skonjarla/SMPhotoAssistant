package net.konjarla.smagent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.smagent.model.SMAgentRecord;
import net.konjarla.smagent.persistence.VectorDataService;
import net.konjarla.smagent.persistence.model.VectorData;
import net.konjarla.smugmug.client.OAuth1HttpClient;
import net.konjarla.smugmug.client.api.Albums;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class Indexer {
    private final ObjectMapper mapper = new ObjectMapper();
    @NonNull
    private OAuth1HttpClient oAuth1HttpClient;
    @NonNull
    private ChatClient photoCaptionChatClient;
    @NonNull
    private VectorStore photoCaptionVectorStore;
    @NonNull
    private VectorDataService vectorDataService;
    @NonNull
    private SmugMugService smugMugService;

    public void indexSmugMugAlbum(String albumKey) {
        Albums.getAllImagesOfAlbum(oAuth1HttpClient, albumKey).forEach(image -> {
            try {
                if (vectorDataService.getByImageKey(image.getImageKey()) == null) {
                    Resource imageURL = new UrlResource(image.getThumbnailUrl());
                    UserMessage userMessage = new UserMessage("Explain what do you see on this picture. Include only what exists in the picture. Do not include any commentary.",
                            List.of(new Media(MimeTypeUtils.IMAGE_JPEG, imageURL)));
                    ChatResponse response = photoCaptionChatClient
                            .prompt(new Prompt(List.of(userMessage)))
                            .call()
                            .chatResponse();
                    assert response != null;
                    String caption = response.getResult().getOutput().getText();
                    log.info(caption);
                    String docId = UUID.randomUUID().toString();
                    String dbId = UUID.randomUUID().toString();
                    Document document = Document.builder()
                            .id(docId)
                            .text(caption)
                            .metadata(Map.of(
                                    "imageKey", image.getImageKey(),
                                    "albumKey", albumKey,
                                    "dbId", dbId))
                            .build();
                    photoCaptionVectorStore.add(List.of(document));
                    VectorData.VectorDataBuilder builder = VectorData.builder();
                    vectorDataService.save(builder
                            .id(dbId)
                            .docid(docId)
                            .imageKey(image.getImageKey())
                            .albumKey(albumKey)
                            .build());
                } else {
                    log.info("Image {} already indexed", image.getImageKey());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    //@EventListener
    //public void loadAlbumDataToVectorStore(ApplicationStartedEvent event) {
    public void loadAlbumDataToVectorStore() {
        VectorData vectorData = vectorDataService.getByImageKey("album_data");
        if (vectorData == null) {
            try {
                String docId = UUID.randomUUID().toString();
                String dbId = UUID.randomUUID().toString();
                List<SMAgentRecord.AlbumInfo> albums = smugMugService.getAllAlbums();
                List<Document> documents = albums.stream()
                        .map(album -> {
                            return Document.builder()
                                    //.id(docId)
                                    .text(album.albumName())
                                    .metadata(Map.of(
                                            "imageKey", "album_data",
                                            "albumKey", album.albumKey(),
                                            //"docId", docId,
                                            "dbId", dbId))
                                    .build();
                        })
                        .toList();
                photoCaptionVectorStore.add(documents);
                VectorData.VectorDataBuilder builder = VectorData.builder();
                vectorDataService.save(builder.id(dbId).docid(docId).imageKey("album_data").albumKey("albumKey").build());
                log.info("vector store loaded with {} documents", documents.size());
            } catch (Exception e) {
                log.info(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
package net.konjarla.smagent.tools;

import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.smagent.model.SMAgentRecord;
import net.konjarla.smagent.persistence.PhotoCaptionVectorDataService;
import net.konjarla.smagent.persistence.model.PhotoCaptionVectorData;
import net.konjarla.smagent.service.VectorSearch;
import net.konjarla.smugmug.client.OAuth1HttpClient;
import net.konjarla.smugmug.client.api.Albums;
import net.konjarla.smugmug.client.api.Images;
import net.konjarla.smugmug.model.SMAlbum;
import net.konjarla.smugmug.model.SMImage;
import net.konjarla.smugmug.model.SMImageMetaData;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class PhotoTools {
    @NonNull
    private ChatClient photoCaptionChatClient;
    @NonNull
    private PhotoCaptionVectorDataService photoCaptionVectorDataService;
    @NonNull
    private VectorSearch vectorSearch;
    @NonNull
    private OAuth1HttpClient oAuth1HttpClient;

    private final static String SM_PHOTO_URL = "https://photos.smugmug.com/photos/";

    @Tool(description = "For a given description, find the most similar photos.")
    public List<SMAgentRecord.PhotoSearchResponse> findPicturesTool(String query) {
        log.info("findPicturesTool query: {}", query);
        List<Document> searchResults = vectorSearch.similaritySearch(query);
        //searchResults.forEach(System.out::println);
        return searchResults.stream()
                .map(doc -> {
                    SMImageMetaData metaData = Images.getMetaDataOfImageByKey(oAuth1HttpClient, doc.getMetadata().get("imageKey").toString());
                    return new SMAgentRecord.PhotoSearchResponse(doc.getId(),
                            vectorSearch.findAlbumNameForKey(doc.getMetadata().get("albumKey").toString()).getFirst().getText(), doc.getText(),
                            cleanAndConvertWebURI(Images.getImageByKey(oAuth1HttpClient, doc.getMetadata().get("imageKey").toString()).getWebUri()),
                            metaData.getAperture(), metaData.getExposure(), metaData.getIso(), metaData.getDateTimeCreated(), metaData.getFocalLength(),
                            metaData.getLatitude(), metaData.getLongitude(), metaData.getAltitude(), metaData.getCity(), metaData.getState(),
                            metaData.getCountry(), metaData.getMake(), metaData.getModel(), metaData.getLens());
                }).limit(3)
                .toList();
    }

    @Tool(description = "For given description and album, find the most similar photos.")
    public List<SMAgentRecord.PhotoSearchResponse> findPicturesInAlbumTool(String query, String album) {
        log.info("findPicturesInAlbumTool query: {}", query);
        List<PhotoCaptionVectorData> photoCaptionVectorData = photoCaptionVectorDataService.findByAlbumName(album);
        if (photoCaptionVectorData.isEmpty()) {
            return new ArrayList<>();
        }
        String albumKey = photoCaptionVectorData.getFirst().metadata.get("albumKey").asText();
        List<Document> searchResults = vectorSearch.similaritySearchInAlbum(query, albumKey);
        //searchResults.forEach(System.out::println);
        return searchResults.stream()
                .map(doc -> {
                    SMImageMetaData metaData = Images.getMetaDataOfImageByKey(oAuth1HttpClient, doc.getMetadata().get("imageKey").toString());
                    return new SMAgentRecord.PhotoSearchResponse(doc.getId(),
                            vectorSearch.findAlbumNameForKey(doc.getMetadata().get("albumKey").toString()).getFirst().getText(), doc.getText(),
                            cleanAndConvertWebURI(Images.getImageByKey(oAuth1HttpClient, doc.getMetadata().get("imageKey").toString()).getWebUri()),
                            metaData.getAperture(), metaData.getExposure(), metaData.getIso(), metaData.getDateTimeCreated(), metaData.getFocalLength(),
                            metaData.getLatitude(), metaData.getLongitude(), metaData.getAltitude(), metaData.getCity(), metaData.getState(),
                            metaData.getCountry(), metaData.getMake(), metaData.getModel(), metaData.getLens());
                }).limit(3)
                .toList();
    }

    @Tool(description = "Retrieve and analyze metadata (e.g., album, caption, location, aperture, exposure, timestamp etc.) for a specified photo ID.")
    public SMAgentRecord.PhotoSearchResponse getPhotoInformationByIdTool(String id) {
        log.info("getPhotoInformationByIdTool id: {}", id);
        PhotoCaptionVectorData photoCaptionVectorData = photoCaptionVectorDataService.getById(id);
        assert photoCaptionVectorData != null;
        SMImageMetaData metaData = Images.getMetaDataOfImageByKey(oAuth1HttpClient, photoCaptionVectorData.metadata.get("imageKey").asText());
        return new SMAgentRecord.PhotoSearchResponse(id,
                vectorSearch.findAlbumNameForKey(photoCaptionVectorData.metadata.get("albumKey").asText()).getFirst().getText(), photoCaptionVectorData.content,
                cleanAndConvertWebURI(Images.getImageByKey(oAuth1HttpClient, photoCaptionVectorData.metadata.get("imageKey").asText()).getWebUri()),
                metaData.getAperture(), metaData.getExposure(), metaData.getIso(), metaData.getDateTimeCreated(), metaData.getFocalLength(),
                metaData.getLatitude(), metaData.getLongitude(), metaData.getAltitude(), metaData.getCity(), metaData.getState(),
                metaData.getCountry(), metaData.getMake(), metaData.getModel(), metaData.getLens());
    }

    @Tool(description = "Finds albums that match a given description or query.")
    public List<Document> findAlbumsSymantically(String query) {
        log.info("findAlbumsSymantically query: {}", query);
        return vectorSearch.albumSearch(query);
    }

    @Tool(description = "Searches for albums by name and retrieves detailed information about each matching album.")
    public List<SMAlbum> findAlbumsByNameOrKey(String name) {
        log.info("findAlbumsByNameOrKey query: {}", name);
        List<PhotoCaptionVectorData> photoCaptionVectorData = photoCaptionVectorDataService.findByAlbumName(name);
        if (photoCaptionVectorData.isEmpty()) {
            return new ArrayList<>();
        } else {
            return photoCaptionVectorData.stream()
                    .map(data -> {
                        String id = data.metadata.get("albumKey").asText();
                        return Albums.getAlbumByKey(oAuth1HttpClient, id);
                    })
                    .collect(Collectors.toList());
        }
    }

    @Tool(description = "Retrieves detailed information about an album, using either the album's unique key or its name.")
    public SMAlbum getAlbumInformationTool(String albumNameOrId) {
        log.info("getAlbumInformationTool albumName: {}", albumNameOrId);
        List<PhotoCaptionVectorData> photoCaptionVectorData = photoCaptionVectorDataService.findByAlbumName(albumNameOrId);
        if (photoCaptionVectorData == null || photoCaptionVectorData.isEmpty()) {
            return Albums.getAlbumByKey(oAuth1HttpClient, albumNameOrId);
        } else {
            String id = photoCaptionVectorData.getFirst().metadata.get("albumKey").asText();
            return Albums.getAlbumByKey(oAuth1HttpClient, id);
        }
    }

    @Tool(description = "Analyzes the visual content of a photo using its ID and answers questions about what is depicted, excluding technical metadata. This tools is very useful when photo ID is available in the context.", returnDirect = true)
    public String analyzePhotoContent(String id, String query) {
        log.info("analyzePhotoContent id: {}, {}", id, query);
        PhotoCaptionVectorData photoCaptionVectorData = photoCaptionVectorDataService.getById(id);
        assert photoCaptionVectorData != null;
        String imageKey = photoCaptionVectorData.metadata.get("imageKey").asText();
        SMImage image = Images.getImageByKey(oAuth1HttpClient, imageKey);
        Resource imageURL = null;
        try {
            imageURL = new UrlResource(image.getThumbnailUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        UserMessage userMessage = new UserMessage(query,
                List.of(new Media(MimeTypeUtils.IMAGE_JPEG, imageURL)));
        ChatResponse response = photoCaptionChatClient
                .prompt(new Prompt(List.of(userMessage)))
                .call()
                .chatResponse();
        assert response != null;
        return response.getResult().getOutput().getText();
    }

    @Tool(description = "Analyzes the visual content of a photo using its WebUri and answers questions about what is depicted, excluding technical metadata. This tools is very useful when photo ID is NOT available and WebUri is available in the context.", returnDirect = true)
    public String analyzePhotoContentByWebUri(String webUri, String query) {
        log.info("analyzePhotoContentByWebUri url: {}", webUri);
        Resource imageURL = null;
        try {
            imageURL = new UrlResource(webUri);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        UserMessage userMessage = new UserMessage(query,
                List.of(new Media(MimeTypeUtils.IMAGE_JPEG, imageURL)));
        ChatResponse response = photoCaptionChatClient
                .prompt(new Prompt(List.of(userMessage)))
                .call()
                .chatResponse();
        assert response != null;
        return response.getResult().getOutput().getText();
    }

    public static String cleanAndConvertWebURI(String uri) {
        if (uri.endsWith(".")) {
            uri = uri.substring(0, uri.length() - 1);
        } else if (uri.endsWith("\"")) {
            uri = uri.substring(0, uri.length() - 1);
        } else if (uri.endsWith(")")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        Path path = Paths.get(uri);
        String photoRef = path.getName(path.getNameCount() - 1).toString();
        return SM_PHOTO_URL + photoRef + "/0/L/" + photoRef + "-L.jpg";
    }

    public String getThumbnailByImageKey(String imageKey) throws IOException {
        SMImage image = Images.getImageByKey(oAuth1HttpClient, imageKey);
        URL url = URI.create(image.getThumbnailUrl()).toURL(); //image.getThumbnailUrl();
        try (InputStream is = url.openStream()) {
            BufferedInputStream bis = new BufferedInputStream(url.openConnection().getInputStream());
            return Base64.getEncoder().encodeToString(bis.readAllBytes());
        }
    }

    public ArrayList<String> extractURL(String content) {
        ArrayList<String> urls = new ArrayList<>();
        UrlDetector parser = new UrlDetector(content, UrlDetectorOptions.Default);
        parser.detect().stream().filter(url -> url.getFullUrl().toLowerCase().contains("https"))
                .forEach(url -> {
                    String fullUrl = url.getFullUrl();
                    if (fullUrl.endsWith(".")) {
                        fullUrl = fullUrl.substring(0, fullUrl.length() - 1);
                    } else if (fullUrl.endsWith("\"")) {
                        fullUrl = fullUrl.substring(0, fullUrl.length() - 1);
                    } else if (fullUrl.endsWith(")")) {
                        fullUrl = fullUrl.substring(0, fullUrl.length() - 1);
                    } else if (urls.contains(fullUrl)) {
                        return;
                    }
                    urls.add(fullUrl);
                });
        return urls;
    }
}


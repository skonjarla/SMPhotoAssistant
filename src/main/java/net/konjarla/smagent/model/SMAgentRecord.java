package net.konjarla.smagent.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author: Srikanth Konjarla
 */

public interface SMAgentRecord {
    public record AlbumInfo(String albumKey, String albumName) {
    }

    public record PhotoSearchQuery(String query) {
    }

    public record PhotoSearchResponse(@JsonProperty("id") String id, @JsonProperty("albumName") String album,
                                      @JsonProperty("caption") String caption,
                                      @JsonProperty("webUri") String webUri, @JsonProperty("aperture") String aperture,
                                      @JsonProperty("exposure") String exposure, @JsonProperty("iso") Integer iso,
                                      @JsonProperty("dateTimeCreated") String dateTimeCreated,
                                      @JsonProperty("focalLength") String focalLength,
                                      @JsonProperty("latitude") Double latitude,
                                      @JsonProperty("longitude") Double longitude,
                                      @JsonProperty("altitude") String altitude, @JsonProperty("city") String City,
                                      @JsonProperty("state") String state, @JsonProperty("country") String country,
                                      @JsonProperty("make") String make, @JsonProperty("model") String model,
                                      @JsonProperty("lens") String lens) {
    }

    public static record Task(String type, String description) {
    }

    public record OrchestratorResponse(String analysis, List<Task> tasks) {
    }

    public record ChatResponse(String reply) {

    }

    public record ChatRequest(String text) {

    }

    public record PhotoChatResponse(String type, String text, List<String> images){}

    public record PhotoChat(String type, String content, String sender) {
    }

}

package net.konjarla.smagent.config;

import io.micrometer.observation.ObservationRegistry;
import lombok.AllArgsConstructor;
import net.konjarla.smagent.service.LoggingAdvisor;
import net.konjarla.smagent.service.ResponseAdvisor;
import net.konjarla.smagent.service.RestClientInterceptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.DEFAULT_CHAT_MEMORY_CONVERSATION_ID;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
@AllArgsConstructor
public class PhotoAgentConfiguration {
    @Value("${photo.caption.vector.store.dim}")
    private Integer dim;

    @Value("${photo.caption.vector.store}")
    private String photo_caption_vector_store;

    @Value("${ollama.host}")
    private String ollamaHost;

    @Value("${ollama.chat.model}")
    private String ollamaChatModel;

    @Value("${ollama.photo.chat.model}")
    private String ollamaPhotoChatModel;

    @Value("${ollama.chat.options.temperature}")
    private Double ollamaChatTemperature;

    @Value("${ollama.photo.chat.options.temperature}")
    private Double ollamaPhotoChatTemperature;

    @Value("${photo.chat.system.prompt.template.file}")
    private Resource systemPrompt;

    @Value("${spring.ai.ollama.chat.options.num-ctx}")
    private Integer numCtx;

    @Bean
    public OllamaApi ollamaApi() {
        RestClient.Builder builder = RestClient.builder();
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings
                .defaults()
                .withConnectTimeout(Duration.ofSeconds(60))
                .withReadTimeout(Duration.ofSeconds(180));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect().build(settings);
        builder.requestFactory(requestFactory)
                .requestInterceptor(new RestClientInterceptor());
        WebClient.Builder webClientBuilder = WebClient.builder();

        return new OllamaApi(ollamaHost, builder, webClientBuilder);
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        ModelManagementOptions modelManagementOptions = ModelManagementOptions.builder()
                .pullModelStrategy(PullModelStrategy.WHEN_MISSING)
                .build();

        return new OllamaEmbeddingModel(ollamaApi(),
                OllamaOptions.builder()
                        .model(OllamaModel.MXBAI_EMBED_LARGE.id())
                        .build(), observationRegistry, modelManagementOptions);
    }

    @Bean
    public ChatModel chatModel() {
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        ModelManagementOptions modelManagementOptions = ModelManagementOptions.builder()
                .pullModelStrategy(PullModelStrategy.WHEN_MISSING)
                .build();
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi())
                .defaultOptions(OllamaOptions.builder()
                        .model(ollamaChatModel)
                        .numCtx(numCtx)
                        .temperature(ollamaChatTemperature)
                        .build())
                .modelManagementOptions(modelManagementOptions)
                .observationRegistry(observationRegistry)
                .build();
    }

    @Bean
    public OllamaChatModel photoChatModel() {
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        ModelManagementOptions modelManagementOptions = ModelManagementOptions.builder()
                .pullModelStrategy(PullModelStrategy.WHEN_MISSING)
                .build();
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi())
                .defaultOptions(OllamaOptions.builder()
                        .model(ollamaPhotoChatModel)
                        .numCtx(numCtx)
                        .temperature(ollamaPhotoChatTemperature)
                        .build())
                .modelManagementOptions(modelManagementOptions)
                .observationRegistry(observationRegistry)
                .build();
    }

    @Bean
    public VectorStore photoCaptionVectorStore(JdbcTemplate jdbcTemplate) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel())
                .dimensions(dim)
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName(photo_caption_vector_store)
                .maxDocumentBatchSize(10000)
                .build();
    }

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public ChatClient photoChatClient() {
        return ChatClient.builder(chatModel())
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        // Chat memory helps us keep context when using the chatbot for up to 20 previous messages.
                        new MessageChatMemoryAdvisor(chatMemory(), DEFAULT_CHAT_MEMORY_CONVERSATION_ID, 10), // CHAT MEMORY
                        new LoggingAdvisor(),
                        new ResponseAdvisor()
                )
                .build();
    }

    @Bean
    public ChatClient photoCaptionChatClient() {
        return ChatClient.builder(photoChatModel())
                .defaultAdvisors(
                        new LoggingAdvisor()
                )
                .build();
    }
}

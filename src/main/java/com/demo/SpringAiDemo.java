package com.demo;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.chatbot.ChatBot;
import org.springframework.ai.chat.chatbot.DefaultChatBot;
import org.springframework.ai.chat.chatbot.DefaultStreamingChatBot;
import org.springframework.ai.chat.chatbot.StreamingChatBot;
import org.springframework.ai.chat.history.*;
import org.springframework.ai.chat.prompt.transformer.QuestionContextAugmentor;
import org.springframework.ai.chat.prompt.transformer.VectorStoreRetriever;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@SpringBootApplication
@Theme(value = "spring-ai-demo")
public class SpringAiDemo implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiDemo.class, args);
    }

    @Bean
    public PgVectorStore pgVectorStore(
            JdbcTemplate jdbcTemplate,
            @Qualifier("openAiEmbeddingClient") EmbeddingClient embeddingClient) {
        return new PgVectorStore(jdbcTemplate, embeddingClient, 1536);
    }

    @Bean
    public OpenAiApi openAiApi() {
        return new OpenAiApi(System.getenv("OPEN_AI_API_KEY"));
    }

    @Bean
    public StreamingChatClient chatClient(OpenAiApi openAiApi) {
        return new OpenAiChatClient(openAiApi);
    }

    @Bean
    public ChatClient defaultChatClient(OpenAiApi openAiApi) {
        return new OpenAiChatClient(openAiApi);
    }

    @Bean
    public StreamingChatBot chatBot(StreamingChatClient chatClient, VectorStore vectorStore) {
        return DefaultStreamingChatBot.builder(chatClient)
                .withRetrievers(List.of(new VectorStoreRetriever(vectorStore, SearchRequest.defaults())))
                .withDocumentPostProcessors(
                        List.of(new LastMaxTokenSizeContentTransformer(new JTokkitTokenCountEstimator(), 1000)))
                .withAugmentors(List.of(new QuestionContextAugmentor()))
                .build();

    }

    @Bean
    public ChatBot chatBot2(ChatClient defaultChatClient, VectorStore vectorStore) {
        return DefaultChatBot.builder(defaultChatClient)
                .withRetrievers(List.of(new VectorStoreRetriever(vectorStore, SearchRequest.defaults())))
                .withContentPostProcessors(
                        List.of(new LastMaxTokenSizeContentTransformer(new JTokkitTokenCountEstimator(), 1000)))
                .withAugmentors(List.of(new QuestionContextAugmentor()))
                .build();

    }

    @Bean
    public RelevancyEvaluator relevancyEvaluator(OpenAiChatClient chatClient) {
        return new RelevancyEvaluator(chatClient);
    }
}
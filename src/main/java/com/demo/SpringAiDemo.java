package com.demo;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
@Theme(value = "spring-ai-demo")
public class SpringAiDemo implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiDemo.class, args);
    }

    @Bean
    public PgVectorStore pgVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return new PgVectorStore(jdbcTemplate, embeddingModel, 1536);
    }

    @Bean
    ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }


    @Bean
    public RelevancyEvaluator relevancyEvaluator(ChatClient.Builder chatClient) {
        return new RelevancyEvaluator(chatClient);
    }
}
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
import org.springframework.ai.mistralai.MistralAiChatClient;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
@Theme(value = "spring-ai-demo")
public class SpringAiDemo implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(SpringAiDemo.class, args);
    }

    @Bean
    public MistralAiApi chatCompletionApi() {
        return new MistralAiApi(System.getenv("MISTRAL_AI_API_KEY"));
    }

    @Bean
    public StreamingChatClient mistralChatClient(MistralAiApi mistralAiApi) {
        return new MistralAiChatClient(mistralAiApi);
    }

    @Bean
    public StreamingChatBot chatBot(StreamingChatClient mistralChatClient) {
        var chatHistory = new InMemoryChatMemory();
        return DefaultStreamingChatBot.builder(mistralChatClient)
                .withRetrievers(List.of(new ChatMemoryRetriever(chatHistory)))
                .withDocumentPostProcessors(
                        List.of(new LastMaxTokenSizeContentTransformer(new JTokkitTokenCountEstimator(), 1000)))
                .withAugmentors(List.of(new SystemPromptChatMemoryAugmentor()))
                .withChatBotListeners(List.of(new ChatMemoryChatBotListener(chatHistory)))
                .build();

    }
}
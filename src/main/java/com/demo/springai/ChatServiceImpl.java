package com.demo.springai;

import org.springframework.ai.chat.chatbot.StreamingChatBot;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.prompt.transformer.PromptContext;
import org.springframework.ai.mistralai.MistralAiChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService {
    private final MistralAiChatClient chatClient;

    private final Resource systemPrompt;
    private final StreamingChatBot chatBot;

    private static final String CONVERSATION_ID = UUID.randomUUID().toString();

    public ChatServiceImpl(
            MistralAiChatClient chatClient,
            @Value("classpath:/prompts/system-qa.st") Resource systemPrompt,
            StreamingChatBot chatBot) {
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt;
        this.chatBot = chatBot;
    }

    @Override
    public String chat(String message) {
        var prompt = getPrompt(message);
        return chatClient.call(prompt).getResult().getOutput().getContent();
    }

    @Override
    public Flux<String> chatWithStream(String message) {
        var prompt = getPrompt(message);
        return chatBot.stream(new PromptContext(prompt)).getChatResponse()
                .map(response -> response.getResult().getOutput().getContent());
    }

    private Prompt getPrompt(String message) {

        var systemMessage = new SystemPromptTemplate(systemPrompt)
                .createMessage(Map.of("question", message));

        var userMessage = new UserMessage(message);

        return new Prompt(List.of(systemMessage, userMessage));
    }

}

package fr.sciam.springai;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiImageClient;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {
    private final OpenAiChatClient chatClient;

    private final OpenAiImageClient imageClient;

    private final Resource systemPrompt;

    private final VectorStore vectorStore;

    public ChatServiceImpl(
            OpenAiChatClient chatClient,
            OpenAiImageClient imageClient,
            @Value("classpath:/prompts/system-qa-rag2.st") Resource systemPrompt,
            VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.imageClient = imageClient;
        this.systemPrompt = systemPrompt;
        this.vectorStore = vectorStore;
    }

    @Override
    public String chat(String message) {
        var prompt = getPrompt(message);
        return chatClient.call(prompt).getResult().getOutput().getContent();
    }

    @Override
    public Flux<String> chatWithStream(String message) {
        var prompt = getPrompt(message);
        return chatClient.stream(prompt)
                .map(response -> response.getResult().getOutput().getContent());
    }

    @Override
    public String generateImage(String message) {
        var prompt = new ImagePrompt(message, OpenAiImageOptions.builder()
                .withHeight(1024)
                .withWidth(1024)
                .withN(1)
                .build());
        return imageClient.call(prompt).getResult().getOutput().getUrl();
    }

    private Prompt getPrompt(String message) {

        // Retrieve similar chunks from the vector database
        var similarity = vectorStore.similaritySearch(
                SearchRequest.query("")
                        .withQuery(message)
                        .withSimilarityThreshold(0.1)
                        .withTopK(3));

        var systemMessage = new SystemPromptTemplate(systemPrompt)
                .createMessage(Map.of("question", message, "documents", similarity));

        var userMessage = new UserMessage(message);

        return new Prompt(List.of(systemMessage, userMessage));
    }

}

package fr.sciam;

import org.springframework.ai.azure.openai.AzureOpenAiChatClient;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mistralai.MistralAiChatClient;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class SpringAiDemo {
    public static void main(String[] args) {
        SpringApplication.run(SpringAiDemo.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(
            MistralAiChatClient mistralAiChatClient,
            OpenAiChatClient openAiChatClient,
            AzureOpenAiChatClient azureOpenAiChatClient,
            /*ChatClient chatClient,*/
            VectorStore vectorStore) {
        return args -> {

            var result = mistralAiChatClient.call("Hello");

            var result2 = mistralAiChatClient.stream("Hello");

            /*ChatResponse response = chatClient.call(
                    new Prompt(
                            "Generate the names of 5 famous pirates.",
                            MistralAiChatOptions.builder()
                                    .withModel(MistralAiApi.ChatModel.LARGE.getValue())
                                    .withTemperature(0.5f)
                                    .build()
            ));*/

            vectorStore.accept(List.of());

            var result3 = vectorStore.similaritySearch("Hello");

            var result4 = vectorStore.similaritySearch(SearchRequest
                    .query("")
                    .withQuery("Hello")
                    .withTopK(5)
                    .withSimilarityThreshold(1)
            );
        };
    }
}
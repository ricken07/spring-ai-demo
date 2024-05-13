package com.demo;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.chatbot.DefaultStreamingChatBot;
import org.springframework.ai.chat.chatbot.StreamingChatBot;
import org.springframework.ai.chat.history.*;
import org.springframework.ai.mistralai.MistralAiChatClient;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@SpringBootApplication
@Theme(value = "spring-ai-demo")
public class SpringAiDemo implements AppShellConfigurator {

    @Value("classpath:/data/rapport-observation.pdf")
    Resource data;

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
    ApplicationRunner runner(VectorStore vectorStore,
                             JdbcTemplate jdbcTemplate) {
        return args -> {

            // Extract data from source
            /*var documents = extractData();

            jdbcTemplate.update("delete from vector_store");

            var tokenTextSplitter = new TokenTextSplitter();*/

            // Parsing document, splitting, creating embeddings and storing in vector store...
            /*vectorStore.accept(tokenTextSplitter.apply(documents));*/

        };
    }

    private List<Document> extractData() {

        // Get data and Extract text from html page
        var dataUrl = "https://www.parisjug.org/events/2024/05-14-ai-llm/";
        var tikaReader = new TikaDocumentReader(dataUrl);

        return tikaReader.get();
    }

    private List<Document> extractData2() {

        // Get data and Extract text from pdf
        var docs = new PagePdfDocumentReader(data,
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(
                                ExtractedTextFormatter.builder()
                                        .withNumberOfTopTextLinesToDelete(0)
                                        .build())
                        .withPagesPerDocument(1)
                        .build());

        return docs.get();
    }

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
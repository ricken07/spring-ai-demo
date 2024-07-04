package com.demo.springai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Service
public class ChatServiceImpl implements ChatService {

    private final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatClient chatClient;

    private static final String CONVERSATION_ID = UUID.randomUUID().toString();

    private final Resource systemPrompt;

    private final VectorStore vectorStore;

    private final Resource rapportCommission;
    private final Resource rapportObservation;
    private final Resource rapportJp;

    private final JdbcTemplate jdbcTemplate;

    public ChatServiceImpl(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            VectorStore vectorStore,
            JdbcTemplate jdbcTemplate,
            @Value("classpath:/prompts/system-qa-rag.st") Resource systemPrompt,
            @Value("classpath:/data/rapport-commission-ia.pdf") Resource rapportCommission,
            @Value("classpath:/data/rapport-observation.pdf") Resource rapportObservation,
            @Value("classpath:/data/jp-morgan-annual-report.pdf") Resource rapportJp) {
        this.systemPrompt = systemPrompt;
        this.vectorStore = vectorStore;
        this.rapportCommission = rapportCommission;
        this.jdbcTemplate = jdbcTemplate;
        this.rapportObservation = rapportObservation;
        this.rapportJp = rapportJp;
        this.chatClient = chatClientBuilder
                .defaultOptions(OpenAiChatOptions.builder()
                        .withTemperature(0.2f)
                        .build())
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    @Override
    public Flux<String> chatWithStream(String message) {
        var prompt = getPrompt(message);
        return chatClient.prompt()
                /*.system(sys -> sys.text(systemPrompt)
                        .param("question", message)
                )*/
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()
                        .withTopK(5).withSimilarityThreshold(0.5)))
                .user(message)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, "1234"))
                .user(message)
                .stream()
                .content();
    }

    @Override
    public String generateImage(String message) {
        var prompt = new ImagePrompt(message, OpenAiImageOptions.builder()
                .withHeight(1024)
                .withWidth(1024)
                .withN(1)
                .build());
        return "";
    }

    @Override
    public void ingestData() {

        //var dataLink = extractData();

        //var docPdf = extractPdfData();

        //var docPdf2 = extractPdf2Data();

        var docPdf3 = extractPdf3Data();
        log.info("\n\n {}", docPdf3.stream().map(Document::getContent).collect(Collectors.joining(System.lineSeparator())));

        jdbcTemplate.update("delete from vector_store");

        var tokenTextSplitter = new TokenTextSplitter();

        // Parsing document, splitting, creating embeddings and storing in vector store...
        //vectorStore.accept(tokenTextSplitter.apply(dataLink));

        //vectorStore.accept(tokenTextSplitter.apply(docPdf));

        //vectorStore.accept(tokenTextSplitter.apply(docPdf2));

        vectorStore.accept(tokenTextSplitter.apply(docPdf3));
    }

    private Prompt getPrompt(String message) {

        // Retrieve similar chunks from the vector database
        // Recherche des informations pertinentes qui correspondent ou sont similaires à l'instruction initiale
        var similarity = vectorStore.similaritySearch(
                SearchRequest.query(message)
                        .withSimilarityThreshold(0.5) // Seuil de similarité pour filtrer la réponse de la recherche.
                        .withTopK(5)); // les "k" premiers résultats similaires à renvoyer.

        var systemMessage = new SystemPromptTemplate(systemPrompt)
                .createMessage(Map.of("question", message, "context", similarity));

        var userMessage = new UserMessage(message);

        return new Prompt(List.of(systemMessage, userMessage));
    }

    private List<Document> extractData() {

        // Get data and Extract text from page html
        var dataUrl = "https://www.parisjug.org/events/2024/05-14-ai-llm/";
        var tikaReader = new TikaDocumentReader(dataUrl);

        return tikaReader.get();
    }

    private List<Document> extractPdfData() {
        // Get data and Extract text from pdf
        var docs = new PagePdfDocumentReader(rapportCommission,
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

    private List<Document> extractPdf2Data() {
        // Get data and Extract text from pdf
        var docs = new PagePdfDocumentReader(rapportObservation,
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

    private List<Document> extractPdf3Data() {
        // Get data and Extract text from pdf
        var docs = new PagePdfDocumentReader(rapportJp,
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

}

package com.demo.springai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.chatbot.ChatBot;
import org.springframework.ai.chat.chatbot.ChatBotResponse;
import org.springframework.ai.chat.chatbot.StreamingChatBot;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.prompt.transformer.PromptContext;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.mistralai.MistralAiChatClient;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageClient;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.ai.openai.api.OpenAiApi.ChatModel.GPT_4_TURBO_PREVIEW;

@Service
public class ChatServiceImpl implements ChatService {

    private final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final MistralAiChatClient mistralAiChatClient;

    private final OpenAiChatClient openAiChatClient;

    private final OpenAiImageClient imageClient;
    private final StreamingChatBot chatBot;
    private final ChatBot chatBot2;

    private static final String CONVERSATION_ID = UUID.randomUUID().toString();

    private final Resource systemPrompt;

    private final VectorStore vectorStore;

    private final Resource rapportCommission;
    private final Resource rapportObservation;
    private final Resource rapportJp;

    private final JdbcTemplate jdbcTemplate;

    public ChatServiceImpl(
            MistralAiChatClient mistralAiChatClient, OpenAiChatClient openAiChatClient,
            OpenAiImageClient imageClient,
            StreamingChatBot chatBot, ChatBot chatBot2,
            VectorStore vectorStore,
            JdbcTemplate jdbcTemplate,
            @Value("classpath:/prompts/system-qa-rag.st") Resource systemPrompt,
            @Value("classpath:/data/rapport-commission-ia.pdf") Resource rapportCommission,
            @Value("classpath:/data/rapport-observation.pdf") Resource rapportObservation,
            @Value("classpath:/data/jp-morgan-annual-report.pdf") Resource rapportJp) {
        this.mistralAiChatClient = mistralAiChatClient;
        this.openAiChatClient = openAiChatClient;
        this.imageClient = imageClient;
        this.chatBot = chatBot;
        this.chatBot2 = chatBot2;
        this.systemPrompt = systemPrompt;
        this.vectorStore = vectorStore;
        this.rapportCommission = rapportCommission;
        this.jdbcTemplate = jdbcTemplate;
        this.rapportObservation = rapportObservation;
        this.rapportJp = rapportJp;
    }

    @Override
    public String chat(String message) {
        var prompt = getPrompt(message);
        var response = chatBot2.call(new PromptContext(prompt));
        var result = evaluate(response);
        return response.getChatResponse().getResult().getOutput().getContent();
    }

    @Override
    public Flux<String> chatWithStream(String message) {
        var prompt = getPrompt(message);
        return chatBot.stream(new PromptContext(prompt)).getChatResponse()
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

    private EvaluationResponse evaluate(ChatBotResponse response) {
        var openAiChatOptions = OpenAiChatOptions.builder()
                .withModel(GPT_4_TURBO_PREVIEW.getValue())
                .build();
        var relevancyEvaluator = new RelevancyEvaluator(this.openAiChatClient, openAiChatOptions);
        var evaluationRequest = new EvaluationRequest(response);
        var evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);
        //assertTrue(evaluationResponse.isPass(), "Response is not relevant to the question");
        return evaluationResponse;
    }

}

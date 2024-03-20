package fr.sciam;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
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
public class SpringAiDemo {

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
            var documents = extractData();

            jdbcTemplate.update("delete from vector_store");

            var tokenTextSplitter = new TokenTextSplitter();

            // Parsing document, splitting, creating embeddings and storing in vector store...
            vectorStore.accept(tokenTextSplitter.apply(documents));

        };
    }

    private List<Document> extractData() {

        // Get data and Extract text from page html
        var dataUrl = "https://rickenbazolo.com/";
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

}
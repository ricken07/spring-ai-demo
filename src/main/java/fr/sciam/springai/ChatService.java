package fr.sciam.springai;

import reactor.core.publisher.Flux;

public interface ChatService {
    String chat(String message);
    Flux<String> chatWithStream(String message);
    String generateImage(String message);
    void ingestData();
}

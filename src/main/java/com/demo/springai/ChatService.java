package com.demo.springai;

import reactor.core.publisher.Flux;

public interface ChatService {
    Flux<String> chatWithStream(String message);
    String generateImage(String message);
    void ingestData();
}

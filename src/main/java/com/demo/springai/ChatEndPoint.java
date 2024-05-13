package com.demo.springai;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

/**
 * @author Ricken Bazolo
 */
@BrowserCallable
@AnonymousAllowed
public class ChatEndPoint {

    private final ChatService chatService;

    @Autowired
    public ChatEndPoint(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostConstruct
    public void init () {
        chatService.ingestData();
    }

    public Flux<String> chat(String message) {
        return chatService.chatWithStream(message);
    }

    public String chat2(String message) {
        return chatService.chat(message);
    }

    public String generateImage(String message)  {
        return chatService.generateImage(message);
    }
}

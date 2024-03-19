package fr.sciam.springai;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("ai/")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("chat")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String chatCompletion(@RequestParam("message") String message) {
        return chatService.chat(message);
    }

    @GetMapping(value = "chat/stream")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Flux<String> chatCompletionWithStream(@RequestParam("message") String message) {
        return chatService.chatWithStream(message);
    }

    @GetMapping(value = "images/generations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String generateImage(@RequestParam("message") String message) {
        return chatService.generateImage(message);
    }
}

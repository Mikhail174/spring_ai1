package org.example.controller;

import org.example.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class StreamingChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping(value = "/chat-stream/{chatId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter talkToModel(@PathVariable Long chatId, @RequestParam String userPrompt){
        SseEmitter emitter = chatService.proceedInteractionWithStreaming(chatId, userPrompt);
        return emitter;
    }

}

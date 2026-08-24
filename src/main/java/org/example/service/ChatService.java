package org.example.service;

import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.example.model.Chat;
import org.example.model.ChatEntry;
import org.example.model.Role;
import org.example.repository.ChatRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

import static org.example.model.Role.ASSISTANT;
import static org.example.model.Role.USER;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatService myProxy;

    public List<Chat> getAllChats() {
        return chatRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Chat getChat(Long chatId) {
        return chatRepository.findById(chatId).orElseThrow();
    }

    public Chat createNewChat(String title) {
        Chat chat = Chat.builder()
                .title(title)
                .build();
        return chatRepository.save(chat);
    }

    public void deleteChat(Long chatId) {
        chatRepository.deleteById(chatId);
    }

    @Transactional
    public void proceedInteraction(Long chatId, String prompt) {
        myProxy.addChatEntry(chatId, prompt, USER);
        String answer = chatClient.prompt().user(prompt).call().content();
        myProxy.addChatEntry(chatId, answer, ASSISTANT);
    }

    @Transactional
    public void addChatEntry(Long chatId, String prompt, Role role) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        chat.addEntry(ChatEntry.builder().content(prompt).role(role).build());
    }

    public SseEmitter proceedInteractionWithStreaming(Long chatId, String prompt) {
        myProxy.addChatEntry(chatId, prompt, USER);

        StringBuilder answer = new StringBuilder();


        SseEmitter emitter = new SseEmitter(0L);
        chatClient.prompt().user(prompt).stream()
                .chatResponse()
                .subscribe(chatResponse -> processToken(chatResponse, emitter, answer),
                        emitter::completeWithError,
                        ()->myProxy.addChatEntry(chatId, answer.toString(), ASSISTANT));
        return emitter;
    }

    @SneakyThrows
    private static void processToken(ChatResponse chatResponse, SseEmitter emitter, StringBuilder answer){
        var token = chatResponse.getResult().getOutput();
        emitter.send(token);
        answer.append(token.getText());
    }
}

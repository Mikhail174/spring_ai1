package org.example.advisors.rag;

import lombok.Builder;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.example.advisors.expension.ExpansionQueryAdvisor.ENRICHED_QUESTION;
import static org.example.advisors.expension.ExpansionQueryAdvisor.template;

@Builder
public class RagAdvisor implements BaseAdvisor {

    @Builder.Default
    private static final PromptTemplate template = 

    private VectorStore vectorStore;

    private int order;


    public static RagAdvisorBuilder build (VectorStore vectorStore) {
        return new RagAdvisorBuilder().vectorStore(vectorStore);
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String userQuestion = chatClientRequest.prompt().getUserMessage().getText();
        String queryToRag = chatClientRequest.context().getOrDefault(ENRICHED_QUESTION, userQuestion).toString();
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder().query(queryToRag).topK(4).similarityThreshold(0.65).build());

        if(documents == null || documents.isEmpty()) {
            return chatClientRequest;
        }

        String llmContext = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));

        String finalUserPrompt = template.render(Map.of("context", llmContext, "question", userQuestion));
        return chatClientRequest.mutate().prompt(chatClientRequest.prompt().augmentUserMessage(finalUserPrompt)).build();

    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return null;
    }

    @Override
    public int getOrder() {
        return order;
    }
}

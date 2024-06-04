package kopo.poly.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RagController {

    private final ChatClient chatClient;
    // RagController가 메모리 올라갈 때, RagConfig 작업이 완료된 vectorStore를 주입받음
    private final VectorStore vectorStore;
    // 사전 작성된 지시사항 로드
    @Value("classpath:/prompts/rag-prompt-template.st")
    private Resource ragPromptTemplate;

    @GetMapping(value = "/rag")
    public String rag(@RequestParam(value = "message", defaultValue = "spring boot가 뭐지 ?") String message) {

        // VectorStore로부터 질문과 가장 연광성이 가장 높은 응답 2가지를 조회
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(message).withTopK(2));

        // 조회된 2가지 결과를 문자열로 변환
        List<String> contentList = similarDocuments.stream().map(Document::getContent).toList();

        // 사전 작성된 지시사항 로드
        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);

        // 익렵값과 응답 데이터를 Map 구조로 변형하여 Prompt 생성
        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("input", message);
        promptParameters.put("documents", String.join("\n", contentList));
        Prompt prompt = promptTemplate.create(promptParameters);

        // 생성된 프롬프트로부터 결과 생성 후 반환
        return chatClient.call(prompt).getResult().getOutput().getContent();
    }

}

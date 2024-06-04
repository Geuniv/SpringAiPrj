package kopo.poly.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageClient;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatController {

    private final ChatClient chatClient;

    private final ImageClient imageClient;

    @GetMapping(value = "sendRequest")
    public String getResponse(@RequestParam(value = "message", defaultValue = "Tell me a dad joke") String message) {
        log.info(this.getClass().getName() + ".getResponse Start !");

        log.info("message : " + message);

        // Get 요청으로부터 넘겨받은 문자열을 넘기고 응답 반환
        String answer = chatClient.call(message);

        log.info("answer : " + answer);

        log.info(this.getClass().getName() + ".getResponse End !");

        return answer;
    }

    @GetMapping(value = "/sendRequestforImage")
    public Image getResponseImage(@RequestParam(value = "prompt", defaultValue = "a little cat") String prompt) {
        log.info(this.getClass().getName() + ".getResponseImage Start !");

        log.info("text prompt : " + prompt);

        // 이미지 생성을 위한 프롬프트 생성
        ImagePrompt imagePrompt = new ImagePrompt(prompt);
        // 생성된 프롬프트를 ImageClient에 전달하여 결과 이미지 반환
        ImageResponse answer = imageClient.call(imagePrompt);

        log.info("answer : " + answer);

        log.info(this.getClass().getName() + ".getResponseImage End !");

        return answer.getResult().getOutput();
    }

    @PostMapping(value = "/getImageByDALLE")
    public Image getImageByDALLE(@RequestBody String prompt) {
        log.info(this.getClass().getName() + ".getImageByDALLE Start !");

        log.info("text prompt : " + prompt);

        // 전달된 프롬프트 값을 이미지 생성 옵션과 함께 ImageClient에 전달하여 결과 이미지 반환
        ImageResponse response = imageClient.call(
                new ImagePrompt (
                        prompt, // 전달된 프롬프트
                         OpenAiImageOptions.builder() // 너비, 높이 등 이미지 생성에 필요한 설정객체
                         .withWidth(1024)
                         .withHeight(1024)
                         .build()
                ));

        log.info(this.getClass().getName() + ".getImageByDALLE End !");

        return response.getResult().getOutput();
    }
}

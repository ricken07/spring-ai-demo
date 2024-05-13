package com.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mistralai.MistralAiChatClient;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class SpringAiDemo {

    private final Logger log = LoggerFactory.getLogger(SpringAiDemo.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringAiDemo.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(
            MistralAiChatClient mistralAiChatClient,
            OpenAiChatClient openAiChatClient) {
        return args -> {

            var result = openAiChatClient.call("Générer les noms de cinq pirates célèbres.");
            log.info("Mistral AI response: \n{}", result);

            log.info("====================================================");

            var response = mistralAiChatClient.call(
                    new Prompt(
                            "Décrire les tâches d'un senior développeur",
                            MistralAiChatOptions.builder()
                                    .withModel(MistralAiApi.ChatModel.TINY.getValue())
                                    .withTemperature(0.5f)
                                    /*.withMaxToken(100)*/
                                    .build()
            ));
            log.info("Mistral AI response with options: \n{}", response.getResult().getOutput().getContent());
        };
    }
}
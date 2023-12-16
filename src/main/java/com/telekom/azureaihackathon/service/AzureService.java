package com.telekom.azureaihackathon.service;

import com.telekom.azureaihackathon.model.ChatResponse;
import com.telekom.azureaihackathon.model.GptResponse;
import com.telekom.azureaihackathon.model.Message;
import com.telekom.azureaihackathon.model.StaticVariables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureService {

    @Qualifier("gptWebClient")
    private final WebClient gptWebClient;

    public ChatResponse getSimpleAnswerChatGpt(final String query, final String role) {
        if (StaticVariables.context.getMessages().isEmpty())
            StaticVariables.context.getMessages().add(new Message("system", String.format("You are a %s.", role)));
        StaticVariables.context.getMessages().add(new Message("user", query));
        log.info("Request to ChatGpt: "+StaticVariables.context);
        final var response = gptWebClient
            .post()
            .bodyValue(StaticVariables.context)
            .retrieve()
            .bodyToMono(GptResponse.class)
            .block();
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new RuntimeException("Not correct response from ChatGPT");
        }
        log.info("Response from ChatGpt: "+response);
        var previousResponse = response.getChoices().get(0).getMessage();
        StaticVariables.context.getMessages().add(previousResponse);
        return new ChatResponse(previousResponse.getContent(),response.getUsage().getTotal_tokens());
    }

    public ChatResponse getSimpleAnswerChatGpt(final String query) {
        if (StaticVariables.context.getMessages().isEmpty())
            StaticVariables.context.getMessages().add(new Message("system", "You are a helpful assistant."));
        StaticVariables.context.getMessages().add(new Message("user", query));
        final var response = gptWebClient
            .post()
            .bodyValue(StaticVariables.context)
            .retrieve()
            .bodyToMono(GptResponse.class)
            .block();
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new RuntimeException("Not correct response from ChatGPT");
        }
        var previousResponse = response.getChoices().get(0).getMessage();
        StaticVariables.context.getMessages().add(previousResponse);
        return new ChatResponse(previousResponse.getContent(), response.getUsage().getTotal_tokens());
    }
}

package net.konjarla.smagent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
public class ResponseAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        return modifyResponse(advisedRequest, advisedResponse);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
        return new MessageAggregator().aggregateAdvisedResponse(advisedResponses,
                advisedResponse -> modifyResponse(advisedRequest, advisedResponse));
    }

    private AdvisedResponse modifyResponse(AdvisedRequest advisedRequest, AdvisedResponse advisedResponse) {
        String replacedNewLines = advisedResponse.response().getResult().getOutput().getText().replace("\\n", "\n");
        return new AdvisedResponse(ChatResponse.builder()
                .generations(List.of(new Generation(new AssistantMessage(replacedNewLines))))
                .build(), advisedRequest.adviseContext());
    }
}

package net.konjarla.smagent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.smagent.model.SMAgentRecord;
import net.konjarla.smagent.tools.PhotoTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PhotoChatAgentService {
    private final ChatClient photoChatClient;
    private final PhotoTools photoTools;
    public static final String DEFAULT_ORCHESTRATOR_PROMPT = """
            Analyze this task and break it down into 2-3 distinct approaches:
            
            Task: {task}
            
            Return your response in this JSON format. Pleae DO NOT include json schema.:
            \\{
            "analysis": "Explain your understanding of the task and which variations would be valuable.
                         Focus on how each approach serves different aspects of the task.",
            "tasks": [
            	\\{
            	"type": "formal",
            	"description": "Write a precise, technical version that emphasizes specifications"
            	\\},
            	\\{
            	"type": "conversational",
            	"description": "Write an engaging, friendly version that connects with readers"
            	\\}
            ]
            \\}
            """;

    public SMAgentRecord.OrchestratorResponse processRequest(String taskDescription) {
        SMAgentRecord.OrchestratorResponse orchestratorResponse = this.photoChatClient.prompt()
                .user(u -> u.text(DEFAULT_ORCHESTRATOR_PROMPT)
                        .param("task", taskDescription))
                .tools(photoTools)
                .call()
                .entity(SMAgentRecord.OrchestratorResponse.class);
        assert orchestratorResponse != null;
        log.info(orchestratorResponse.analysis(), orchestratorResponse.tasks());
        return orchestratorResponse;
    }

    public String processQuery(String taskDescription) {
        String response = this.photoChatClient.prompt()
                .user(u -> u.text(taskDescription))
                .tools(photoTools)
                .call()
                .content();
        assert response != null;
        return response;
    }
}

package net.konjarla.smagent.controller;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.smagent.model.SMAgentRecord;
import net.konjarla.smagent.service.PhotoChatAgentService;
import net.konjarla.smagent.tools.PhotoTools;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@RequestMapping("/photo")
public class SearchController {
    @NonNull
    private PhotoChatAgentService photoChatAgentService;
    @NonNull
    private PhotoTools photoTools;

    @RequestMapping("/agent")
    public @ResponseBody SMAgentRecord.OrchestratorResponse process(@RequestParam(name = "task") String query) {
        return photoChatAgentService.processRequest(query);
    }

    @PostMapping(path = "/chat", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody SMAgentRecord.PhotoChatResponse processTextRequest(@RequestBody @NonNull SMAgentRecord.PhotoChat query) {
        String response = photoChatAgentService.processQuery(query.content());
        return new SMAgentRecord.PhotoChatResponse("mixed", response, photoTools.extractURL(response));
    }
}

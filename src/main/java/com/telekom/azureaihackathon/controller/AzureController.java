package com.telekom.azureaihackathon.controller;

import com.telekom.azureaihackathon.Util.JiraUtil;
import com.telekom.azureaihackathon.config.JiraClient;
import com.telekom.azureaihackathon.model.ChatResponse;
import com.telekom.azureaihackathon.model.StaticVariables;
import com.telekom.azureaihackathon.service.AzureService;
import com.telekom.azureaihackathon.service.WordParserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.telekom.azureaihackathon.model.StaticVariables.businessRequirements;

@Slf4j
@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class AzureController {
    private final AzureService azureService;
    private final WordParserService wordParserService;

    private final JiraClient jiraClient;
    private final JiraUtil jiraUtil;


    @PostMapping
    public ResponseEntity<ChatResponse> askChatGpt(@RequestBody String query) {
        return ResponseEntity.ok().body(azureService.getSimpleAnswerChatGpt(query));
    }

    @PostMapping("/uml")
    public ResponseEntity<ChatResponse> askChatGptAboutUml(@RequestBody String query) {
        return ResponseEntity.ok().body(wordParserService.getUml(azureService.getSimpleAnswerChatGpt(query)));
    }

    @DeleteMapping("/clean")
    public void askChatGpt() {
        log.info("clean context");
        StaticVariables.cleanContext();
    }

    @SneakyThrows
    @PostMapping(value = "/analyze/epic/{epicName}")
    public ResponseEntity<ChatResponse> analyzeEpic(@PathVariable String epicName, @RequestBody String role) {
        final var epic = jiraUtil.prepareEpicInfo(jiraClient.getIssue(epicName));
        var stories = jiraUtil.getInformationFromStories(jiraClient.getIssues(epicName));
        businessRequirements.add(epic.toString());
        businessRequirements.add("Here is a list of user stories for analysis");
        businessRequirements.add(stories.toString());
        businessRequirements.add("\nAnalyze this data and:\n" +
                "- highlight missing user stories and/or requirements in user stories;\n" +
                "- show redundant requirements in user stories;\n" +
                "- provide drafts of missing user stories;\n" +
                "- advise improvements of existing user stories (e.g. according to some quality standard);\n" +
                "- highlight missing test cases;\n" +
                "- show redundant test cases;\n" +
                "- show estimated test coverage, including negative test cases;\n" +
                "- provide a draft of test cases to achieve 80% test coverage;\n" +
                "- advise improvements of existing test cases.\n" +
                "Based on the analysis, create a report that contains each user story and test from " +
                "list of user stories for analysis and new user stories and test if needed with the following information:\n" +
                "- issueKey, if this is a new user story or test, mark it as NEW;\n" +
                "- type;\n" +
                "- name;\n" +
                "- acceptanceCriteria with a conclusion about the completeness of the information and instructions for improvement if necessary;\n" +
                "- description with a conclusion about the completeness of the information and instructions for improvement if necessary,\n" +
                "and show estimated test coverage of Epic in percentages.\n" +
                "Each user story that needs to be tested must have a targetIssueKey (link to the test)\n");

        return ResponseEntity.ok(azureService.getSimpleAnswerChatGpt(businessRequirements.toString(), role));
    }

    @SneakyThrows
    @PostMapping(value = "/analyze/uploadFile", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadFile(@RequestBody MultipartFile file) {
        wordParserService.processFiles(file);
        StringBuilder stringBuilder = new StringBuilder();
        businessRequirements.forEach(stringBuilder::append);
        return ResponseEntity.ok(stringBuilder.toString());
    }
}

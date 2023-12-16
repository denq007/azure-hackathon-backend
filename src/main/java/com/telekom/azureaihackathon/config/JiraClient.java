package com.telekom.azureaihackathon.config;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@Data
public class JiraClient {
    @Value("${jira.userName}")
    private String jiraUserName;
    @Value("${jira.password}")
    private String jiraPassword;
    @Value("${jira.baseUrl}")
    private String jiraBaseUrl;

    public JiraClient(
            @Value("${jira.userName}") String jiraUserName,
            @Value("${jira.password}") String jiraPassword,
            @Value("${jira.baseUrl}") String jiraBaseUrl
    ) {
        this.jiraUserName = jiraUserName;
        this.jiraPassword = jiraPassword;
        this.jiraBaseUrl = jiraBaseUrl;

        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        this.jiraRestClient = factory.createWithBasicHttpAuthentication(URI.create(jiraBaseUrl), jiraUserName, jiraPassword);
    }

    private final JiraRestClient jiraRestClient;

    public Issue getIssue(String issueKey) {
        return jiraRestClient.getIssueClient().getIssue(issueKey).claim();
    }

    public List<Issue> getIssues(String issueKey) throws IOException {

        Iterable<Issue> issuesIterable = jiraRestClient.getSearchClient()
                .searchJql("\"Epic Link\"=\"" + issueKey + "\"")
                .claim()
                .getIssues();

        List<Issue> issuesList = new ArrayList<>();
        for (Issue issue : issuesIterable) {
            System.out.println("Issue Key: " + issue.getKey() + ", Summary: " + issue.getSummary());
            issuesList.add(issue);
        }

        return issuesList;
    }
}

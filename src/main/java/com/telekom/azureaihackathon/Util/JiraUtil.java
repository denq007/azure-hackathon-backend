package com.telekom.azureaihackathon.Util;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueLinkType;
import com.telekom.azureaihackathon.model.Epic;
import com.telekom.azureaihackathon.model.UserStory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class JiraUtil {

    public List<UserStory> getInformationFromStories(List<Issue> issues) {
        List<UserStory> storyInformationList = new ArrayList<>();
        for (Issue issue : issues) {
            UserStory storyInformation = new UserStory();
            storyInformation.setIssueKey(issue.getKey());
            storyInformation.setType(issue.getIssueType().getName());
            storyInformation.setName(issue.getSummary());
            storyInformation.setAcceptanceCriteria(getFieldByName(issue,"Acceptance Criteria"));
            storyInformation.setDescription(issue.getDescription());
            storyInformation.setStatus(issue.getStatus().getName());
            if (issue.getIssueType().getName().equals("Story")) {
                storyInformation.setTargetIssueKey(getTestNumber(issue));
            }
            storyInformationList.add(storyInformation);
        }
        return storyInformationList;
    }

    private String getTestNumber(Issue issue) {
        Iterable<IssueLink> issueLinks = issue.getIssueLinks();
        if (issueLinks == null)
            return null;
        for (IssueLink issueLink : issueLinks) {
            IssueLinkType linkType = issueLink.getIssueLinkType();
            if (linkType != null && linkType.getDescription().equalsIgnoreCase("Tested by")) {
                return issueLink.getTargetIssueKey();
            }
        }
        return null;
    }

    private String getFieldByName(Issue issue,String fieldName)
    {
        Optional<Object> acceptanceCriteriaOptional = Optional.ofNullable(issue.getFieldByName(fieldName))
                .map(IssueField::getValue);
        return acceptanceCriteriaOptional.map(Object::toString).orElse(null);
    }

    public Epic prepareEpicInfo(Issue issue)
    {
       var epic=new Epic();
       epic.setName(issue.getSummary());
       epic.setStatus(issue.getStatus().getName());
       epic.setAcceptanceCriteria(getFieldByName(issue,"Acceptance Criteria"));
       epic.setDescription(issue.getDescription());
       epic.setIssueKey(issue.getKey());
       epic.setBusinessScope(getFieldByName(issue,"Business Scope"));
       return epic;
    }
}

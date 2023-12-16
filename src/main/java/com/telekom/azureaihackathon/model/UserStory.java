package com.telekom.azureaihackathon.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserStory {
    private String issueKey;
    private String name;
    private String acceptanceCriteria;
    private String description;
    private String status;
    private String type;
    private String targetIssueKey;
}
